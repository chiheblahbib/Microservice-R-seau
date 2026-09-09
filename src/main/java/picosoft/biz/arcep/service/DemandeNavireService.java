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
import picosoft.biz.arcep.controller.errors.NavireErrors;
import picosoft.biz.arcep.domain.navire.DemandeNavire;
import picosoft.biz.arcep.domain.shared.Commentaire;
import picosoft.biz.arcep.domain.shared.RapportTechnique;
import picosoft.biz.arcep.service.dto.RapportTechniqueDTO;
import picosoft.biz.arcep.repository.CommentaireRepository;
import picosoft.biz.arcep.repository.DemandeNavireRepository;
import picosoft.biz.arcep.service.criteria.DemandeNavireCriteria;
import picosoft.biz.arcep.domain.navire.enumeration.*;
import picosoft.biz.arcep.service.dto.DemandeNavireDTO;
import picosoft.biz.arcep.service.dto.ApplicantDTO;
import picosoft.biz.arcep.service.dto.EquipementBordNavireDTO;
import picosoft.biz.arcep.service.dto.DemandeNavireInputDTO;
import picosoft.biz.arcep.service.dto.DemandeNavireOutputDTO;
import picosoft.biz.arcep.service.mapper.DemandeNavireInputMapper;
import picosoft.biz.arcep.service.mapper.DemandeNavireMapper;
import picosoft.biz.arcep.service.mapper.DemandeNavireOutputMapper;

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
public class DemandeNavireService {

    private final Logger log = LoggerFactory.getLogger(DemandeNavireService.class);

    private final DemandeNavireRepository demandeNavireRepository;
    private final DemandeNavireQueryService demandeNavireQueryService;
    private final DemandeNavireMapper demandeNavireMapper;
    private final DemandeNavireInputMapper demandeNavireInputMapper;
    private final DemandeNavireOutputMapper demandeNavireOutputMapper;
    private final CommentaireRepository commentaireRepository;
    private final KernelInterface kernelInterface;
    private final WorkflowService workflowService;
    private final CurrentUser currentUser;

    public DemandeNavireService(DemandeNavireRepository demandeNavireRepository,
                                      DemandeNavireQueryService demandeNavireQueryService,
                                      DemandeNavireMapper demandeNavireMapper,
                                      DemandeNavireInputMapper demandeNavireInputMapper,
                                      DemandeNavireOutputMapper demandeNavireOutputMapper,
                                      CommentaireRepository commentaireRepository,
                                      KernelInterface kernelInterface,
                                      WorkflowService workflowService,
                                      CurrentUser currentUser) {
        this.demandeNavireRepository = demandeNavireRepository;
        this.demandeNavireQueryService = demandeNavireQueryService;
        this.demandeNavireMapper = demandeNavireMapper;
        this.demandeNavireInputMapper = demandeNavireInputMapper;
        this.demandeNavireOutputMapper = demandeNavireOutputMapper;
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
    public DemandeNavireDTO save(DemandeNavireDTO dto) {
        if (dto.getId() != null) {
            Optional<DemandeNavire> existant = demandeNavireRepository.findById(dto.getId());
            if (existant.isPresent()) {
                DemandeNavire gere = existant.get();
                demandeNavireMapper.partialUpdate(gere, dto);
                rattacherEnfants(gere);
                return demandeNavireMapper.toDto(demandeNavireRepository.save(gere));
            }
        }
        DemandeNavire entity = demandeNavireMapper.toEntity(dto);
        rattacherEnfants(entity);
        return demandeNavireMapper.toDto(demandeNavireRepository.save(entity));
    }

    public DemandeNavireDTO update(Long id, DemandeNavireDTO dto) {
        DemandeNavire entity = demandeNavireRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(NavireErrors.OBJECT_NOT_FOUND,
                        NavireErrors.CLASS, NavireErrors.OBJECT_NOT_FOUND));
        demandeNavireMapper.partialUpdate(entity, dto);
        rattacherEnfants(entity);
        return demandeNavireMapper.toDto(demandeNavireRepository.save(entity));
    }

    public Page<DemandeNavireDTO> findAll(DemandeNavireCriteria criteria, Pageable pageable, Integer size) {
        return demandeNavireQueryService.findByCriteria(criteria, pageable, size);
    }

    public Optional<DemandeNavireDTO> findOne(Long id) {
        return demandeNavireRepository.findById(id).map(demandeNavireMapper::toDto);
    }

    public DemandeNavireOutputDTO byId(Long id) {
        DemandeNavire entity = demandeNavireRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(NavireErrors.OBJECT_NOT_FOUND,
                        NavireErrors.CLASS, NavireErrors.OBJECT_NOT_FOUND));
        return demandeNavireOutputMapper.toDto(entity);
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
        DemandeNavire entity = demandeNavireRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(NavireErrors.OBJECT_NOT_FOUND,
                        NavireErrors.CLASS, NavireErrors.OBJECT_NOT_FOUND));

        if (entity.getWfProcessID() != null) {
            throw new BadRequestAlertException(NavireErrors.OBJECT_ENGAGED,
                    NavireErrors.CLASS, NavireErrors.OBJECT_ENGAGED);
        }
        // Une autorisation delivree est un document officiel : le dossier qui la
        // porte ne s'efface pas, meme si son circuit est termine.
        if (entity.getAttestations() != null && !entity.getAttestations().isEmpty()) {
            throw new BadRequestAlertException(NavireErrors.OBJECT_ENGAGED,
                    NavireErrors.CLASS, NavireErrors.OBJECT_ENGAGED);
        }
        // Pas de garde par site : contrairement aux stations d'une demande
        // d'implantation, les sites d'un reseau ne portent pas de circuit propre.
        // Le reseau est autorise d'un bloc, et c'est le circuit du dossier -- teste
        // juste au-dessus -- qui protege la suppression.
        demandeNavireRepository.delete(entity);
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
    public void exigerPourSoumission(DemandeNavireInputDTO input) {
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

        // ----- le demandeur, rubrique 1
        //
        // Le PROPRIETAIRE (rubrique 2) n'est PAS exige : le formulaire porte
        // « a ne renseigner que si le demandeur indique ci-dessus n'est pas le
        // proprietaire ». Le reclamer refuserait le cas le plus courant, celui
        // ou l'armateur depose pour son propre navire.
        //
        // La personne vit sur `applicant`, la table partagee : UNE SEULE par
        // dossier, en paire avec le `client` qui porte la structure. Le nom et
        // les prenoms s'y fondent dans `applicantName`, comme chez ASI.
        ApplicantDTO r = input.getApplicant();
        if (r == null) {
            manques.add("le demandeur de l'autorisation");
        } else {
            if (estVide(r.getApplicantName())) { manques.add("le demandeur : l'identite"); }
            if (estVide(r.getQualification())) { manques.add("le demandeur : la fonction"); }
            if (estVide(r.getEmail()))         { manques.add("le demandeur : l'adresse electronique"); }
            if (estVide(r.getAddress())) {
                manques.add("le demandeur : l'adresse permanente");
            }
        }

        // ----- la nature de la demande, rubrique 3
        if (input.getNatureDemande() == null) {
            manques.add("la nature de la demande");
        }

        // ----- les equipements de bord, rubrique 5
        //
        // Une station sans aucun equipement declare n'a rien a autoriser :
        // c'est le coeur technique du dossier.
        if (input.getEquipements() == null || input.getEquipements().isEmpty()) {
            manques.add("au moins un equipement de bord");
        } else {
            for (int i = 0; i < input.getEquipements().size(); i++) {
                EquipementBordNavireDTO e = input.getEquipements().get(i);
                String ou = "l'equipement n" + (i + 1);
                if (e == null || estVide(e.getDesignation())) {
                    manques.add(ou + " : la designation du materiel");
                }
                if (e != null && estVide(e.getBandesFrequences())) {
                    manques.add(ou + " : les bandes de frequences");
                }
            }
        }

        // ----- le trafic, rubrique 6
        //
        // Au moins un type coche : le formulaire en propose trois, cumulables,
        // mais n'admet pas qu'aucun ne le soit.
        boolean unTraficCoche = Boolean.TRUE.equals(input.getTraficVoix())
                || Boolean.TRUE.equals(input.getTraficDonnees())
                || Boolean.TRUE.equals(input.getTraficAutres());
        if (!unTraficCoche) {
            manques.add("au moins un type de trafic a ecouler");
        }
        if (Boolean.TRUE.equals(input.getTraficAutres())
                && estVide(input.getTraficAutresPrecision())) {
            manques.add("la precision du trafic « autres »");
        }
        if (!Boolean.TRUE.equals(input.getTraficNational())
                && !Boolean.TRUE.equals(input.getTraficInternational())) {
            manques.add("la nature du trafic (national ou international)");
        }

        if (!manques.isEmpty()) {
            throw new BadRequestAlertException(
                    "Le dossier ne peut pas etre soumis, il manque : " + String.join(", ", manques),
                    NavireErrors.CLASS, NavireErrors.OBJECT_NOT_VALID);
        }
    }

    /** Vrai pour une chaine nulle, vide ou faite d'espaces. */
    private static boolean estVide(String v) {
        return v == null || v.trim().isEmpty();
    }

    private void exigerPiecesObligatoires(DemandeNavireInputDTO input, AclClass aclClass) {
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
                    NavireErrors.CLASS, NavireErrors.PIECES_MANQUANTES);
        }
    }

    /**
     * L'annexe tarifaire du formulaire, qui porte DEUX montants :
     *
     *     Frais de dossier      par navire   200 000 FCFA
     *     Redevance annuelle*               700 000 FCFA
     *     * Payable apres que l'ARCEP a juge le dossier conforme.
     *
     * Les deux sont arretes au depot et conserves sur le dossier -- un bareme
     * qui change ne doit pas reecrire ce qui a ete reclame a un dossier deja
     * instruit. Mais ils ne se declenchent pas au meme moment : les frais sont
     * dus au depot, la redevance seulement apres une decision de conformite.
     * Le back ne fait donc qu'enregistrer le montant applicable ; c'est le
     * circuit qui declenchera la seconde, quand RefTarif y sera cable.
     */
    private static final BigDecimal FRAIS_DOSSIER_NAVIRE = new BigDecimal("200000");
    private static final BigDecimal REDEVANCE_ANNUELLE_NAVIRE = new BigDecimal("700000");
    private static final String DEVISE_PAR_DEFAUT = "XAF";

    /**
     * Arrete les montants du dossier, une fois pour toutes.
     *
     * Ne fait rien si le montant est deja pose : un dossier repris garde celui
     * qui lui a ete reclame, meme si le bareme a change depuis. Les deux
     * montants sont testes SEPAREMENT -- un dossier ancien peut porter les
     * frais sans la redevance, celle-ci n'ayant ete ajoutee qu'ici.
     */
    void arreterFraisDossier(DemandeNavireInputDTO input) {
        if (input.getFraisDossier() == null) {
            input.setFraisDossier(FRAIS_DOSSIER_NAVIRE);
        }
        if (input.getRedevanceAnnuelle() == null) {
            input.setRedevanceAnnuelle(REDEVANCE_ANNUELLE_NAVIRE);
        }
        if (input.getDeviseFrais() == null) {
            input.setDeviseFrais(DEVISE_PAR_DEFAUT);
        }
    }

    public DemandeNavireOutputDTO initAndSubmit(DemandeNavireInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(NavireErrors.ACL_CLASS_NOT_FOUND,
                    NavireErrors.CLASS, NavireErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);

        input.setCreatedDate(ZonedDateTime.now());
        input.setSendedDate(ZonedDateTime.now());
        arreterFraisDossier(input);

        DemandeNavire entity = toEntityOrLoad(input);
        entity = demandeNavireRepository.save(entity);

        DemandeNavireOutputDTO output = demandeNavireOutputMapper.toDto(entity);
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

        return demandeNavireOutputMapper.toDto(entity);
    }

    /**
     * Transitions suivantes : le dossier existe et son instance de process tourne.
     */
    public DemandeNavireOutputDTO submit(DemandeNavireInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(NavireErrors.ACL_CLASS_NOT_FOUND,
                    NavireErrors.CLASS, NavireErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);
        if (input.getId() == null) {
            throw new BadRequestAlertException(NavireErrors.OBJECT_NOT_VALID,
                    NavireErrors.CLASS, NavireErrors.OBJECT_NOT_VALID);
        }

        DemandeNavire entity = demandeNavireRepository.findById(input.getId())
                .orElseThrow(() -> new BadRequestAlertException(NavireErrors.OBJECT_NOT_FOUND,
                        NavireErrors.CLASS, NavireErrors.OBJECT_NOT_FOUND));

        demandeNavireInputMapper.partialUpdate(entity, input);
        entity = demandeNavireRepository.save(entity);

        DemandeNavireOutputDTO output = demandeNavireOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        BpmJob bpmJob = workflowService._nextTask(entity.getWfProcessID(),
                input.getDecision(), input.getWfComment(), output, aclClass);

        entity = appliquerRetourMoteur(bpmJob, aclClass);

        saveWorkflowCommentaire(input.getCommentaire(), entity, aclClass);

        return demandeNavireOutputMapper.toDto(entity);
    }

    /**
     * Enregistrement sans franchir d'etape : le circuit n'est pas sollicite.
     */
    public DemandeNavireOutputDTO saveAsDraft(DemandeNavireInputDTO input, AclClass aclClass) {
        if (aclClass == null) {
            throw new BadRequestAlertException(NavireErrors.ACL_CLASS_NOT_FOUND,
                    NavireErrors.CLASS, NavireErrors.ACL_CLASS_NOT_FOUND);
        }

        DemandeNavire entity = toEntityOrLoad(input);
        entity = demandeNavireRepository.save(entity);

        DemandeNavireOutputDTO output = demandeNavireOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        // le brouillon n'est visible que de son auteur tant qu'il n'est pas soumis
        entity = persistAndApplySecurity(entity,
                Arrays.asList(currentUser.getEmployeSid()), new ArrayList<>(), aclClass);

        return demandeNavireOutputMapper.toDto(entity);
    }

    // ------------------------------------------------------------- internes

    private DemandeNavire toEntityOrLoad(DemandeNavireInputDTO input) {
        if (input.getId() != null) {
            Optional<DemandeNavire> existant = demandeNavireRepository.findById(input.getId());
            if (existant.isPresent()) {
                DemandeNavire entity = existant.get();
                demandeNavireInputMapper.partialUpdate(entity, input);
                return entity;
            }
        }
        return demandeNavireInputMapper.toEntity(input);
    }

    /**
     * Reporte sur l'entite ce que le moteur a decide, puis rejoue la securite.
     */
    private DemandeNavire appliquerRetourMoteur(BpmJob bpmJob, AclClass aclClass) throws Exception {
        DemandeNavireOutputDTO output = (DemandeNavireOutputDTO) bpmJob.getDataObject();

        // On repart de l'instance GEREE, jamais d'une entite reconstruite : le DTO
        // porterait une collection neuve, et le merge ferait lever Hibernate sur
        // orphanRemoval. Il ne porte pas non plus l'aclObjectIdentity.
        DemandeNavire entity = output.getId() != null
                ? demandeNavireRepository.findById(output.getId()).orElse(null)
                : null;
        if (entity == null) {
            entity = demandeNavireOutputMapper.toEntity(output);
        } else {
            demandeNavireOutputMapper.partialUpdate(entity, output);
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
    private DemandeNavire persistAndApplySecurity(DemandeNavire entity,
                                                        List<String> authors, List<String> readers,
                                                        AclClass aclClass) {
        if (entity.getId() != null) {
            demandeNavireRepository.findById(entity.getId()).ifPresent(recent -> {
                if (entity.getAclObjectIdentity() == null) {
                    entity.setAclObjectIdentity(recent.getAclObjectIdentity());
                }
            });
        }

        rattacherEnfants(entity);

        DemandeNavire persiste = demandeNavireRepository.save(entity);

        try {
            if (authors != null && readers != null) {
                kernelInterface.applySecurity(aclClass.getClasse(), persiste.getId(), authors, readers,
                        new ArrayList<>(), null, null, persiste.getAclObjectIdentity() == null, false);
            }
            if (persiste.getAclObjectIdentity() == null) {
                persiste = setAclObjectIdentity(persiste, aclClass);
                persiste = demandeNavireRepository.save(persiste);
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
    private void ouvrirRapportTechnique(DemandeNavire entity) {
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
        rapport.setDemandeNavire(entity);
        entity.setRapportTechnique(rapport);
    }

    private void rattacherEnfants(DemandeNavire entity) {
        if (entity.getClient() != null) {
            entity.getClient().setDemandeNavire(entity);
        }
        if (entity.getApplicant() != null) {

            entity.getApplicant().setDemandeNavire(entity);

        }
        if (entity.getAutorisationsAnterieures() != null) {
            entity.getAutorisationsAnterieures().forEach(a -> a.setDemandeNavire(entity));
        }
        if (entity.getEquipements() != null) {
            entity.getEquipements().forEach(e -> e.setDemandeNavire(entity));
        }
        if (entity.getRapportTechnique() != null) {
            entity.getRapportTechnique().setDemandeNavire(entity);
        }
        if (entity.getAttestations() != null) {
            entity.getAttestations().forEach(a -> a.setDemandeNavire(entity));
        }
    }

    public DemandeNavire setAclObjectIdentity(DemandeNavire entity, AclClass aclClass) {
        Integer aclObjectIdentityID = kernelInterface.findACLObjectIdentity(aclClass.getId(), entity.getId());
        if (aclObjectIdentityID == null) {
            return entity;
        }
        AclObjectIdentity aclObjectIdentity = new AclObjectIdentity();
        aclObjectIdentity.setId(aclObjectIdentityID.longValue());
        entity.setAclObjectIdentity(aclObjectIdentity);
        return entity;
    }

    private void publierPiecesJointes(DemandeNavireInputDTO input, DemandeNavireOutputDTO output,
                                      DemandeNavire entity, AclClass aclClass) {
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

    private void saveWorkflowCommentaire(String texte, DemandeNavire entity, AclClass aclClass) {
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
