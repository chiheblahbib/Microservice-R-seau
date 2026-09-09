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
import picosoft.biz.arcep.controller.errors.InstallateurErrors;
import picosoft.biz.arcep.domain.installateur.DemandeInstallateur;
import picosoft.biz.arcep.domain.shared.Commentaire;
import picosoft.biz.arcep.domain.shared.RapportTechnique;
import picosoft.biz.arcep.service.dto.RapportTechniqueDTO;
import picosoft.biz.arcep.repository.CommentaireRepository;
import picosoft.biz.arcep.repository.DemandeInstallateurRepository;
import picosoft.biz.arcep.service.criteria.DemandeInstallateurCriteria;
import picosoft.biz.arcep.domain.installateur.enumeration.*;
import picosoft.biz.arcep.service.dto.DemandeInstallateurDTO;
import picosoft.biz.arcep.service.dto.PersonneInstallateurDTO;
import picosoft.biz.arcep.domain.installateur.enumeration.EtendueActivite;
import picosoft.biz.arcep.domain.installateur.enumeration.TypeAutorisation;
import picosoft.biz.arcep.service.dto.TechnicienSpecialisteDTO;
import picosoft.biz.arcep.service.dto.DemandeInstallateurInputDTO;
import picosoft.biz.arcep.service.dto.DemandeInstallateurOutputDTO;
import picosoft.biz.arcep.service.mapper.DemandeInstallateurInputMapper;
import picosoft.biz.arcep.service.mapper.DemandeInstallateurMapper;
import picosoft.biz.arcep.service.mapper.DemandeInstallateurOutputMapper;

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
public class DemandeInstallateurService {

    private final Logger log = LoggerFactory.getLogger(DemandeInstallateurService.class);

    private final DemandeInstallateurRepository demandeInstallateurRepository;
    private final DemandeInstallateurQueryService demandeInstallateurQueryService;
    private final DemandeInstallateurMapper demandeInstallateurMapper;
    private final DemandeInstallateurInputMapper demandeInstallateurInputMapper;
    private final DemandeInstallateurOutputMapper demandeInstallateurOutputMapper;
    private final CommentaireRepository commentaireRepository;
    private final KernelInterface kernelInterface;
    private final WorkflowService workflowService;
    private final CurrentUser currentUser;

    public DemandeInstallateurService(DemandeInstallateurRepository demandeInstallateurRepository,
                                      DemandeInstallateurQueryService demandeInstallateurQueryService,
                                      DemandeInstallateurMapper demandeInstallateurMapper,
                                      DemandeInstallateurInputMapper demandeInstallateurInputMapper,
                                      DemandeInstallateurOutputMapper demandeInstallateurOutputMapper,
                                      CommentaireRepository commentaireRepository,
                                      KernelInterface kernelInterface,
                                      WorkflowService workflowService,
                                      CurrentUser currentUser) {
        this.demandeInstallateurRepository = demandeInstallateurRepository;
        this.demandeInstallateurQueryService = demandeInstallateurQueryService;
        this.demandeInstallateurMapper = demandeInstallateurMapper;
        this.demandeInstallateurInputMapper = demandeInstallateurInputMapper;
        this.demandeInstallateurOutputMapper = demandeInstallateurOutputMapper;
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
    public DemandeInstallateurDTO save(DemandeInstallateurDTO dto) {
        if (dto.getId() != null) {
            Optional<DemandeInstallateur> existant = demandeInstallateurRepository.findById(dto.getId());
            if (existant.isPresent()) {
                DemandeInstallateur gere = existant.get();
                demandeInstallateurMapper.partialUpdate(gere, dto);
                rattacherEnfants(gere);
                return demandeInstallateurMapper.toDto(demandeInstallateurRepository.save(gere));
            }
        }
        DemandeInstallateur entity = demandeInstallateurMapper.toEntity(dto);
        rattacherEnfants(entity);
        return demandeInstallateurMapper.toDto(demandeInstallateurRepository.save(entity));
    }

    public DemandeInstallateurDTO update(Long id, DemandeInstallateurDTO dto) {
        DemandeInstallateur entity = demandeInstallateurRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(InstallateurErrors.OBJECT_NOT_FOUND,
                        InstallateurErrors.CLASS, InstallateurErrors.OBJECT_NOT_FOUND));
        demandeInstallateurMapper.partialUpdate(entity, dto);
        rattacherEnfants(entity);
        return demandeInstallateurMapper.toDto(demandeInstallateurRepository.save(entity));
    }

    public Page<DemandeInstallateurDTO> findAll(DemandeInstallateurCriteria criteria, Pageable pageable, Integer size) {
        return demandeInstallateurQueryService.findByCriteria(criteria, pageable, size);
    }

    public Optional<DemandeInstallateurDTO> findOne(Long id) {
        return demandeInstallateurRepository.findById(id).map(demandeInstallateurMapper::toDto);
    }

    public DemandeInstallateurOutputDTO byId(Long id) {
        DemandeInstallateur entity = demandeInstallateurRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(InstallateurErrors.OBJECT_NOT_FOUND,
                        InstallateurErrors.CLASS, InstallateurErrors.OBJECT_NOT_FOUND));
        return demandeInstallateurOutputMapper.toDto(entity);
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
        DemandeInstallateur entity = demandeInstallateurRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(InstallateurErrors.OBJECT_NOT_FOUND,
                        InstallateurErrors.CLASS, InstallateurErrors.OBJECT_NOT_FOUND));

        if (entity.getWfProcessID() != null) {
            throw new BadRequestAlertException(InstallateurErrors.OBJECT_ENGAGED,
                    InstallateurErrors.CLASS, InstallateurErrors.OBJECT_ENGAGED);
        }
        // Une autorisation delivree est un document officiel : le dossier qui la
        // porte ne s'efface pas, meme si son circuit est termine.
        if (entity.getAttestations() != null && !entity.getAttestations().isEmpty()) {
            throw new BadRequestAlertException(InstallateurErrors.OBJECT_ENGAGED,
                    InstallateurErrors.CLASS, InstallateurErrors.OBJECT_ENGAGED);
        }
        // Pas de garde par site : contrairement aux stations d'une demande
        // d'implantation, les sites d'un reseau ne portent pas de circuit propre.
        // Le reseau est autorise d'un bloc, et c'est le circuit du dossier -- teste
        // juste au-dessus -- qui protege la suppression.
        demandeInstallateurRepository.delete(entity);
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
    public void exigerPourSoumission(DemandeInstallateurInputDTO input) {
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
        // ----- le representant legal
        //
        // Le REQUERANT n'est pas exige : le formulaire porte « remplir cette
        // partie si l'identite du requerant differe de celle du
        // representant ». Le reclamer refuserait le cas le plus courant.
        PersonneInstallateurDTO r = (input.getPersonnes() == null) ? null
                : input.getPersonnes().stream()
                       .filter(p -> p != null && "REPRESENTANT".equals(p.getRole()))
                       .findFirst().orElse(null);
        if (r == null) {
            manques.add("le representant legal de l'entreprise");
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

        // ----- les qualites demandees, rubrique 1
        //
        // Au moins une : sans elle, on ne sait ni ce qui est demande ni quel
        // tarif appliquer -- arreterFraisDossier ne pose alors rien.
        if (input.getQualites() == null || input.getQualites().isEmpty()) {
            manques.add("au moins une qualite demandee (installateur, distributeur"
                    + " ou sous-distributeur)");
        }

        // ----- l'etendue de l'activite, rubrique 2
        if (input.getEtendues() == null || input.getEtendues().isEmpty()) {
            manques.add("au moins une etendue d'activite (rubrique 2)");
        } else if (input.getEtendues().contains(EtendueActivite.AUTRE)
                && estVide(input.getEtendueAutrePrecision())) {
            manques.add("preciser l'autre etendue d'activite");
        }

        // ----- le responsable de l'activite, rubrique 4
        //
        // C'est lui qui repond techniquement de l'activite autorisee : un
        // dossier qui ne le nomme pas ne dit pas qui en repond.
        boolean responsable = input.getPersonnes() != null
                && input.getPersonnes().stream()
                        .anyMatch(p -> p != null && "RESPONSABLE".equals(p.getRole())
                                && !estVide(p.getNom()));
        if (!responsable) {
            manques.add("le responsable de l'activite sollicitee (rubrique 4)");
        }

        // ----- les techniciens specialistes, rubrique 5
        //
        // Au moins un : c'est ce qui fonde la competence technique de
        // l'entreprise, et l'instruction juge dessus.
        if (input.getTechniciens() == null || input.getTechniciens().isEmpty()) {
            manques.add("au moins un technicien specialiste (rubrique 5)");
        } else {
            for (int i = 0; i < input.getTechniciens().size(); i++) {
                TechnicienSpecialisteDTO t = input.getTechniciens().get(i);
                if (t == null || estVide(t.getNom())) {
                    manques.add("le technicien n" + (i + 1) + " : le nom");
                }
                if (t != null && estVide(t.getQualification())) {
                    manques.add("le technicien n" + (i + 1) + " : la qualification");
                }
            }
        }

        // La ZONE GEOGRAPHIQUE (rubrique 8) n'est pas exigee : un dossier peut
        // legitimement n'avoir pas encore arrete ses implantations, et le
        // formulaire n'y met pas d'asterisque.

        if (!manques.isEmpty()) {
            throw new BadRequestAlertException(
                    "Le dossier ne peut pas etre soumis, il manque : " + String.join(", ", manques),
                    InstallateurErrors.CLASS, InstallateurErrors.OBJECT_NOT_VALID);
        }
    }

    /** Vrai pour une chaine nulle, vide ou faite d'espaces. */
    private static boolean estVide(String v) {
        return v == null || v.trim().isEmpty();
    }

    private void exigerPiecesObligatoires(DemandeInstallateurInputDTO input, AclClass aclClass) {
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
                    InstallateurErrors.CLASS, InstallateurErrors.PIECES_MANQUANTES);
        }
    }

    /**
     * Les frais de dossier, rubrique 9 du formulaire : un FORFAIT de
     * 200 000 FCFA par installateur.
     *
     * Pas de grille par service comme le reseau -- l'annexe de ce formulaire
     * ne connait qu'un montant. Il est arrete au depot et conserve sur le
     * dossier : un bareme qui change ne doit pas reecrire un dossier deja
     * instruit.
     */
    /**
     * L'annexe tarifaire, rubrique 11 :
     *
     *     Installateur et/ou Distributeur   frais de dossier      100 000
     *     Installateur                      redevance annuelle  2 000 000
     *     Distributeur                      redevance annuelle  1 000 000
     *     Sous-Distributeur                 frais de dossier       50 000
     *     Sous-Distributeur                 redevance annuelle    150 000
     *
     * Les frais sont payables au depot, la redevance apres validation.
     */
    private static final BigDecimal FRAIS_INSTALLATEUR_DISTRIBUTEUR = new BigDecimal("100000");
    private static final BigDecimal FRAIS_SOUS_DISTRIBUTEUR = new BigDecimal("50000");
    private static final BigDecimal REDEVANCE_INSTALLATEUR = new BigDecimal("2000000");
    private static final BigDecimal REDEVANCE_DISTRIBUTEUR = new BigDecimal("1000000");
    private static final BigDecimal REDEVANCE_SOUS_DISTRIBUTEUR = new BigDecimal("150000");
    private static final String DEVISE_PAR_DEFAUT = "XAF";

    /**
     * Arrete les deux montants, d'apres les qualites demandees.
     *
     * CE QUE L'ANNEXE NE DIT PAS : elle donne une redevance PAR qualite, et
     * autorise par ailleurs de cumuler installateur et distributeur -- sans
     * dire ce qu'on doit alors au titre de l'annuel. Les additionner (3
     * millions) et retenir la plus forte (2 millions) sont deux lectures
     * defendables du meme texte.
     *
     * On retient LA PLUS FORTE, et on le dit ici pour que ce soit visible :
     * c'est le choix le moins couteux a corriger si le metier tranche
     * autrement -- une ligne a changer, contre des dossiers a recalculer si on
     * avait surfacture. La regle EST une hypothese, pas une lecture certaine.
     */
    void arreterFraisDossier(DemandeInstallateurInputDTO input) {
        java.util.Set<TypeAutorisation> qualites = new java.util.HashSet<>();
        if (input.getQualites() != null) {
            input.getQualites().stream()
                 .filter(q -> q != null && q.getQualite() != null)
                 .forEach(q -> qualites.add(q.getQualite()));
        }

        if (input.getFraisDossier() == null && !qualites.isEmpty()) {
            // 50 000 pour le SEUL sous-distributeur ; des qu'une autre qualite
            // est demandee, la ligne « Installateur et/ou Distributeur »
            // s'applique et le tarif passe a 100 000.
            boolean seulementSousDistributeur =
                    qualites.size() == 1 && qualites.contains(TypeAutorisation.SOUS_DISTRIBUTEUR);
            input.setFraisDossier(seulementSousDistributeur
                    ? FRAIS_SOUS_DISTRIBUTEUR : FRAIS_INSTALLATEUR_DISTRIBUTEUR);
        }

        if (input.getRedevanceAnnuelle() == null && !qualites.isEmpty()) {
            BigDecimal plusForte = BigDecimal.ZERO;
            if (qualites.contains(TypeAutorisation.INSTALLATEUR)) {
                plusForte = plusForte.max(REDEVANCE_INSTALLATEUR);
            }
            if (qualites.contains(TypeAutorisation.DISTRIBUTEUR)) {
                plusForte = plusForte.max(REDEVANCE_DISTRIBUTEUR);
            }
            if (qualites.contains(TypeAutorisation.SOUS_DISTRIBUTEUR)) {
                plusForte = plusForte.max(REDEVANCE_SOUS_DISTRIBUTEUR);
            }
            input.setRedevanceAnnuelle(plusForte);
        }

        if (input.getDeviseFrais() == null) {
            input.setDeviseFrais(DEVISE_PAR_DEFAUT);
        }
    }

    public DemandeInstallateurOutputDTO initAndSubmit(DemandeInstallateurInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(InstallateurErrors.ACL_CLASS_NOT_FOUND,
                    InstallateurErrors.CLASS, InstallateurErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);

        input.setCreatedDate(ZonedDateTime.now());
        input.setSendedDate(ZonedDateTime.now());
        arreterFraisDossier(input);

        DemandeInstallateur entity = toEntityOrLoad(input);
        entity = demandeInstallateurRepository.save(entity);

        DemandeInstallateurOutputDTO output = demandeInstallateurOutputMapper.toDto(entity);
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

        return demandeInstallateurOutputMapper.toDto(entity);
    }

    /**
     * Transitions suivantes : le dossier existe et son instance de process tourne.
     */
    public DemandeInstallateurOutputDTO submit(DemandeInstallateurInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(InstallateurErrors.ACL_CLASS_NOT_FOUND,
                    InstallateurErrors.CLASS, InstallateurErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);
        if (input.getId() == null) {
            throw new BadRequestAlertException(InstallateurErrors.OBJECT_NOT_VALID,
                    InstallateurErrors.CLASS, InstallateurErrors.OBJECT_NOT_VALID);
        }

        DemandeInstallateur entity = demandeInstallateurRepository.findById(input.getId())
                .orElseThrow(() -> new BadRequestAlertException(InstallateurErrors.OBJECT_NOT_FOUND,
                        InstallateurErrors.CLASS, InstallateurErrors.OBJECT_NOT_FOUND));

        demandeInstallateurInputMapper.partialUpdate(entity, input);
        entity = demandeInstallateurRepository.save(entity);

        DemandeInstallateurOutputDTO output = demandeInstallateurOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        BpmJob bpmJob = workflowService._nextTask(entity.getWfProcessID(),
                input.getDecision(), input.getWfComment(), output, aclClass);

        entity = appliquerRetourMoteur(bpmJob, aclClass);

        saveWorkflowCommentaire(input.getCommentaire(), entity, aclClass);

        return demandeInstallateurOutputMapper.toDto(entity);
    }

    /**
     * Enregistrement sans franchir d'etape : le circuit n'est pas sollicite.
     */
    public DemandeInstallateurOutputDTO saveAsDraft(DemandeInstallateurInputDTO input, AclClass aclClass) {
        if (aclClass == null) {
            throw new BadRequestAlertException(InstallateurErrors.ACL_CLASS_NOT_FOUND,
                    InstallateurErrors.CLASS, InstallateurErrors.ACL_CLASS_NOT_FOUND);
        }

        DemandeInstallateur entity = toEntityOrLoad(input);
        entity = demandeInstallateurRepository.save(entity);

        DemandeInstallateurOutputDTO output = demandeInstallateurOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        // le brouillon n'est visible que de son auteur tant qu'il n'est pas soumis
        entity = persistAndApplySecurity(entity,
                Arrays.asList(currentUser.getEmployeSid()), new ArrayList<>(), aclClass);

        return demandeInstallateurOutputMapper.toDto(entity);
    }

    // ------------------------------------------------------------- internes

    private DemandeInstallateur toEntityOrLoad(DemandeInstallateurInputDTO input) {
        if (input.getId() != null) {
            Optional<DemandeInstallateur> existant = demandeInstallateurRepository.findById(input.getId());
            if (existant.isPresent()) {
                DemandeInstallateur entity = existant.get();
                demandeInstallateurInputMapper.partialUpdate(entity, input);
                return entity;
            }
        }
        return demandeInstallateurInputMapper.toEntity(input);
    }

    /**
     * Reporte sur l'entite ce que le moteur a decide, puis rejoue la securite.
     */
    private DemandeInstallateur appliquerRetourMoteur(BpmJob bpmJob, AclClass aclClass) throws Exception {
        DemandeInstallateurOutputDTO output = (DemandeInstallateurOutputDTO) bpmJob.getDataObject();

        // On repart de l'instance GEREE, jamais d'une entite reconstruite : le DTO
        // porterait une collection neuve, et le merge ferait lever Hibernate sur
        // orphanRemoval. Il ne porte pas non plus l'aclObjectIdentity.
        DemandeInstallateur entity = output.getId() != null
                ? demandeInstallateurRepository.findById(output.getId()).orElse(null)
                : null;
        if (entity == null) {
            entity = demandeInstallateurOutputMapper.toEntity(output);
        } else {
            demandeInstallateurOutputMapper.partialUpdate(entity, output);
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
    private DemandeInstallateur persistAndApplySecurity(DemandeInstallateur entity,
                                                        List<String> authors, List<String> readers,
                                                        AclClass aclClass) {
        if (entity.getId() != null) {
            demandeInstallateurRepository.findById(entity.getId()).ifPresent(recent -> {
                if (entity.getAclObjectIdentity() == null) {
                    entity.setAclObjectIdentity(recent.getAclObjectIdentity());
                }
            });
        }

        rattacherEnfants(entity);

        DemandeInstallateur persiste = demandeInstallateurRepository.save(entity);

        try {
            if (authors != null && readers != null) {
                kernelInterface.applySecurity(aclClass.getClasse(), persiste.getId(), authors, readers,
                        new ArrayList<>(), null, null, persiste.getAclObjectIdentity() == null, false);
            }
            if (persiste.getAclObjectIdentity() == null) {
                persiste = setAclObjectIdentity(persiste, aclClass);
                persiste = demandeInstallateurRepository.save(persiste);
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
    private void ouvrirRapportTechnique(DemandeInstallateur entity) {
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
        rapport.setDemandeInstallateur(entity);
        entity.setRapportTechnique(rapport);
    }

    private void rattacherEnfants(DemandeInstallateur entity) {
        if (entity.getClient() != null) {
            entity.getClient().setDemandeInstallateur(entity);
        }
        if (entity.getPersonnes() != null) {
            entity.getPersonnes().forEach(p -> p.setDemandeInstallateur(entity));
        }
        if (entity.getTechniciens() != null) {
            entity.getTechniciens().forEach(t -> t.setDemandeInstallateur(entity));
        }
        if (entity.getOutillages() != null) {
            entity.getOutillages().forEach(o -> o.setDemandeInstallateur(entity));
        }
        if (entity.getQualites() != null) {
            entity.getQualites().forEach(e -> e.setDemandeInstallateur(entity));
        }
        if (entity.getRapportTechnique() != null) {
            entity.getRapportTechnique().setDemandeInstallateur(entity);
        }
        if (entity.getAttestations() != null) {
            entity.getAttestations().forEach(a -> a.setDemandeInstallateur(entity));
        }
    }

    public DemandeInstallateur setAclObjectIdentity(DemandeInstallateur entity, AclClass aclClass) {
        Integer aclObjectIdentityID = kernelInterface.findACLObjectIdentity(aclClass.getId(), entity.getId());
        if (aclObjectIdentityID == null) {
            return entity;
        }
        AclObjectIdentity aclObjectIdentity = new AclObjectIdentity();
        aclObjectIdentity.setId(aclObjectIdentityID.longValue());
        entity.setAclObjectIdentity(aclObjectIdentity);
        return entity;
    }

    private void publierPiecesJointes(DemandeInstallateurInputDTO input, DemandeInstallateurOutputDTO output,
                                      DemandeInstallateur entity, AclClass aclClass) {
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

    private void saveWorkflowCommentaire(String texte, DemandeInstallateur entity, AclClass aclClass) {
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
