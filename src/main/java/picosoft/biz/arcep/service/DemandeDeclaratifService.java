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
import picosoft.biz.arcep.controller.errors.DeclaratifErrors;
import picosoft.biz.arcep.domain.declaratif.DemandeDeclaratif;
import picosoft.biz.arcep.domain.shared.Commentaire;
import picosoft.biz.arcep.domain.shared.RapportTechnique;
import picosoft.biz.arcep.service.dto.RapportTechniqueDTO;
import picosoft.biz.arcep.repository.CommentaireRepository;
import picosoft.biz.arcep.repository.DemandeDeclaratifRepository;
import picosoft.biz.arcep.service.criteria.DemandeDeclaratifCriteria;
import picosoft.biz.arcep.domain.declaratif.enumeration.*;
import picosoft.biz.arcep.service.dto.DemandeDeclaratifDTO;
import picosoft.biz.arcep.service.dto.ApplicantDTO;
import picosoft.biz.arcep.domain.declaratif.enumeration.TypeCouverture;
import picosoft.biz.arcep.domain.declaratif.enumeration.TypeEnregistrement;
import picosoft.biz.arcep.domain.declaratif.enumeration.TypeServiceDeclare;
import picosoft.biz.arcep.service.dto.ServiceDeclareDeclaratifDTO;
import picosoft.biz.arcep.service.dto.DemandeDeclaratifInputDTO;
import picosoft.biz.arcep.service.dto.DemandeDeclaratifOutputDTO;
import picosoft.biz.arcep.service.mapper.DemandeDeclaratifInputMapper;
import picosoft.biz.arcep.service.mapper.DemandeDeclaratifMapper;
import picosoft.biz.arcep.service.mapper.DemandeDeclaratifOutputMapper;

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
public class DemandeDeclaratifService {

    private final Logger log = LoggerFactory.getLogger(DemandeDeclaratifService.class);

    private final DemandeDeclaratifRepository demandeDeclaratifRepository;
    private final DemandeDeclaratifQueryService demandeDeclaratifQueryService;
    private final DemandeDeclaratifMapper demandeDeclaratifMapper;
    private final DemandeDeclaratifInputMapper demandeDeclaratifInputMapper;
    private final DemandeDeclaratifOutputMapper demandeDeclaratifOutputMapper;
    private final CommentaireRepository commentaireRepository;
    private final KernelInterface kernelInterface;
    private final WorkflowService workflowService;
    private final CurrentUser currentUser;

    public DemandeDeclaratifService(DemandeDeclaratifRepository demandeDeclaratifRepository,
                                      DemandeDeclaratifQueryService demandeDeclaratifQueryService,
                                      DemandeDeclaratifMapper demandeDeclaratifMapper,
                                      DemandeDeclaratifInputMapper demandeDeclaratifInputMapper,
                                      DemandeDeclaratifOutputMapper demandeDeclaratifOutputMapper,
                                      CommentaireRepository commentaireRepository,
                                      KernelInterface kernelInterface,
                                      WorkflowService workflowService,
                                      CurrentUser currentUser) {
        this.demandeDeclaratifRepository = demandeDeclaratifRepository;
        this.demandeDeclaratifQueryService = demandeDeclaratifQueryService;
        this.demandeDeclaratifMapper = demandeDeclaratifMapper;
        this.demandeDeclaratifInputMapper = demandeDeclaratifInputMapper;
        this.demandeDeclaratifOutputMapper = demandeDeclaratifOutputMapper;
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
    public DemandeDeclaratifDTO save(DemandeDeclaratifDTO dto) {
        if (dto.getId() != null) {
            Optional<DemandeDeclaratif> existant = demandeDeclaratifRepository.findById(dto.getId());
            if (existant.isPresent()) {
                DemandeDeclaratif gere = existant.get();
                demandeDeclaratifMapper.partialUpdate(gere, dto);
                rattacherEnfants(gere);
                return demandeDeclaratifMapper.toDto(demandeDeclaratifRepository.save(gere));
            }
        }
        DemandeDeclaratif entity = demandeDeclaratifMapper.toEntity(dto);
        rattacherEnfants(entity);
        return demandeDeclaratifMapper.toDto(demandeDeclaratifRepository.save(entity));
    }

    public DemandeDeclaratifDTO update(Long id, DemandeDeclaratifDTO dto) {
        DemandeDeclaratif entity = demandeDeclaratifRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(DeclaratifErrors.OBJECT_NOT_FOUND,
                        DeclaratifErrors.CLASS, DeclaratifErrors.OBJECT_NOT_FOUND));
        demandeDeclaratifMapper.partialUpdate(entity, dto);
        rattacherEnfants(entity);
        return demandeDeclaratifMapper.toDto(demandeDeclaratifRepository.save(entity));
    }

    public Page<DemandeDeclaratifDTO> findAll(DemandeDeclaratifCriteria criteria, Pageable pageable, Integer size) {
        return demandeDeclaratifQueryService.findByCriteria(criteria, pageable, size);
    }

    public Optional<DemandeDeclaratifDTO> findOne(Long id) {
        return demandeDeclaratifRepository.findById(id).map(demandeDeclaratifMapper::toDto);
    }

    public DemandeDeclaratifOutputDTO byId(Long id) {
        DemandeDeclaratif entity = demandeDeclaratifRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(DeclaratifErrors.OBJECT_NOT_FOUND,
                        DeclaratifErrors.CLASS, DeclaratifErrors.OBJECT_NOT_FOUND));
        return demandeDeclaratifOutputMapper.toDto(entity);
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
        DemandeDeclaratif entity = demandeDeclaratifRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(DeclaratifErrors.OBJECT_NOT_FOUND,
                        DeclaratifErrors.CLASS, DeclaratifErrors.OBJECT_NOT_FOUND));

        if (entity.getWfProcessID() != null) {
            throw new BadRequestAlertException(DeclaratifErrors.OBJECT_ENGAGED,
                    DeclaratifErrors.CLASS, DeclaratifErrors.OBJECT_ENGAGED);
        }
        // Une autorisation delivree est un document officiel : le dossier qui la
        // porte ne s'efface pas, meme si son circuit est termine.
        if (entity.getAttestations() != null && !entity.getAttestations().isEmpty()) {
            throw new BadRequestAlertException(DeclaratifErrors.OBJECT_ENGAGED,
                    DeclaratifErrors.CLASS, DeclaratifErrors.OBJECT_ENGAGED);
        }
        // Pas de garde par site : contrairement aux stations d'une demande
        // d'implantation, les sites d'un reseau ne portent pas de circuit propre.
        // Le reseau est autorise d'un bloc, et c'est le circuit du dossier -- teste
        // juste au-dessus -- qui protege la suppression.
        demandeDeclaratifRepository.delete(entity);
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
    public void exigerPourSoumission(DemandeDeclaratifInputDTO input) {
        List<String> manques = new ArrayList<>();

        if (input.getNatureDemande() == null) { manques.add("la nature de la demande"); }

        // PAS DE CONTROLE DE RENOUVELLEMENT ICI, contrairement aux huit autres
        // dossiers du module : ce formulaire ne renouvelle pas une
        // autorisation, il MODIFIE un certificat d'enregistrement. La
        // verification equivalente porte donc sur `numeroCertificat`, plus
        // bas, sous le type d'enregistrement -- qui est la vraie question de
        // la rubrique 4.

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
        // ----- les correspondants obligatoires, rubriques 2 et 3
        //
        // Le formulaire marque les DEUX d'un asterisque. On exige donc le
        // correspondant de la declaration, et on signale separement l'absence
        // de celui du paiement plutot que de les confondre.
        //
        // Les personnes vivent desormais sur `applicant`, la table partagee, et
        // se reconnaissent a leur ROLE. Le nom et les prenoms s'y fondent dans
        // `applicantName`, comme chez ASI.
        ApplicantDTO r = Acteurs.parRole(input.getApplicants(), Acteurs.DECLARATION);
        if (!Acteurs.nomme(input.getApplicants(), Acteurs.PAIEMENT)) {
            manques.add("le correspondant relatif au paiement (rubrique 3)");
        }
        if (r == null) {
            manques.add("le correspondant relatif a la declaration (rubrique 2)");
        } else {
            if (estVide(r.getApplicantName())) { manques.add("le correspondant de la declaration : l'identite"); }
            if (estVide(r.getQualification())) { manques.add("le correspondant de la declaration : la fonction"); }
            if (estVide(r.getEmail()))         { manques.add("le correspondant de la declaration : l'adresse electronique"); }
            if (estVide(r.getAddress())) {
                manques.add("le correspondant de la declaration : l'adresse permanente");
            }
        }

        // ----- la nature de la demande, rubrique 3
        if (input.getNatureDemande() == null) {
            manques.add("la nature de la demande");
        }

        // ----- l'enregistrement, rubrique 4
        if (input.getTypeEnregistrement() == null) {
            manques.add("s'il s'agit d'une nouvelle declaration ou d'une modification");
        } else if (input.getTypeEnregistrement() == TypeEnregistrement.MODIFICATION_CERTIFICAT
                && estVide(input.getNumeroCertificat())) {
            // Modifier un certificat suppose de dire lequel.
            manques.add("le numero du certificat a modifier");
        }

        // ----- la clientele cible, rubrique 5 : au moins une case
        boolean uneClientele = Boolean.TRUE.equals(input.getClienteleGrandPublic())
                || Boolean.TRUE.equals(input.getClienteleProfessionnels())
                || Boolean.TRUE.equals(input.getClienteleOperateurs());
        if (!uneClientele) {
            manques.add("au moins un type de clientele cible");
        }

        // ----- la couverture, rubrique 6
        if (input.getTypeCouverture() == null) {
            manques.add("la couverture geographique");
        } else if (input.getTypeCouverture() == TypeCouverture.PROVINCE
                && estVide(input.getProvinces())) {
            // « Province de : ... » sans province ne couvre rien.
            manques.add("la ou les provinces couvertes");
        }

        // ----- les services declares, rubrique 7
        //
        // C'est l'objet meme de la declaration : un dossier qui n'en porte
        // aucun ne declare rien.
        if (input.getServices() == null || input.getServices().isEmpty()) {
            manques.add("au moins un service a valeur ajoutee (rubrique 7)");
        } else {
            for (int i = 0; i < input.getServices().size(); i++) {
                ServiceDeclareDeclaratifDTO sd = input.getServices().get(i);
                if (sd == null || sd.getTypeService() == null) {
                    manques.add("le service n" + (i + 1) + " : la nature");
                } else if (sd.getTypeService() == TypeServiceDeclare.AUTRE
                        && estVide(sd.getPrecisionAutre())) {
                    manques.add("le service n" + (i + 1) + " : preciser la nature");
                }
            }
        }

        if (!manques.isEmpty()) {
            throw new BadRequestAlertException(
                    "Le dossier ne peut pas etre soumis, il manque : " + String.join(", ", manques),
                    DeclaratifErrors.CLASS, DeclaratifErrors.OBJECT_NOT_VALID);
        }
    }

    /** Vrai pour une chaine nulle, vide ou faite d'espaces. */
    private static boolean estVide(String v) {
        return v == null || v.trim().isEmpty();
    }

    private void exigerPiecesObligatoires(DemandeDeclaratifInputDTO input, AclClass aclClass) {
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
                    DeclaratifErrors.CLASS, DeclaratifErrors.PIECES_MANQUANTES);
        }
    }

    /**
     * Les frais de dossier, rubrique 9 du formulaire : un FORFAIT de
     * 200 000 FCFA par declaratif.
     *
     * Pas de grille par service comme le reseau -- l'annexe de ce formulaire
     * ne connait qu'un montant. Il est arrete au depot et conserve sur le
     * dossier : un bareme qui change ne doit pas reecrire un dossier deja
     * instruit.
     */
    private static final String DEVISE_PAR_DEFAUT = "XAF";

    /**
     * Des frais mentionnes, mais AUCUN BAREME.
     *
     * La liste des pieces se termine par « 19) Frais d'etudes du dossier »
     * sans dire combien, et le formulaire n'a pas d'annexe tarifaire. Le
     * principe des frais est donc acquis, leur montant non.
     *
     * On ne pose rien : inventer un forfait facturerait une somme que rien
     * n'autorise, et mettre zero contredirait la piece 19 en affichant la
     * gratuite. Une colonne vide, elle, se lit correctement -- « montant a
     * determiner » -- et c'est exactement l'etat du dossier.
     */
    void arreterFraisDossier(DemandeDeclaratifInputDTO input) {
        if (input.getDeviseFrais() == null) {
            input.setDeviseFrais(DEVISE_PAR_DEFAUT);
        }
    }

    public DemandeDeclaratifOutputDTO initAndSubmit(DemandeDeclaratifInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(DeclaratifErrors.ACL_CLASS_NOT_FOUND,
                    DeclaratifErrors.CLASS, DeclaratifErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);

        input.setCreatedDate(ZonedDateTime.now());
        input.setSendedDate(ZonedDateTime.now());
        arreterFraisDossier(input);

        DemandeDeclaratif entity = toEntityOrLoad(input);
        entity = demandeDeclaratifRepository.save(entity);

        DemandeDeclaratifOutputDTO output = demandeDeclaratifOutputMapper.toDto(entity);
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

        return demandeDeclaratifOutputMapper.toDto(entity);
    }

    /**
     * Transitions suivantes : le dossier existe et son instance de process tourne.
     */
    public DemandeDeclaratifOutputDTO submit(DemandeDeclaratifInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(DeclaratifErrors.ACL_CLASS_NOT_FOUND,
                    DeclaratifErrors.CLASS, DeclaratifErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);
        if (input.getId() == null) {
            throw new BadRequestAlertException(DeclaratifErrors.OBJECT_NOT_VALID,
                    DeclaratifErrors.CLASS, DeclaratifErrors.OBJECT_NOT_VALID);
        }

        DemandeDeclaratif entity = demandeDeclaratifRepository.findById(input.getId())
                .orElseThrow(() -> new BadRequestAlertException(DeclaratifErrors.OBJECT_NOT_FOUND,
                        DeclaratifErrors.CLASS, DeclaratifErrors.OBJECT_NOT_FOUND));

        demandeDeclaratifInputMapper.partialUpdate(entity, input);
        entity = demandeDeclaratifRepository.save(entity);

        DemandeDeclaratifOutputDTO output = demandeDeclaratifOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        BpmJob bpmJob = workflowService._nextTask(entity.getWfProcessID(),
                input.getDecision(), input.getWfComment(), output, aclClass);

        entity = appliquerRetourMoteur(bpmJob, aclClass);

        saveWorkflowCommentaire(input.getCommentaire(), entity, aclClass);

        return demandeDeclaratifOutputMapper.toDto(entity);
    }

    /**
     * Enregistrement sans franchir d'etape : le circuit n'est pas sollicite.
     */
    public DemandeDeclaratifOutputDTO saveAsDraft(DemandeDeclaratifInputDTO input, AclClass aclClass) {
        if (aclClass == null) {
            throw new BadRequestAlertException(DeclaratifErrors.ACL_CLASS_NOT_FOUND,
                    DeclaratifErrors.CLASS, DeclaratifErrors.ACL_CLASS_NOT_FOUND);
        }

        DemandeDeclaratif entity = toEntityOrLoad(input);
        entity = demandeDeclaratifRepository.save(entity);

        DemandeDeclaratifOutputDTO output = demandeDeclaratifOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        // le brouillon n'est visible que de son auteur tant qu'il n'est pas soumis
        entity = persistAndApplySecurity(entity,
                Arrays.asList(currentUser.getEmployeSid()), new ArrayList<>(), aclClass);

        return demandeDeclaratifOutputMapper.toDto(entity);
    }

    // ------------------------------------------------------------- internes

    private DemandeDeclaratif toEntityOrLoad(DemandeDeclaratifInputDTO input) {
        if (input.getId() != null) {
            Optional<DemandeDeclaratif> existant = demandeDeclaratifRepository.findById(input.getId());
            if (existant.isPresent()) {
                DemandeDeclaratif entity = existant.get();
                demandeDeclaratifInputMapper.partialUpdate(entity, input);
                return entity;
            }
        }
        return demandeDeclaratifInputMapper.toEntity(input);
    }

    /**
     * Reporte sur l'entite ce que le moteur a decide, puis rejoue la securite.
     */
    private DemandeDeclaratif appliquerRetourMoteur(BpmJob bpmJob, AclClass aclClass) throws Exception {
        DemandeDeclaratifOutputDTO output = (DemandeDeclaratifOutputDTO) bpmJob.getDataObject();

        // On repart de l'instance GEREE, jamais d'une entite reconstruite : le DTO
        // porterait une collection neuve, et le merge ferait lever Hibernate sur
        // orphanRemoval. Il ne porte pas non plus l'aclObjectIdentity.
        DemandeDeclaratif entity = output.getId() != null
                ? demandeDeclaratifRepository.findById(output.getId()).orElse(null)
                : null;
        if (entity == null) {
            entity = demandeDeclaratifOutputMapper.toEntity(output);
        } else {
            demandeDeclaratifOutputMapper.partialUpdate(entity, output);
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
    private DemandeDeclaratif persistAndApplySecurity(DemandeDeclaratif entity,
                                                        List<String> authors, List<String> readers,
                                                        AclClass aclClass) {
        if (entity.getId() != null) {
            demandeDeclaratifRepository.findById(entity.getId()).ifPresent(recent -> {
                if (entity.getAclObjectIdentity() == null) {
                    entity.setAclObjectIdentity(recent.getAclObjectIdentity());
                }
            });
        }

        rattacherEnfants(entity);

        DemandeDeclaratif persiste = demandeDeclaratifRepository.save(entity);

        try {
            if (authors != null && readers != null) {
                kernelInterface.applySecurity(aclClass.getClasse(), persiste.getId(), authors, readers,
                        new ArrayList<>(), null, null, persiste.getAclObjectIdentity() == null, false);
            }
            if (persiste.getAclObjectIdentity() == null) {
                persiste = setAclObjectIdentity(persiste, aclClass);
                persiste = demandeDeclaratifRepository.save(persiste);
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
    private void ouvrirRapportTechnique(DemandeDeclaratif entity) {
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
        rapport.setDemandeDeclaratif(entity);
        entity.setRapportTechnique(rapport);
    }

    private void rattacherEnfants(DemandeDeclaratif entity) {
        if (entity.getClient() != null) {
            entity.getClient().setDemandeDeclaratif(entity);
        }
        if (entity.getApplicants() != null) {
            entity.getApplicants().forEach(a -> a.setDemandeDeclaratif(entity));
        }
        if (entity.getInfrastructures() != null) {
            entity.getInfrastructures().forEach(i -> i.setDemandeDeclaratif(entity));
        }
        if (entity.getServices() != null) {
            entity.getServices().forEach(e -> e.setDemandeDeclaratif(entity));
        }
        if (entity.getRapportTechnique() != null) {
            entity.getRapportTechnique().setDemandeDeclaratif(entity);
        }
        if (entity.getAttestations() != null) {
            entity.getAttestations().forEach(a -> a.setDemandeDeclaratif(entity));
        }
    }

    public DemandeDeclaratif setAclObjectIdentity(DemandeDeclaratif entity, AclClass aclClass) {
        Integer aclObjectIdentityID = kernelInterface.findACLObjectIdentity(aclClass.getId(), entity.getId());
        if (aclObjectIdentityID == null) {
            return entity;
        }
        AclObjectIdentity aclObjectIdentity = new AclObjectIdentity();
        aclObjectIdentity.setId(aclObjectIdentityID.longValue());
        entity.setAclObjectIdentity(aclObjectIdentity);
        return entity;
    }

    private void publierPiecesJointes(DemandeDeclaratifInputDTO input, DemandeDeclaratifOutputDTO output,
                                      DemandeDeclaratif entity, AclClass aclClass) {
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

    private void saveWorkflowCommentaire(String texte, DemandeDeclaratif entity, AclClass aclClass) {
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
