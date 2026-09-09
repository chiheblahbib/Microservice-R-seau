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
import picosoft.biz.arcep.controller.errors.PqErrors;
import picosoft.biz.arcep.domain.pq.DemandePq;
import picosoft.biz.arcep.domain.shared.Commentaire;
import picosoft.biz.arcep.domain.shared.RapportTechnique;
import picosoft.biz.arcep.service.dto.RapportTechniqueDTO;
import picosoft.biz.arcep.repository.CommentaireRepository;
import picosoft.biz.arcep.repository.DemandePqRepository;
import picosoft.biz.arcep.service.criteria.DemandePqCriteria;
import picosoft.biz.arcep.domain.pq.enumeration.*;
import picosoft.biz.arcep.service.dto.DemandePqDTO;
import picosoft.biz.arcep.service.dto.ApplicantDTO;
import picosoft.biz.arcep.domain.pq.enumeration.TypeOperationPq;
import picosoft.biz.arcep.service.dto.BlocNumerosDTO;
import picosoft.biz.arcep.service.dto.DemandePqInputDTO;
import picosoft.biz.arcep.service.dto.DemandePqOutputDTO;
import picosoft.biz.arcep.service.mapper.DemandePqInputMapper;
import picosoft.biz.arcep.service.mapper.DemandePqMapper;
import picosoft.biz.arcep.service.mapper.DemandePqOutputMapper;

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
public class DemandePqService {

    private final Logger log = LoggerFactory.getLogger(DemandePqService.class);

    private final DemandePqRepository demandePqRepository;
    private final DemandePqQueryService demandePqQueryService;
    private final DemandePqMapper demandePqMapper;
    private final DemandePqInputMapper demandePqInputMapper;
    private final DemandePqOutputMapper demandePqOutputMapper;
    private final CommentaireRepository commentaireRepository;
    private final KernelInterface kernelInterface;
    private final WorkflowService workflowService;
    private final CurrentUser currentUser;

    public DemandePqService(DemandePqRepository demandePqRepository,
                                      DemandePqQueryService demandePqQueryService,
                                      DemandePqMapper demandePqMapper,
                                      DemandePqInputMapper demandePqInputMapper,
                                      DemandePqOutputMapper demandePqOutputMapper,
                                      CommentaireRepository commentaireRepository,
                                      KernelInterface kernelInterface,
                                      WorkflowService workflowService,
                                      CurrentUser currentUser) {
        this.demandePqRepository = demandePqRepository;
        this.demandePqQueryService = demandePqQueryService;
        this.demandePqMapper = demandePqMapper;
        this.demandePqInputMapper = demandePqInputMapper;
        this.demandePqOutputMapper = demandePqOutputMapper;
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
    public DemandePqDTO save(DemandePqDTO dto) {
        if (dto.getId() != null) {
            Optional<DemandePq> existant = demandePqRepository.findById(dto.getId());
            if (existant.isPresent()) {
                DemandePq gere = existant.get();
                demandePqMapper.partialUpdate(gere, dto);
                rattacherEnfants(gere);
                return demandePqMapper.toDto(demandePqRepository.save(gere));
            }
        }
        DemandePq entity = demandePqMapper.toEntity(dto);
        rattacherEnfants(entity);
        return demandePqMapper.toDto(demandePqRepository.save(entity));
    }

    public DemandePqDTO update(Long id, DemandePqDTO dto) {
        DemandePq entity = demandePqRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(PqErrors.OBJECT_NOT_FOUND,
                        PqErrors.CLASS, PqErrors.OBJECT_NOT_FOUND));
        demandePqMapper.partialUpdate(entity, dto);
        rattacherEnfants(entity);
        return demandePqMapper.toDto(demandePqRepository.save(entity));
    }

    public Page<DemandePqDTO> findAll(DemandePqCriteria criteria, Pageable pageable, Integer size) {
        return demandePqQueryService.findByCriteria(criteria, pageable, size);
    }

    public Optional<DemandePqDTO> findOne(Long id) {
        return demandePqRepository.findById(id).map(demandePqMapper::toDto);
    }

    public DemandePqOutputDTO byId(Long id) {
        DemandePq entity = demandePqRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(PqErrors.OBJECT_NOT_FOUND,
                        PqErrors.CLASS, PqErrors.OBJECT_NOT_FOUND));
        return demandePqOutputMapper.toDto(entity);
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
        DemandePq entity = demandePqRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(PqErrors.OBJECT_NOT_FOUND,
                        PqErrors.CLASS, PqErrors.OBJECT_NOT_FOUND));

        if (entity.getWfProcessID() != null) {
            throw new BadRequestAlertException(PqErrors.OBJECT_ENGAGED,
                    PqErrors.CLASS, PqErrors.OBJECT_ENGAGED);
        }
        // Une autorisation delivree est un document officiel : le dossier qui la
        // porte ne s'efface pas, meme si son circuit est termine.
        if (entity.getAttestations() != null && !entity.getAttestations().isEmpty()) {
            throw new BadRequestAlertException(PqErrors.OBJECT_ENGAGED,
                    PqErrors.CLASS, PqErrors.OBJECT_ENGAGED);
        }
        // Pas de garde par site : contrairement aux stations d'une demande
        // d'implantation, les sites d'un reseau ne portent pas de circuit propre.
        // Le reseau est autorise d'un bloc, et c'est le circuit du dossier -- teste
        // juste au-dessus -- qui protege la suppression.
        demandePqRepository.delete(entity);
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
    public void exigerPourSoumission(DemandePqInputDTO input) {
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
        ApplicantDTO r = Acteurs.parRole(input.getApplicants(), Acteurs.REPRESENTANT);
        if (r == null) {
            manques.add("le representant de l'operateur");
        } else {
            if (estVide(r.getApplicantName())) { manques.add("le representant : l'identite"); }
            if (estVide(r.getQualification())) { manques.add("le representant : la fonction"); }
            if (estVide(r.getEmail()))         { manques.add("le representant : l'adresse electronique"); }
            if (estVide(r.getAddress()))       { manques.add("le representant : l'adresse permanente"); }
        }

        // ----- la nature de la demande, rubrique 3
        if (input.getNatureDemande() == null) {
            manques.add("la nature de la demande");
        }

        // ----- la nature de l'activite, rubrique 3
        if (input.getNatureActivite() == null) {
            manques.add("la nature de l'activite (mobile ou fixe)");
        }
        if (input.getTypeOperation() == null) {
            manques.add("le type d'operation (attribution ou restitution)");
        }

        // ----- les blocs, rubrique 4
        //
        // Au moins un : que ce soit pour en demander ou pour en rendre, un
        // dossier qui n'en nomme aucun ne porte sur rien.
        if (input.getBlocs() == null || input.getBlocs().isEmpty()) {
            manques.add("au moins un bloc BPQ (rubrique 4)");
        } else {
            for (int i = 0; i < input.getBlocs().size(); i++) {
                BlocNumerosDTO b = input.getBlocs().get(i);
                if (b == null || estVide(b.getBloc())) {
                    manques.add("le bloc n" + (i + 1) + " : la valeur du BPQ");
                }
            }
        }

        // Une RESTITUTION porte sur des blocs deja attribues : sans la
        // reference du titre, l'instruction ne sait pas ce qui est rendu.
        if (input.getTypeOperation() == TypeOperationPq.RESTITUTION
                && estVide(input.getReferenceAutorisationAnterieure())) {
            manques.add("la reference de l'autorisation dont les blocs sont restitues");
        }

        if (!manques.isEmpty()) {
            throw new BadRequestAlertException(
                    "Le dossier ne peut pas etre soumis, il manque : " + String.join(", ", manques),
                    PqErrors.CLASS, PqErrors.OBJECT_NOT_VALID);
        }
    }

    /** Vrai pour une chaine nulle, vide ou faite d'espaces. */
    private static boolean estVide(String v) {
        return v == null || v.trim().isEmpty();
    }

    private void exigerPiecesObligatoires(DemandePqInputDTO input, AclClass aclClass) {
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
                    PqErrors.CLASS, PqErrors.PIECES_MANQUANTES);
        }
    }

    /**
     * Les frais de dossier, rubrique 9 du formulaire : un FORFAIT de
     * 200 000 FCFA par pq.
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
     * Le numero court en a une (4 millions pour un classique, 10 pour un
     * gold), pas celui-ci. On ne pose donc aucun montant : en inventer un
     * reviendrait a facturer une somme que rien n'autorise, et mettre zero
     * laisserait croire a une gratuite decidee alors qu'elle n'est ecrite
     * nulle part. La colonne restera vide jusqu'a ce que le metier fournisse
     * un bareme.
     */
    void arreterFraisDossier(DemandePqInputDTO input) {
        if (input.getDeviseFrais() == null) {
            input.setDeviseFrais(DEVISE_PAR_DEFAUT);
        }
    }

    public DemandePqOutputDTO initAndSubmit(DemandePqInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(PqErrors.ACL_CLASS_NOT_FOUND,
                    PqErrors.CLASS, PqErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);

        input.setCreatedDate(ZonedDateTime.now());
        input.setSendedDate(ZonedDateTime.now());
        arreterFraisDossier(input);

        DemandePq entity = toEntityOrLoad(input);
        entity = demandePqRepository.save(entity);

        DemandePqOutputDTO output = demandePqOutputMapper.toDto(entity);
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

        return demandePqOutputMapper.toDto(entity);
    }

    /**
     * Transitions suivantes : le dossier existe et son instance de process tourne.
     */
    public DemandePqOutputDTO submit(DemandePqInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(PqErrors.ACL_CLASS_NOT_FOUND,
                    PqErrors.CLASS, PqErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);
        if (input.getId() == null) {
            throw new BadRequestAlertException(PqErrors.OBJECT_NOT_VALID,
                    PqErrors.CLASS, PqErrors.OBJECT_NOT_VALID);
        }

        DemandePq entity = demandePqRepository.findById(input.getId())
                .orElseThrow(() -> new BadRequestAlertException(PqErrors.OBJECT_NOT_FOUND,
                        PqErrors.CLASS, PqErrors.OBJECT_NOT_FOUND));

        demandePqInputMapper.partialUpdate(entity, input);
        entity = demandePqRepository.save(entity);

        DemandePqOutputDTO output = demandePqOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        BpmJob bpmJob = workflowService._nextTask(entity.getWfProcessID(),
                input.getDecision(), input.getWfComment(), output, aclClass);

        entity = appliquerRetourMoteur(bpmJob, aclClass);

        saveWorkflowCommentaire(input.getCommentaire(), entity, aclClass);

        return demandePqOutputMapper.toDto(entity);
    }

    /**
     * Enregistrement sans franchir d'etape : le circuit n'est pas sollicite.
     */
    public DemandePqOutputDTO saveAsDraft(DemandePqInputDTO input, AclClass aclClass) {
        if (aclClass == null) {
            throw new BadRequestAlertException(PqErrors.ACL_CLASS_NOT_FOUND,
                    PqErrors.CLASS, PqErrors.ACL_CLASS_NOT_FOUND);
        }

        DemandePq entity = toEntityOrLoad(input);
        entity = demandePqRepository.save(entity);

        DemandePqOutputDTO output = demandePqOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        // le brouillon n'est visible que de son auteur tant qu'il n'est pas soumis
        entity = persistAndApplySecurity(entity,
                Arrays.asList(currentUser.getEmployeSid()), new ArrayList<>(), aclClass);

        return demandePqOutputMapper.toDto(entity);
    }

    // ------------------------------------------------------------- internes

    private DemandePq toEntityOrLoad(DemandePqInputDTO input) {
        if (input.getId() != null) {
            Optional<DemandePq> existant = demandePqRepository.findById(input.getId());
            if (existant.isPresent()) {
                DemandePq entity = existant.get();
                demandePqInputMapper.partialUpdate(entity, input);
                return entity;
            }
        }
        return demandePqInputMapper.toEntity(input);
    }

    /**
     * Reporte sur l'entite ce que le moteur a decide, puis rejoue la securite.
     */
    private DemandePq appliquerRetourMoteur(BpmJob bpmJob, AclClass aclClass) throws Exception {
        DemandePqOutputDTO output = (DemandePqOutputDTO) bpmJob.getDataObject();

        // On repart de l'instance GEREE, jamais d'une entite reconstruite : le DTO
        // porterait une collection neuve, et le merge ferait lever Hibernate sur
        // orphanRemoval. Il ne porte pas non plus l'aclObjectIdentity.
        DemandePq entity = output.getId() != null
                ? demandePqRepository.findById(output.getId()).orElse(null)
                : null;
        if (entity == null) {
            entity = demandePqOutputMapper.toEntity(output);
        } else {
            demandePqOutputMapper.partialUpdate(entity, output);
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
    private DemandePq persistAndApplySecurity(DemandePq entity,
                                                        List<String> authors, List<String> readers,
                                                        AclClass aclClass) {
        if (entity.getId() != null) {
            demandePqRepository.findById(entity.getId()).ifPresent(recent -> {
                if (entity.getAclObjectIdentity() == null) {
                    entity.setAclObjectIdentity(recent.getAclObjectIdentity());
                }
            });
        }

        rattacherEnfants(entity);

        DemandePq persiste = demandePqRepository.save(entity);

        try {
            if (authors != null && readers != null) {
                kernelInterface.applySecurity(aclClass.getClasse(), persiste.getId(), authors, readers,
                        new ArrayList<>(), null, null, persiste.getAclObjectIdentity() == null, false);
            }
            if (persiste.getAclObjectIdentity() == null) {
                persiste = setAclObjectIdentity(persiste, aclClass);
                persiste = demandePqRepository.save(persiste);
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
    private void ouvrirRapportTechnique(DemandePq entity) {
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
        rapport.setDemandePq(entity);
        entity.setRapportTechnique(rapport);
    }

    private void rattacherEnfants(DemandePq entity) {
        if (entity.getClient() != null) {
            entity.getClient().setDemandePq(entity);
        }
        if (entity.getApplicants() != null) {
            entity.getApplicants().forEach(a -> a.setDemandePq(entity));
        }
        if (entity.getBlocs() != null) {
            entity.getBlocs().forEach(e -> e.setDemandePq(entity));
        }
        if (entity.getRapportTechnique() != null) {
            entity.getRapportTechnique().setDemandePq(entity);
        }
        if (entity.getAttestations() != null) {
            entity.getAttestations().forEach(a -> a.setDemandePq(entity));
        }
    }

    public DemandePq setAclObjectIdentity(DemandePq entity, AclClass aclClass) {
        Integer aclObjectIdentityID = kernelInterface.findACLObjectIdentity(aclClass.getId(), entity.getId());
        if (aclObjectIdentityID == null) {
            return entity;
        }
        AclObjectIdentity aclObjectIdentity = new AclObjectIdentity();
        aclObjectIdentity.setId(aclObjectIdentityID.longValue());
        entity.setAclObjectIdentity(aclObjectIdentity);
        return entity;
    }

    private void publierPiecesJointes(DemandePqInputDTO input, DemandePqOutputDTO output,
                                      DemandePq entity, AclClass aclClass) {
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

    private void saveWorkflowCommentaire(String texte, DemandePq entity, AclClass aclClass) {
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
