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
import picosoft.biz.arcep.controller.errors.IspcErrors;
import picosoft.biz.arcep.domain.ispc.DemandeIspc;
import picosoft.biz.arcep.domain.shared.Commentaire;
import picosoft.biz.arcep.domain.shared.RapportTechnique;
import picosoft.biz.arcep.service.dto.RapportTechniqueDTO;
import picosoft.biz.arcep.repository.CommentaireRepository;
import picosoft.biz.arcep.repository.DemandeIspcRepository;
import picosoft.biz.arcep.service.criteria.DemandeIspcCriteria;
import picosoft.biz.arcep.domain.ispc.enumeration.*;
import picosoft.biz.arcep.service.dto.DemandeIspcDTO;
import picosoft.biz.arcep.service.dto.ApplicantDTO;
import picosoft.biz.arcep.domain.ispc.enumeration.FonctionSemaphore;
import picosoft.biz.arcep.service.dto.FonctionPointSemaphoreDTO;
import picosoft.biz.arcep.service.dto.DemandeIspcInputDTO;
import picosoft.biz.arcep.service.dto.DemandeIspcOutputDTO;
import picosoft.biz.arcep.service.mapper.DemandeIspcInputMapper;
import picosoft.biz.arcep.service.mapper.DemandeIspcMapper;
import picosoft.biz.arcep.service.mapper.DemandeIspcOutputMapper;

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
public class DemandeIspcService {

    private final Logger log = LoggerFactory.getLogger(DemandeIspcService.class);

    private final DemandeIspcRepository demandeIspcRepository;
    private final DemandeIspcQueryService demandeIspcQueryService;
    private final DemandeIspcMapper demandeIspcMapper;
    private final DemandeIspcInputMapper demandeIspcInputMapper;
    private final DemandeIspcOutputMapper demandeIspcOutputMapper;
    private final CommentaireRepository commentaireRepository;
    private final KernelInterface kernelInterface;
    private final WorkflowService workflowService;
    private final CurrentUser currentUser;

    public DemandeIspcService(DemandeIspcRepository demandeIspcRepository,
                                      DemandeIspcQueryService demandeIspcQueryService,
                                      DemandeIspcMapper demandeIspcMapper,
                                      DemandeIspcInputMapper demandeIspcInputMapper,
                                      DemandeIspcOutputMapper demandeIspcOutputMapper,
                                      CommentaireRepository commentaireRepository,
                                      KernelInterface kernelInterface,
                                      WorkflowService workflowService,
                                      CurrentUser currentUser) {
        this.demandeIspcRepository = demandeIspcRepository;
        this.demandeIspcQueryService = demandeIspcQueryService;
        this.demandeIspcMapper = demandeIspcMapper;
        this.demandeIspcInputMapper = demandeIspcInputMapper;
        this.demandeIspcOutputMapper = demandeIspcOutputMapper;
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
    public DemandeIspcDTO save(DemandeIspcDTO dto) {
        if (dto.getId() != null) {
            Optional<DemandeIspc> existant = demandeIspcRepository.findById(dto.getId());
            if (existant.isPresent()) {
                DemandeIspc gere = existant.get();
                demandeIspcMapper.partialUpdate(gere, dto);
                rattacherEnfants(gere);
                return demandeIspcMapper.toDto(demandeIspcRepository.save(gere));
            }
        }
        DemandeIspc entity = demandeIspcMapper.toEntity(dto);
        rattacherEnfants(entity);
        return demandeIspcMapper.toDto(demandeIspcRepository.save(entity));
    }

    public DemandeIspcDTO update(Long id, DemandeIspcDTO dto) {
        DemandeIspc entity = demandeIspcRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(IspcErrors.OBJECT_NOT_FOUND,
                        IspcErrors.CLASS, IspcErrors.OBJECT_NOT_FOUND));
        demandeIspcMapper.partialUpdate(entity, dto);
        rattacherEnfants(entity);
        return demandeIspcMapper.toDto(demandeIspcRepository.save(entity));
    }

    public Page<DemandeIspcDTO> findAll(DemandeIspcCriteria criteria, Pageable pageable, Integer size) {
        return demandeIspcQueryService.findByCriteria(criteria, pageable, size);
    }

    public Optional<DemandeIspcDTO> findOne(Long id) {
        return demandeIspcRepository.findById(id).map(demandeIspcMapper::toDto);
    }

    public DemandeIspcOutputDTO byId(Long id) {
        DemandeIspc entity = demandeIspcRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(IspcErrors.OBJECT_NOT_FOUND,
                        IspcErrors.CLASS, IspcErrors.OBJECT_NOT_FOUND));
        return demandeIspcOutputMapper.toDto(entity);
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
        DemandeIspc entity = demandeIspcRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(IspcErrors.OBJECT_NOT_FOUND,
                        IspcErrors.CLASS, IspcErrors.OBJECT_NOT_FOUND));

        if (entity.getWfProcessID() != null) {
            throw new BadRequestAlertException(IspcErrors.OBJECT_ENGAGED,
                    IspcErrors.CLASS, IspcErrors.OBJECT_ENGAGED);
        }
        // Une autorisation delivree est un document officiel : le dossier qui la
        // porte ne s'efface pas, meme si son circuit est termine.
        if (entity.getAttestations() != null && !entity.getAttestations().isEmpty()) {
            throw new BadRequestAlertException(IspcErrors.OBJECT_ENGAGED,
                    IspcErrors.CLASS, IspcErrors.OBJECT_ENGAGED);
        }
        // Pas de garde par site : contrairement aux stations d'une demande
        // d'implantation, les sites d'un reseau ne portent pas de circuit propre.
        // Le reseau est autorise d'un bloc, et c'est le circuit du dossier -- teste
        // juste au-dessus -- qui protege la suppression.
        demandeIspcRepository.delete(entity);
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
    public void exigerPourSoumission(DemandeIspcInputDTO input) {
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
        //
        // Il vit desormais sur `applicant`, la table partagee, et se reconnait a
        // son ROLE. Le nom et les prenoms s'y fondent dans `applicantName`,
        // comme chez ASI : les tables partagees n'ont pas de champ de prenom.
        ApplicantDTO r = Acteurs.parRole(input.getApplicants(), Acteurs.CONTACT);
        if (r == null) {
            manques.add("la personne a contacter");
        } else {
            if (estVide(r.getApplicantName())) { manques.add("le contact : l'identite"); }
            if (estVide(r.getQualification())) { manques.add("le contact : la fonction"); }
            if (estVide(r.getEmail()))         { manques.add("le contact : l'adresse electronique"); }
            if (estVide(r.getAddress()))       { manques.add("le contact : l'adresse permanente"); }
        }

        // ----- la nature de la demande, rubrique 3
        if (input.getNatureDemande() == null) {
            manques.add("la nature de la demande");
        }

        // ----- les fonctions dans le reseau, rubrique 6
        //
        // Au moins une cochee : le formulaire dit « plus d'une fonction peut
        // s'appliquer », ce qui suppose qu'au moins une s'applique. Un point
        // semaphore sans fonction declaree ne decrit rien.
        if (input.getFonctions() == null || input.getFonctions().isEmpty()) {
            manques.add("au moins une fonction dans le reseau (rubrique 6)");
        } else {
            for (int i = 0; i < input.getFonctions().size(); i++) {
                FonctionPointSemaphoreDTO fn = input.getFonctions().get(i);
                if (fn == null || fn.getFonction() == null) {
                    manques.add("la fonction n" + (i + 1) + " : la nature");
                } else if (fn.getFonction() == FonctionSemaphore.AUTRE
                        && estVide(fn.getPrecisionAutre())) {
                    // « Autre » cochee sans texte ne dit rien a l'instructeur.
                    manques.add("la fonction n" + (i + 1) + " : preciser la nature");
                }
            }
        }

        // ----- le point semaphore local, rubriques 7 a 9
        if (estVide(input.getFabricantTypeSemaphore())) {
            manques.add("le fabricant et le type du point semaphore");
        }
        if (estVide(input.getAdressePhysiqueSemaphore())) {
            manques.add("l'adresse physique du point semaphore");
        }
        if (input.getDateMiseEnService() == null) {
            manques.add("la date de mise en service du point semaphore");
        }

        // ----- le point semaphore distant, rubriques 11 et 12
        //
        // Le CODE ISPC du distant (rubrique 13) n'est PAS exige : le
        // formulaire porte « s'il est connu ». Le reclamer refuserait un
        // dossier que le formulaire accepte.
        if (estVide(input.getSemaphoreDistantNomAdresse())) {
            manques.add("le nom et l'adresse du point semaphore distant");
        }
        if (estVide(input.getSemaphoreDistantEmplacement())) {
            manques.add("l'emplacement du point semaphore distant");
        }

        if (!manques.isEmpty()) {
            throw new BadRequestAlertException(
                    "Le dossier ne peut pas etre soumis, il manque : " + String.join(", ", manques),
                    IspcErrors.CLASS, IspcErrors.OBJECT_NOT_VALID);
        }
    }

    /** Vrai pour une chaine nulle, vide ou faite d'espaces. */
    private static boolean estVide(String v) {
        return v == null || v.trim().isEmpty();
    }

    private void exigerPiecesObligatoires(DemandeIspcInputDTO input, AclClass aclClass) {
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
                    IspcErrors.CLASS, IspcErrors.PIECES_MANQUANTES);
        }
    }

    /**
     * Les frais de dossier, rubrique 9 du formulaire : un FORFAIT de
     * 200 000 FCFA par ispc.
     *
     * Pas de grille par service comme le reseau -- l'annexe de ce formulaire
     * ne connait qu'un montant. Il est arrete au depot et conserve sur le
     * dossier : un bareme qui change ne doit pas reecrire un dossier deja
     * instruit.
     */
    private static final String DEVISE_PAR_DEFAUT = "XAF";

    /**
     * Ce formulaire N'A PAS D'ANNEXE TARIFAIRE.
     *
     * Contrairement a l'aeronef (200 000 FCFA) ou au navire (200 000 plus une
     * redevance annuelle), la fiche ISPC ne porte aucun montant. On ne pose
     * donc AUCUN frais : inventer un forfait reviendrait a facturer une somme
     * que rien n'autorise, et mettre zero laisserait croire que la gratuite a
     * ete decidee alors qu'elle n'a jamais ete ecrite.
     *
     * La colonne `fraisDossier` reste, heritee de la plomberie commune. Elle
     * restera vide tant que le metier n'aura pas fourni de bareme -- et c'est
     * exactement ce qu'un montant absent doit donner a lire.
     */
    void arreterFraisDossier(DemandeIspcInputDTO input) {
        if (input.getDeviseFrais() == null) {
            input.setDeviseFrais(DEVISE_PAR_DEFAUT);
        }
    }

    public DemandeIspcOutputDTO initAndSubmit(DemandeIspcInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(IspcErrors.ACL_CLASS_NOT_FOUND,
                    IspcErrors.CLASS, IspcErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);

        input.setCreatedDate(ZonedDateTime.now());
        input.setSendedDate(ZonedDateTime.now());
        arreterFraisDossier(input);

        DemandeIspc entity = toEntityOrLoad(input);
        entity = demandeIspcRepository.save(entity);

        DemandeIspcOutputDTO output = demandeIspcOutputMapper.toDto(entity);
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

        return demandeIspcOutputMapper.toDto(entity);
    }

    /**
     * Transitions suivantes : le dossier existe et son instance de process tourne.
     */
    public DemandeIspcOutputDTO submit(DemandeIspcInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(IspcErrors.ACL_CLASS_NOT_FOUND,
                    IspcErrors.CLASS, IspcErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);
        if (input.getId() == null) {
            throw new BadRequestAlertException(IspcErrors.OBJECT_NOT_VALID,
                    IspcErrors.CLASS, IspcErrors.OBJECT_NOT_VALID);
        }

        DemandeIspc entity = demandeIspcRepository.findById(input.getId())
                .orElseThrow(() -> new BadRequestAlertException(IspcErrors.OBJECT_NOT_FOUND,
                        IspcErrors.CLASS, IspcErrors.OBJECT_NOT_FOUND));

        demandeIspcInputMapper.partialUpdate(entity, input);
        entity = demandeIspcRepository.save(entity);

        DemandeIspcOutputDTO output = demandeIspcOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        BpmJob bpmJob = workflowService._nextTask(entity.getWfProcessID(),
                input.getDecision(), input.getWfComment(), output, aclClass);

        entity = appliquerRetourMoteur(bpmJob, aclClass);

        saveWorkflowCommentaire(input.getCommentaire(), entity, aclClass);

        return demandeIspcOutputMapper.toDto(entity);
    }

    /**
     * Enregistrement sans franchir d'etape : le circuit n'est pas sollicite.
     */
    public DemandeIspcOutputDTO saveAsDraft(DemandeIspcInputDTO input, AclClass aclClass) {
        if (aclClass == null) {
            throw new BadRequestAlertException(IspcErrors.ACL_CLASS_NOT_FOUND,
                    IspcErrors.CLASS, IspcErrors.ACL_CLASS_NOT_FOUND);
        }

        DemandeIspc entity = toEntityOrLoad(input);
        entity = demandeIspcRepository.save(entity);

        DemandeIspcOutputDTO output = demandeIspcOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        // le brouillon n'est visible que de son auteur tant qu'il n'est pas soumis
        entity = persistAndApplySecurity(entity,
                Arrays.asList(currentUser.getEmployeSid()), new ArrayList<>(), aclClass);

        return demandeIspcOutputMapper.toDto(entity);
    }

    // ------------------------------------------------------------- internes

    private DemandeIspc toEntityOrLoad(DemandeIspcInputDTO input) {
        if (input.getId() != null) {
            Optional<DemandeIspc> existant = demandeIspcRepository.findById(input.getId());
            if (existant.isPresent()) {
                DemandeIspc entity = existant.get();
                demandeIspcInputMapper.partialUpdate(entity, input);
                return entity;
            }
        }
        return demandeIspcInputMapper.toEntity(input);
    }

    /**
     * Reporte sur l'entite ce que le moteur a decide, puis rejoue la securite.
     */
    private DemandeIspc appliquerRetourMoteur(BpmJob bpmJob, AclClass aclClass) throws Exception {
        DemandeIspcOutputDTO output = (DemandeIspcOutputDTO) bpmJob.getDataObject();

        // On repart de l'instance GEREE, jamais d'une entite reconstruite : le DTO
        // porterait une collection neuve, et le merge ferait lever Hibernate sur
        // orphanRemoval. Il ne porte pas non plus l'aclObjectIdentity.
        DemandeIspc entity = output.getId() != null
                ? demandeIspcRepository.findById(output.getId()).orElse(null)
                : null;
        if (entity == null) {
            entity = demandeIspcOutputMapper.toEntity(output);
        } else {
            demandeIspcOutputMapper.partialUpdate(entity, output);
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
    private DemandeIspc persistAndApplySecurity(DemandeIspc entity,
                                                        List<String> authors, List<String> readers,
                                                        AclClass aclClass) {
        if (entity.getId() != null) {
            demandeIspcRepository.findById(entity.getId()).ifPresent(recent -> {
                if (entity.getAclObjectIdentity() == null) {
                    entity.setAclObjectIdentity(recent.getAclObjectIdentity());
                }
            });
        }

        rattacherEnfants(entity);

        DemandeIspc persiste = demandeIspcRepository.save(entity);

        try {
            if (authors != null && readers != null) {
                kernelInterface.applySecurity(aclClass.getClasse(), persiste.getId(), authors, readers,
                        new ArrayList<>(), null, null, persiste.getAclObjectIdentity() == null, false);
            }
            if (persiste.getAclObjectIdentity() == null) {
                persiste = setAclObjectIdentity(persiste, aclClass);
                persiste = demandeIspcRepository.save(persiste);
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
    private void ouvrirRapportTechnique(DemandeIspc entity) {
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
        rapport.setDemandeIspc(entity);
        entity.setRapportTechnique(rapport);
    }

    private void rattacherEnfants(DemandeIspc entity) {
        if (entity.getClient() != null) {
            entity.getClient().setDemandeIspc(entity);
        }
        if (entity.getApplicants() != null) {
            entity.getApplicants().forEach(a -> a.setDemandeIspc(entity));
        }
        if (entity.getFonctions() != null) {
            entity.getFonctions().forEach(fn -> fn.setDemandeIspc(entity));
        }
        if (entity.getRapportTechnique() != null) {
            entity.getRapportTechnique().setDemandeIspc(entity);
        }
        if (entity.getAttestations() != null) {
            entity.getAttestations().forEach(a -> a.setDemandeIspc(entity));
        }
    }

    public DemandeIspc setAclObjectIdentity(DemandeIspc entity, AclClass aclClass) {
        Integer aclObjectIdentityID = kernelInterface.findACLObjectIdentity(aclClass.getId(), entity.getId());
        if (aclObjectIdentityID == null) {
            return entity;
        }
        AclObjectIdentity aclObjectIdentity = new AclObjectIdentity();
        aclObjectIdentity.setId(aclObjectIdentityID.longValue());
        entity.setAclObjectIdentity(aclObjectIdentity);
        return entity;
    }

    private void publierPiecesJointes(DemandeIspcInputDTO input, DemandeIspcOutputDTO output,
                                      DemandeIspc entity, AclClass aclClass) {
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

    private void saveWorkflowCommentaire(String texte, DemandeIspc entity, AclClass aclClass) {
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
