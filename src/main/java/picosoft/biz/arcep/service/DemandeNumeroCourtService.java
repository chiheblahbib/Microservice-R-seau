package picosoft.biz.arcep.service;

import picosoft.biz.arcep.client.kernel.model.objects.AttachementInputDTO;
import picosoft.biz.arcep.client.kernel.model.global.GetRequestFileDefinitionDTO;
import picosoft.biz.arcep.client.kernel.model.global.AclClassFilesDto;
import java.util.Set;
import java.util.HashSet;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import picosoft.biz.arcep.Workflow.domain.BpmJob;
import picosoft.biz.arcep.Workflow.service.WorkflowService;
import picosoft.biz.arcep.client.currentuser.model.CurrentUser;
import picosoft.biz.arcep.client.kernel.intercomm.KernelInterface;
import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import picosoft.biz.arcep.client.kernel.model.acl.AclObjectIdentity;
import picosoft.biz.arcep.client.kernel.model.objects.AttachementInputDTO;
import picosoft.biz.arcep.client.kernel.model.objects.PublicAttachementDto;
import picosoft.biz.arcep.controller.errors.BadRequestAlertException;
import picosoft.biz.arcep.controller.errors.NumeroCourtErrors;
import picosoft.biz.arcep.domain.numerocourt.DemandeNumeroCourt;
import picosoft.biz.arcep.domain.shared.Commentaire;
import picosoft.biz.arcep.domain.shared.RapportTechnique;
import picosoft.biz.arcep.service.dto.RapportTechniqueDTO;
import picosoft.biz.arcep.repository.CommentaireRepository;
import picosoft.biz.arcep.repository.DemandeNumeroCourtRepository;
import picosoft.biz.arcep.service.criteria.DemandeNumeroCourtCriteria;
import picosoft.biz.arcep.domain.numerocourt.enumeration.*;
import picosoft.biz.arcep.service.dto.DemandeNumeroCourtDTO;
import picosoft.biz.arcep.service.dto.PersonneNumeroCourtDTO;
import picosoft.biz.arcep.domain.numerocourt.enumeration.ModeExploitation;
import picosoft.biz.arcep.domain.numerocourt.enumeration.NatureActivite;
import picosoft.biz.arcep.domain.numerocourt.enumeration.TypeNumeroCourt;
import picosoft.biz.arcep.service.dto.NumeroRattachementCourtDTO;
import picosoft.biz.arcep.service.dto.DemandeNumeroCourtInputDTO;
import picosoft.biz.arcep.service.dto.DemandeNumeroCourtOutputDTO;
import picosoft.biz.arcep.service.mapper.DemandeNumeroCourtInputMapper;
import picosoft.biz.arcep.service.mapper.DemandeNumeroCourtMapper;
import picosoft.biz.arcep.service.mapper.DemandeNumeroCourtOutputMapper;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service metier du dossier parent d'implantation.
 *
 * La transaction canonique reproduit celle de HomologationService :
 *   1. resoudre l'AclClass ;
 *   2. persister l'entite et obtenir sa reference du kernel ;
 *   3. appeler le moteur (_initAndNextTask au premier depot, _nextTask ensuite) ;
 *   4. repousser auteurs et lecteurs vers le kernel (applySecurity) et rattacher
 *      l'AclObjectIdentity.
 */
@Service
@Transactional
public class DemandeNumeroCourtService {

    private final Logger log = LoggerFactory.getLogger(DemandeNumeroCourtService.class);

    private final DemandeNumeroCourtRepository demandeNumeroCourtRepository;
    private final DemandeNumeroCourtQueryService demandeNumeroCourtQueryService;
    private final DemandeNumeroCourtMapper demandeNumeroCourtMapper;
    private final DemandeNumeroCourtInputMapper demandeNumeroCourtInputMapper;
    private final DemandeNumeroCourtOutputMapper demandeNumeroCourtOutputMapper;
    private final CommentaireRepository commentaireRepository;
    private final KernelInterface kernelInterface;
    private final WorkflowService workflowService;
    private final CurrentUser currentUser;

    public DemandeNumeroCourtService(DemandeNumeroCourtRepository demandeNumeroCourtRepository,
                                      DemandeNumeroCourtQueryService demandeNumeroCourtQueryService,
                                      DemandeNumeroCourtMapper demandeNumeroCourtMapper,
                                      DemandeNumeroCourtInputMapper demandeNumeroCourtInputMapper,
                                      DemandeNumeroCourtOutputMapper demandeNumeroCourtOutputMapper,
                                      CommentaireRepository commentaireRepository,
                                      KernelInterface kernelInterface,
                                      WorkflowService workflowService,
                                      CurrentUser currentUser) {
        this.demandeNumeroCourtRepository = demandeNumeroCourtRepository;
        this.demandeNumeroCourtQueryService = demandeNumeroCourtQueryService;
        this.demandeNumeroCourtMapper = demandeNumeroCourtMapper;
        this.demandeNumeroCourtInputMapper = demandeNumeroCourtInputMapper;
        this.demandeNumeroCourtOutputMapper = demandeNumeroCourtOutputMapper;
        this.commentaireRepository = commentaireRepository;
        this.kernelInterface = kernelInterface;
        this.workflowService = workflowService;
        this.currentUser = currentUser;
    }

    // ------------------------------------------------------------------ CRUD

    /**
     * Cree le dossier, ou le met a jour si le DTO porte deja un identifiant.
     *
     * Dans ce second cas on CHARGE l'instance geree avant d'appliquer les
     * modifications : construire une entite detachee puis la merger ferait lever
     * a Hibernate "a collection with cascade=all-delete-orphan was no longer
     * referenced", la collection du DTO etant une instance differente.
     */
    public DemandeNumeroCourtDTO save(DemandeNumeroCourtDTO dto) {
        if (dto.getId() != null) {
            Optional<DemandeNumeroCourt> existant = demandeNumeroCourtRepository.findById(dto.getId());
            if (existant.isPresent()) {
                DemandeNumeroCourt gere = existant.get();
                demandeNumeroCourtMapper.partialUpdate(gere, dto);
                rattacherEnfants(gere);
                return demandeNumeroCourtMapper.toDto(demandeNumeroCourtRepository.save(gere));
            }
        }
        DemandeNumeroCourt entity = demandeNumeroCourtMapper.toEntity(dto);
        rattacherEnfants(entity);
        return demandeNumeroCourtMapper.toDto(demandeNumeroCourtRepository.save(entity));
    }

    public DemandeNumeroCourtDTO update(Long id, DemandeNumeroCourtDTO dto) {
        DemandeNumeroCourt entity = demandeNumeroCourtRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(NumeroCourtErrors.OBJECT_NOT_FOUND,
                        NumeroCourtErrors.CLASS, NumeroCourtErrors.OBJECT_NOT_FOUND));
        demandeNumeroCourtMapper.partialUpdate(entity, dto);
        rattacherEnfants(entity);
        return demandeNumeroCourtMapper.toDto(demandeNumeroCourtRepository.save(entity));
    }

    public Page<DemandeNumeroCourtDTO> findAll(DemandeNumeroCourtCriteria criteria, Pageable pageable, Integer size) {
        return demandeNumeroCourtQueryService.findByCriteria(criteria, pageable, size);
    }

    public Optional<DemandeNumeroCourtDTO> findOne(Long id) {
        return demandeNumeroCourtRepository.findById(id).map(demandeNumeroCourtMapper::toDto);
    }

    public DemandeNumeroCourtOutputDTO byId(Long id) {
        DemandeNumeroCourt entity = demandeNumeroCourtRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(NumeroCourtErrors.OBJECT_NOT_FOUND,
                        NumeroCourtErrors.CLASS, NumeroCourtErrors.OBJECT_NOT_FOUND));
        return demandeNumeroCourtOutputMapper.toDto(entity);
    }

    /**
     * Supprime le dossier et ses enfants structurels (client, demandeur, stations,
     * sites, frequences) par cascade.
     *
     * Refuse si le dossier est engage : un circuit demarre laisse une instance
     * Flowable et des entrees ACL derriere lui, et une autorisation delivree est
     * un document qui ne doit pas disparaitre en silence.
     */
    public void delete(Long id) {
        DemandeNumeroCourt entity = demandeNumeroCourtRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(NumeroCourtErrors.OBJECT_NOT_FOUND,
                        NumeroCourtErrors.CLASS, NumeroCourtErrors.OBJECT_NOT_FOUND));

        if (entity.getWfProcessID() != null) {
            throw new BadRequestAlertException(NumeroCourtErrors.OBJECT_ENGAGED,
                    NumeroCourtErrors.CLASS, NumeroCourtErrors.OBJECT_ENGAGED);
        }
        // Une autorisation delivree est un document officiel : le dossier qui la
        // porte ne s'efface pas, meme si son circuit est termine.
        if (entity.getAttestations() != null && !entity.getAttestations().isEmpty()) {
            throw new BadRequestAlertException(NumeroCourtErrors.OBJECT_ENGAGED,
                    NumeroCourtErrors.CLASS, NumeroCourtErrors.OBJECT_ENGAGED);
        }
        // Pas de garde par site : contrairement aux stations d'une demande
        // d'implantation, les sites d'un reseau ne portent pas de circuit propre.
        // Le reseau est autorise d'un bloc, et c'est le circuit du dossier -- teste
        // juste au-dessus -- qui protege la suppression.
        demandeNumeroCourtRepository.delete(entity);
    }

    // -------------------------------------------------------------- workflow

    /**
     * Premier depot : cree le dossier et demarre le circuit parent.
     */
    /**
     * Champs exiges pour SOUMETTRE, et seulement pour soumettre.
     *
     * Ces contraintes ne peuvent pas vivre en @NotBlank sur le DTO : le meme DTO
     * sert au brouillon, qui est legitimement partiel -- on ne perd pas une heure
     * de saisie parce qu'une coordonnee manque. La distinction brouillon /
     * soumission n'existe qu'ici, au niveau du service.
     *
     * Le message nomme TOUT ce qui manque en une fois, plutot que de renvoyer
     * l'utilisateur au premier champ vide et de recommencer au suivant.
     */
    /**
     * Ce qui manque pour soumettre, enonce d'un seul coup.
     *
     * On rassemble TOUS les manques avant de repondre : renvoyer la premiere
     * erreur venue obligerait l'usager a soumettre autant de fois qu'il a
     * d'oublis.
     */
    public void exigerPourSoumission(DemandeNumeroCourtInputDTO input) {
        List<String> manques = new ArrayList<>();

        if (input.getNatureDemande() == null) { manques.add("la nature de la demande"); }

        // Le formulaire exige une copie de l'ancienne autorisation en cas de
        // renouvellement : sans sa reference, l'instruction ne peut pas la retrouver.
        if (input.getNatureDemande() == NatureDemande.RENOUVELLEMENT
                && estVide(input.getReferenceAutorisationAnterieure())) {
            manques.add("la reference de l'autorisation a renouveler");
        }

        // Le titulaire peut etre une societe ou un particulier. La raison sociale
        // va dans `company`, le nom d'une personne physique dans `clientName` :
        // n'exiger que le premier interdisait a un particulier de deposer, alors
        // que le formulaire lui ouvre explicitement ce droit.
        if (input.getClient() == null
                || (estVide(input.getClient().getCompany())
                    && estVide(input.getClient().getClientName()))) {
            manques.add("l'identite du titulaire du reseau");
        }

        // Rubriques 1 et 3 : deux personnes distinctes, et le formulaire les
        // separe parce que l'instruction a besoin de savoir qui repond du reseau.
        // ----- le representant, rubrique 2
        PersonneNumeroCourtDTO r = input.getRepresentant();
        if (r == null) {
            manques.add("le representant de l'operateur");
        } else {
            if (estVide(r.getNom()))      { manques.add("le representant : le nom"); }
            if (estVide(r.getPrenoms()))  { manques.add("le representant : les prenoms"); }
            if (estVide(r.getEmail()))    { manques.add("le representant : l'adresse electronique"); }
            if (estVide(r.getAdressePermanente())) {
                manques.add("le representant : l'adresse permanente");
            }
        }

        // ----- la nature de la demande, rubrique 3
        if (input.getNatureDemande() == null) {
            manques.add("la nature de la demande");
        }

        // ----- la nature de l'activite, rubrique 3
        if (input.getNatureActivite() == null) {
            manques.add("la nature de l'activite");
        } else if (input.getNatureActivite() == NatureActivite.AUTRE
                && estVide(input.getNatureActivitePrecision())) {
            // « Autre reseau ou service » sans texte ne dit rien a l'instructeur.
            manques.add("preciser l'autre reseau ou service");
        }
        if (input.getPorteeReseau() == null) {
            manques.add("s'il s'agit d'un nouveau reseau ou d'une extension");
        }

        // ----- la specification de services, rubrique 4
        if (input.getTypeNumero() == null) {
            // Sans lui, aucune redevance ne peut etre arretee.
            manques.add("le type de numero (classique ou gold)");
        }
        if (input.getTypeExploitation() == null) {
            manques.add("le type d'exploitation");
        }
        if (input.getModeExploitation() == null) {
            manques.add("le mode d'exploitation (numero normal ou vert)");
        } else if (input.getModeExploitation() == ModeExploitation.NORMAL
                && input.getTypeFacturation() == null) {
            // Un numero normal fait payer l'appelant : il faut dire comment.
            // La question ne se pose pas pour un numero vert.
            manques.add("le type de facturation du numero normal");
        }

        // Au moins un numero de rattachement : un numero court qui n'aboutit
        // nulle part ne rend aucun service.
        if (input.getNumerosRattachement() == null || input.getNumerosRattachement().isEmpty()) {
            manques.add("au moins un numero long ou fixe de rattachement");
        } else {
            for (int i = 0; i < input.getNumerosRattachement().size(); i++) {
                NumeroRattachementCourtDTO n = input.getNumerosRattachement().get(i);
                if (n == null || estVide(n.getNumero())) {
                    manques.add("le numero de rattachement n" + (i + 1));
                }
            }
        }

        // ----- le trafic, rubrique 4 : quatre cases, au moins une cochee
        boolean unTraficCoche = Boolean.TRUE.equals(input.getTraficDonnees())
                || Boolean.TRUE.equals(input.getTraficVoix())
                || Boolean.TRUE.equals(input.getTraficSms())
                || Boolean.TRUE.equals(input.getTraficAutres());
        if (!unTraficCoche) {
            manques.add("au moins un type de trafic a ecouler");
        }
        if (Boolean.TRUE.equals(input.getTraficAutres())
                && estVide(input.getTraficAutresPrecision())) {
            manques.add("preciser l'autre type de trafic");
        }

        // ----- le point focal, rubrique 4
        if (estVide(input.getPointFocalNom()))   { manques.add("le point focal : le nom"); }
        if (estVide(input.getPointFocalEmail())) { manques.add("le point focal : le courriel"); }

        if (!manques.isEmpty()) {
            throw new BadRequestAlertException(
                    "Le dossier ne peut pas etre soumis, il manque : " + String.join(", ", manques),
                    NumeroCourtErrors.CLASS, NumeroCourtErrors.OBJECT_NOT_VALID);
        }
    }

    /** Vrai pour une chaine nulle, vide ou faite d'espaces. */
    private static boolean estVide(String v) {
        return v == null || v.trim().isEmpty();
    }

    private void exigerPiecesObligatoires(DemandeNumeroCourtInputDTO input, AclClass aclClass) {
        List<GetRequestFileDefinitionDTO> attendues;
        try {
            AclClassFilesDto referentiel =
                    kernelInterface.getFileDefinitionsByClassName(aclClass.getSimpleName());
            attendues = referentiel == null ? null : referentiel.getRequestFileDefinition();
        } catch (Exception e) {
            log.warn("Referentiel des pieces injoignable pour {} : {}. "
                    + "La soumission n'est pas bloquee.", aclClass.getSimpleName(), e.getMessage());
            return;
        }
        if (attendues == null || attendues.isEmpty()) {
            return;
        }

        Set<String> fournies = new HashSet<>();
        if (input.getAttachements() != null) {
            for (AttachementInputDTO piece : input.getAttachements()) {
                if (piece != null && piece.getReqFileDefName() != null) {
                    fournies.add(piece.getReqFileDefName());
                }
            }
        }
        // Pieces publiees lors d'un enregistrement precedent -- declarees par le client.
        if (input.getLabelsPostAttachments() != null) {
            for (String nom : input.getLabelsPostAttachments().split("[,;]")) {
                if (!nom.trim().isEmpty()) { fournies.add(nom.trim()); }
            }
        }

        List<String> manquantes = new ArrayList<>();
        for (GetRequestFileDefinitionDTO d : attendues) {
            if (Boolean.TRUE.equals(d.getFileRequired())
                    && !fournies.contains(d.getName())
                    && !fournies.contains(d.getLabel())) {
                manquantes.add(d.getLabel() != null ? d.getLabel() : d.getName());
            }
        }

        if (!manquantes.isEmpty()) {
            throw new BadRequestAlertException(
                    "Pieces obligatoires absentes : " + String.join(", ", manquantes),
                    NumeroCourtErrors.CLASS, NumeroCourtErrors.PIECES_MANQUANTES);
        }
    }

    /**
     * Les frais de dossier, rubrique 9 du formulaire : un FORFAIT de
     * 200 000 FCFA par numerocourt.
     *
     * Pas de grille par service comme le reseau -- l'annexe de ce formulaire
     * ne connait qu'un montant. Il est arrete au depot et conserve sur le
     * dossier : un bareme qui change ne doit pas reecrire un dossier deja
     * instruit.
     */
    /**
     * L'annexe tarifaire, qui ne connait que deux lignes :
     *
     *     Classique    4 000 000 FCFA de redevance annuelle
     *     Gold        10 000 000 FCFA de redevance annuelle
     *
     * Pas de frais de DOSSIER : l'annexe n'en porte pas. `fraisDossier` reste
     * donc vide -- y recopier la redevance melangerait deux choses que le
     * formulaire distingue, un droit d'entree et un loyer annuel.
     */
    private static final BigDecimal REDEVANCE_CLASSIQUE = new BigDecimal("4000000");
    private static final BigDecimal REDEVANCE_GOLD = new BigDecimal("10000000");
    private static final String DEVISE_PAR_DEFAUT = "XAF";

    /**
     * Arrete la redevance annuelle, une fois pour toutes.
     *
     * Ne fait rien si elle est deja posee : un dossier repris garde le montant
     * qui lui a ete reclame, meme si le bareme a change depuis.
     *
     * Sans type de numero, on ne pose RIEN : la soumission l'exige de toute
     * facon, et deviner « classique » ferait facturer 4 millions un gold a
     * 10.
     */
    void arreterFraisDossier(DemandeNumeroCourtInputDTO input) {
        if (input.getRedevanceAnnuelle() == null && input.getTypeNumero() != null) {
            input.setRedevanceAnnuelle(
                    input.getTypeNumero() == TypeNumeroCourt.GOLD
                            ? REDEVANCE_GOLD : REDEVANCE_CLASSIQUE);
        }
        if (input.getDeviseFrais() == null) {
            input.setDeviseFrais(DEVISE_PAR_DEFAUT);
        }
    }

    public DemandeNumeroCourtOutputDTO initAndSubmit(DemandeNumeroCourtInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(NumeroCourtErrors.ACL_CLASS_NOT_FOUND,
                    NumeroCourtErrors.CLASS, NumeroCourtErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);

        input.setCreatedDate(ZonedDateTime.now());
        input.setSendedDate(ZonedDateTime.now());
        arreterFraisDossier(input);

        DemandeNumeroCourt entity = toEntityOrLoad(input);
        entity = demandeNumeroCourtRepository.save(entity);

        DemandeNumeroCourtOutputDTO output = demandeNumeroCourtOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        if (entity.getReference() == null) {
            entity.setReference(kernelInterface.getSequenceNumberByClass(
                    new JSONObject(output).toString(), aclClass.getClasse()));
            output.setReference(entity.getReference());
        }

        ouvrirRapportTechnique(entity);

        publierPiecesJointes(input, output, entity, aclClass);

        BpmJob bpmJob = workflowService._initAndNextTask(aclClass.getFwProcess(),
                input.getDecision(), input.getWfComment(), output, aclClass);

        entity = appliquerRetourMoteur(bpmJob, aclClass);

        saveWorkflowCommentaire(input.getCommentaire(), entity, aclClass);

        return demandeNumeroCourtOutputMapper.toDto(entity);
    }

    /**
     * Transitions suivantes : le dossier existe et son instance de process tourne.
     */
    public DemandeNumeroCourtOutputDTO submit(DemandeNumeroCourtInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(NumeroCourtErrors.ACL_CLASS_NOT_FOUND,
                    NumeroCourtErrors.CLASS, NumeroCourtErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);
        if (input.getId() == null) {
            throw new BadRequestAlertException(NumeroCourtErrors.OBJECT_NOT_VALID,
                    NumeroCourtErrors.CLASS, NumeroCourtErrors.OBJECT_NOT_VALID);
        }

        DemandeNumeroCourt entity = demandeNumeroCourtRepository.findById(input.getId())
                .orElseThrow(() -> new BadRequestAlertException(NumeroCourtErrors.OBJECT_NOT_FOUND,
                        NumeroCourtErrors.CLASS, NumeroCourtErrors.OBJECT_NOT_FOUND));

        demandeNumeroCourtInputMapper.partialUpdate(entity, input);
        entity = demandeNumeroCourtRepository.save(entity);

        DemandeNumeroCourtOutputDTO output = demandeNumeroCourtOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        BpmJob bpmJob = workflowService._nextTask(entity.getWfProcessID(),
                input.getDecision(), input.getWfComment(), output, aclClass);

        entity = appliquerRetourMoteur(bpmJob, aclClass);

        saveWorkflowCommentaire(input.getCommentaire(), entity, aclClass);

        return demandeNumeroCourtOutputMapper.toDto(entity);
    }

    /**
     * Enregistrement sans franchir d'etape : le circuit n'est pas sollicite.
     */
    public DemandeNumeroCourtOutputDTO saveAsDraft(DemandeNumeroCourtInputDTO input, AclClass aclClass) {
        if (aclClass == null) {
            throw new BadRequestAlertException(NumeroCourtErrors.ACL_CLASS_NOT_FOUND,
                    NumeroCourtErrors.CLASS, NumeroCourtErrors.ACL_CLASS_NOT_FOUND);
        }

        DemandeNumeroCourt entity = toEntityOrLoad(input);
        entity = demandeNumeroCourtRepository.save(entity);

        DemandeNumeroCourtOutputDTO output = demandeNumeroCourtOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        // le brouillon n'est visible que de son auteur tant qu'il n'est pas soumis
        entity = persistAndApplySecurity(entity,
                Arrays.asList(currentUser.getEmployeSid()), new ArrayList<>(), aclClass);

        return demandeNumeroCourtOutputMapper.toDto(entity);
    }

    // ------------------------------------------------------------- internes

    private DemandeNumeroCourt toEntityOrLoad(DemandeNumeroCourtInputDTO input) {
        if (input.getId() != null) {
            Optional<DemandeNumeroCourt> existant = demandeNumeroCourtRepository.findById(input.getId());
            if (existant.isPresent()) {
                DemandeNumeroCourt entity = existant.get();
                demandeNumeroCourtInputMapper.partialUpdate(entity, input);
                return entity;
            }
        }
        return demandeNumeroCourtInputMapper.toEntity(input);
    }

    /**
     * Reporte sur l'entite ce que le moteur a decide, puis rejoue la securite.
     */
    private DemandeNumeroCourt appliquerRetourMoteur(BpmJob bpmJob, AclClass aclClass) throws Exception {
        DemandeNumeroCourtOutputDTO output = (DemandeNumeroCourtOutputDTO) bpmJob.getDataObject();

        // On repart de l'instance GEREE, jamais d'une entite reconstruite : le DTO
        // porterait une collection neuve, et le merge ferait lever Hibernate sur
        // orphanRemoval. Il ne porte pas non plus l'aclObjectIdentity.
        DemandeNumeroCourt entity = output.getId() != null
                ? demandeNumeroCourtRepository.findById(output.getId()).orElse(null)
                : null;
        if (entity == null) {
            entity = demandeNumeroCourtOutputMapper.toEntity(output);
        } else {
            demandeNumeroCourtOutputMapper.partialUpdate(entity, output);
        }
        entity.setWfProcessID(bpmJob.getProcessID());
        entity.setActivityName(bpmJob.getActivityName());
        entity.setEndProcess(bpmJob.getEndProcess());
        entity.setAssignee(bpmJob.getAssignee());

        return persistAndApplySecurity(entity, bpmJob.getAuthors(), bpmJob.getReaders(), aclClass);
    }

    /**
     * Rattache les enfants, persiste, puis pousse les habilitations au kernel.
     */
    private DemandeNumeroCourt persistAndApplySecurity(DemandeNumeroCourt entity,
                                                        List<String> authors, List<String> readers,
                                                        AclClass aclClass) {
        if (entity.getId() != null) {
            demandeNumeroCourtRepository.findById(entity.getId()).ifPresent(recent -> {
                if (entity.getAclObjectIdentity() == null) {
                    entity.setAclObjectIdentity(recent.getAclObjectIdentity());
                }
            });
        }

        rattacherEnfants(entity);

        DemandeNumeroCourt persiste = demandeNumeroCourtRepository.save(entity);

        try {
            if (authors != null && readers != null) {
                kernelInterface.applySecurity(aclClass.getClasse(), persiste.getId(), authors, readers,
                        new ArrayList<>(), null, null, persiste.getAclObjectIdentity() == null, false);
            }
            if (persiste.getAclObjectIdentity() == null) {
                persiste = setAclObjectIdentity(persiste, aclClass);
                persiste = demandeNumeroCourtRepository.save(persiste);
            }
        } catch (Exception e) {
            log.error("applySecurity a echoue pour le dossier {} : {}", persiste.getId(), e.toString());
        }

        return persiste;
    }


    /**
     * Pose les references retour sur tout le graphe avant persistance.
     *
     * Indispensable : la cascade PERSIST/MERGE ecrit bien les enfants, mais c'est
     * l'enfant qui porte la FK. Sans ce cablage ils sont persistes en orphelins,
     * et la reponse HTTP parait pourtant correcte puisqu'elle serialise le graphe
     * en memoire, pas ce qui est relu de la base.
     */
    /**
     * Repose la cle etrangere sur chaque enfant.
     *
     * MapStruct construit les enfants sans connaitre leur parent : sans ce
     * passage, la cascade les insererait avec une cle nulle et la base les
     * refuserait -- ou pire, les accepterait detaches du dossier.
     */
    /**
     * Ouvre le rapport technique du dossier, s'il n'en a pas deja un.
     *
     * Appele au depot, quand le circuit demarre : c'est la que l'instruction
     * commence, donc que le rapport a lieu d'exister. Il est cree vide -- la
     * conclusion est le travail de l'instructeur --, mais avec sa classe ACL
     * et sa reference, sans lesquelles il ne pourrait pas porter de pieces.
     *
     * Silencieux si la classe n'est pas declaree au kernel : un referentiel
     * incomplet ne doit pas faire echouer un depot. Le rapport sera ouvert au
     * depot suivant, une fois la classe declaree.
     */
    private void ouvrirRapportTechnique(DemandeNumeroCourt entity) {
        if (entity.getRapportTechnique() != null) {
            return;
        }
        AclClass classeRapport =
                kernelInterface.getaclClassByClassName(RapportTechnique.class.getName());
        if (classeRapport == null) {
            log.warn("classe ACL absente pour {} : rapport technique non ouvert",
                    RapportTechnique.class.getName());
            return;
        }
        RapportTechnique rapport = new RapportTechnique();
        rapport.setClassId(classeRapport.getId());
        rapport.setDateRapport(ZonedDateTime.now());
        rapport.setReference(kernelInterface.getSequenceNumberByClass(
                new JSONObject(new RapportTechniqueDTO()).toString(),
                classeRapport.getClasse()));
        rapport.setDemandeNumeroCourt(entity);
        entity.setRapportTechnique(rapport);
    }

    private void rattacherEnfants(DemandeNumeroCourt entity) {
        if (entity.getClient() != null) {
            entity.getClient().setDemandeNumeroCourt(entity);
        }
        if (entity.getRepresentant() != null) {
            entity.getRepresentant().setDemandeNumeroCourt(entity);
        }
        if (entity.getNumerosRattachement() != null) {
            entity.getNumerosRattachement().forEach(e -> e.setDemandeNumeroCourt(entity));
        }
        if (entity.getRapportTechnique() != null) {
            entity.getRapportTechnique().setDemandeNumeroCourt(entity);
        }
        if (entity.getAttestations() != null) {
            entity.getAttestations().forEach(a -> a.setDemandeNumeroCourt(entity));
        }
    }

    public DemandeNumeroCourt setAclObjectIdentity(DemandeNumeroCourt entity, AclClass aclClass) {
        Integer aclObjectIdentityID = kernelInterface.findACLObjectIdentity(aclClass.getId(), entity.getId());
        if (aclObjectIdentityID == null) {
            return entity;
        }
        AclObjectIdentity aclObjectIdentity = new AclObjectIdentity();
        aclObjectIdentity.setId(aclObjectIdentityID.longValue());
        entity.setAclObjectIdentity(aclObjectIdentity);
        return entity;
    }

    private void publierPiecesJointes(DemandeNumeroCourtInputDTO input, DemandeNumeroCourtOutputDTO output,
                                      DemandeNumeroCourt entity, AclClass aclClass) {
        if (input.getAttachements() == null || input.getAttachements().isEmpty()) {
            return;
        }
        for (AttachementInputDTO attachement : input.getAttachements()) {
            if (attachement == null) {
                continue;
            }
            try {
                if (attachement.getUuid() == null) {
                    if (attachement.getFileBase64() == null) {
                        continue;
                    }
                    PublicAttachementDto piece = new PublicAttachementDto();
                    piece.setFileName(attachement.getFileName());
                    piece.setReqFileDefName(attachement.getReqFileDefName());
                    piece.setClassId(aclClass.getId());
                    piece.setObjectId(entity.getId());
                    piece.setDecodedBytes(Base64.getDecoder().decode(attachement.getFileBase64()));
                    piece.setObjectData(new JSONObject(output).toString());
                    kernelInterface.publicAttachement(piece);
                } else if ("DELETE".equalsIgnoreCase(attachement.getAction())) {
                    kernelInterface.deleteFileRessource(attachement.getUuid(), "");
                }
            } catch (Exception e) {
                log.error("piece jointe refusee pour le dossier {} : {}", entity.getId(), e.toString());
            }
        }
    }

    private void saveWorkflowCommentaire(String texte, DemandeNumeroCourt entity, AclClass aclClass) {
        if (texte == null || texte.trim().isEmpty()) {
            return;
        }
        Commentaire commentaire = new Commentaire();
        commentaire.setAuteur(currentUser.getEmployeSid());
        commentaire.setDescription(texte);
        commentaire.setDateSaisie(ZonedDateTime.now());
        commentaire.setClassId(aclClass.getId());
        commentaire.setObjectID(entity.getId());
        commentaire.setExterne(false);
        commentaireRepository.save(commentaire);
    }
}
