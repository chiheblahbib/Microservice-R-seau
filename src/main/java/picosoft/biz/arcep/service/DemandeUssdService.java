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
import picosoft.biz.arcep.client.kernel.model.acl.Permission;
import picosoft.biz.arcep.client.kernel.model.acl.AclObjectIdentity;
import picosoft.biz.arcep.client.kernel.model.objects.AttachementInputDTO;
import picosoft.biz.arcep.client.kernel.model.objects.PublicAttachementDto;
import picosoft.biz.arcep.controller.errors.BadRequestAlertException;
import picosoft.biz.arcep.controller.errors.UssdErrors;
import picosoft.biz.arcep.domain.ussd.DemandeUssd;
import picosoft.biz.arcep.domain.shared.Commentaire;
import picosoft.biz.arcep.domain.drrrs.RapportTechnique;
import picosoft.biz.arcep.service.dto.RapportTechniqueDTO;
import picosoft.biz.arcep.repository.CommentaireRepository;
import picosoft.biz.arcep.repository.DemandeUssdRepository;
import picosoft.biz.arcep.service.criteria.DemandeUssdCriteria;
import picosoft.biz.arcep.domain.ussd.enumeration.*;
import picosoft.biz.arcep.service.dto.DemandeUssdDTO;
import picosoft.biz.arcep.service.dto.ApplicantDTO;
import picosoft.biz.arcep.domain.ussd.enumeration.NatureActivite;
import picosoft.biz.arcep.service.dto.CodeUssdDTO;
import picosoft.biz.arcep.service.dto.DemandeUssdInputDTO;
import picosoft.biz.arcep.service.dto.DemandeUssdOutputDTO;
import picosoft.biz.arcep.service.mapper.DemandeUssdInputMapper;
import picosoft.biz.arcep.service.mapper.DemandeUssdMapper;
import picosoft.biz.arcep.service.mapper.DemandeUssdOutputMapper;

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
public class DemandeUssdService {

    private final Logger log = LoggerFactory.getLogger(DemandeUssdService.class);

    private final DemandeUssdRepository demandeUssdRepository;
    private final DemandeUssdQueryService demandeUssdQueryService;
    private final DemandeUssdMapper demandeUssdMapper;
    private final DemandeUssdInputMapper demandeUssdInputMapper;
    private final DemandeUssdOutputMapper demandeUssdOutputMapper;
    private final CommentaireRepository commentaireRepository;
    private final KernelInterface kernelInterface;
    private final WorkflowService workflowService;
    private final CurrentUser currentUser;

    public DemandeUssdService(DemandeUssdRepository demandeUssdRepository,
                                      DemandeUssdQueryService demandeUssdQueryService,
                                      DemandeUssdMapper demandeUssdMapper,
                                      DemandeUssdInputMapper demandeUssdInputMapper,
                                      DemandeUssdOutputMapper demandeUssdOutputMapper,
                                      CommentaireRepository commentaireRepository,
                                      KernelInterface kernelInterface,
                                      WorkflowService workflowService,
                                      CurrentUser currentUser) {
        this.demandeUssdRepository = demandeUssdRepository;
        this.demandeUssdQueryService = demandeUssdQueryService;
        this.demandeUssdMapper = demandeUssdMapper;
        this.demandeUssdInputMapper = demandeUssdInputMapper;
        this.demandeUssdOutputMapper = demandeUssdOutputMapper;
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
    public DemandeUssdDTO save(DemandeUssdDTO dto) {
        if (dto.getId() != null) {
            Optional<DemandeUssd> existant = demandeUssdRepository.findById(dto.getId());
            if (existant.isPresent()) {
                DemandeUssd gere = existant.get();
                demandeUssdMapper.partialUpdate(gere, dto);
                rattacherEnfants(gere);
                return demandeUssdMapper.toDto(demandeUssdRepository.save(gere));
            }
        }
        DemandeUssd entity = demandeUssdMapper.toEntity(dto);
        rattacherEnfants(entity);
        return demandeUssdMapper.toDto(demandeUssdRepository.save(entity));
    }

    public DemandeUssdDTO update(Long id, DemandeUssdDTO dto) {
        DemandeUssd entity = demandeUssdRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(UssdErrors.OBJECT_NOT_FOUND,
                        UssdErrors.CLASS, UssdErrors.OBJECT_NOT_FOUND));
        demandeUssdMapper.partialUpdate(entity, dto);
        rattacherEnfants(entity);
        return demandeUssdMapper.toDto(demandeUssdRepository.save(entity));
    }

    public Page<DemandeUssdDTO> findAll(DemandeUssdCriteria criteria, Pageable pageable, Integer size) {
        return demandeUssdQueryService.findByCriteria(criteria, pageable, size);
    }

    /**
     * Le dossier pour l'ecran de SAISIE, avec ce que le kernel autorise.
     *
     * A la difference de byId, cette lecture ne refuse RIEN. byId sert
     * l'ecran de detail et applique la regle du gabarit -- un acces NONE
     * est une erreur. Ici on renseigne la permission et on laisse le
     * formulaire en tirer les consequences rubrique par rubrique, ce qui
     * est precisement ce que la map `components` du circuit decrit.
     */
    public Optional<DemandeUssdDTO> findOne(Long id) {
        return demandeUssdRepository.findById(id).map(entity -> {
            DemandeUssdDTO dto = demandeUssdMapper.toDto(entity);
            dto.setUserPermission(permissionSur(entity.getId()));
            return dto;
        });
    }

    /**
     * Ce que le kernel accorde a l'utilisateur courant, ou null s'il se tait.
     *
     * Null n'est pas un refus, c'est une absence de reponse : classe ACL
     * pas encore declaree, ou kernel injoignable. L'ecran retombe alors
     * sur son comportement d'avant plutot que de se verrouiller sur une
     * donnee qu'il n'a pas. Verrouiller sur un silence ferait passer une
     * panne d'infrastructure pour un refus de droits.
     */
    private String permissionSur(Long id) {
        if (id == null) { return null; }
        try {
            AclClass aclClass = kernelInterface.getaclClassByClassName(DemandeUssd.class.getName());
            if (aclClass == null) { return null; }
            return kernelInterface.checkSecurity(aclClass.getSimpleName(), id, currentUser.getSid());
        } catch (Exception e) {
            log.warn("checkSecurity indisponible pour le dossier {} : {}", id, e.toString());
            return null;
        }
    }

    /**
     * Le dossier tel que CET utilisateur a le droit de le voir.
     *
     * Repris de AsiService.asiById : resoudre la classe ACL, demander au
     * kernel la permission de l'utilisateur courant sur cet objet, refuser
     * si elle est NONE, puis la poser sur le DTO. Le front lit ensuite
     * `userPermission` exactement comme sur un dossier ASI.
     *
     * Le refus ne vaut que si l'objet porte deja une identite ACL : un
     * dossier tout juste cree n'en a pas encore, et le kernel n'a alors
     * rien a repondre -- l'interdire fermerait l'ecran a son propre auteur.
     *
     * Ecart assume avec le gabarit : le test est ecrit dans l'autre sens,
     * Permission.NONE.name().equals(permission). Celui d'ASI appelle
     * .equals sur la reponse du kernel et leve un NullPointerException si
     * elle est nulle. Meme comportement pour toute reponse non nulle.
     */
    public DemandeUssdOutputDTO byId(Long id) {
        DemandeUssd entity = demandeUssdRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(UssdErrors.OBJECT_NOT_FOUND,
                        UssdErrors.CLASS, UssdErrors.OBJECT_NOT_FOUND));

        AclClass aclClass = kernelInterface.getaclClassByClassName(DemandeUssd.class.getName());
        if (aclClass == null) {
            throw new BadRequestAlertException(UssdErrors.ACL_CLASS_NOT_FOUND,
                    UssdErrors.CLASS, UssdErrors.ACL_CLASS_NOT_FOUND);
        }

        String permission = kernelInterface.checkSecurity(
                aclClass.getSimpleName(), id, currentUser.getSid());

        if (Permission.NONE.name().equals(permission) && entity.getAclObjectIdentity() != null) {
            throw new BadRequestAlertException(UssdErrors.OBJECT_NOT_AUTHORIZED,
                    UssdErrors.CLASS, UssdErrors.OBJECT_NOT_AUTHORIZED);
        }

        DemandeUssdOutputDTO output = demandeUssdOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());
        output.setUserPermission(permission);
        return output;
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
        DemandeUssd entity = demandeUssdRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(UssdErrors.OBJECT_NOT_FOUND,
                        UssdErrors.CLASS, UssdErrors.OBJECT_NOT_FOUND));

        if (entity.getWfProcessID() != null) {
            throw new BadRequestAlertException(UssdErrors.OBJECT_ENGAGED,
                    UssdErrors.CLASS, UssdErrors.OBJECT_ENGAGED);
        }
        // Une autorisation delivree est un document officiel : le dossier qui la
        // porte ne s'efface pas, meme si son circuit est termine.
        if (entity.getAttestations() != null && !entity.getAttestations().isEmpty()) {
            throw new BadRequestAlertException(UssdErrors.OBJECT_ENGAGED,
                    UssdErrors.CLASS, UssdErrors.OBJECT_ENGAGED);
        }
        // Pas de garde par site : contrairement aux stations d'une demande
        // d'implantation, les sites d'un reseau ne portent pas de circuit propre.
        // Le reseau est autorise d'un bloc, et c'est le circuit du dossier -- teste
        // juste au-dessus -- qui protege la suppression.
        demandeUssdRepository.delete(entity);
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
    public void exigerPourSoumission(DemandeUssdInputDTO input) {
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

        // ----- le representant, rubrique 2
        //
        // Il vit sur `applicant`, la table partagee : UNE SEULE personne par
        // dossier, en paire avec le `client` qui porte la structure. Le nom et
        // les prenoms s'y fondent dans `applicantName`, comme chez ASI.
        ApplicantDTO r = input.getApplicant();
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
            manques.add("la nature de l'activite");
        } else if (input.getNatureActivite() == NatureActivite.AUTRE
                && estVide(input.getNatureActivitePrecision())) {
            manques.add("preciser l'autre reseau ou service");
        }

        // ----- les codes sollicites, rubrique 5
        //
        // Au moins un : une demande d'attribution qui ne nomme aucun code ne
        // porte sur rien. Le NOMBRE sollicite, lui, n'est pas exige : il se
        // deduit de la liste, et le reclamer en plus ferait saisir deux fois
        // la meme information -- avec le risque qu'elles divergent.
        if (input.getCodes() == null || input.getCodes().isEmpty()) {
            manques.add("au moins un code USSD sollicite (rubrique 5)");
        } else {
            for (int i = 0; i < input.getCodes().size(); i++) {
                CodeUssdDTO c = input.getCodes().get(i);
                if (c == null || estVide(c.getCode())) {
                    manques.add("le code n" + (i + 1) + " : la valeur");
                }
            }
        }

        // ----- la description des services, rubrique 6
        //
        // Exigee : c'est sur elle que l'instruction juge l'usage annonce des
        // codes, et un dossier qui ne la porte pas ne dit pas a quoi ils
        // serviront.
        if (estVide(input.getDescriptionServices())) {
            manques.add("la description des services offerts sur les codes");
        }

        // Le TARIF AUX USAGERS (rubrique 7) n'est PAS exige : un service
        // gratuit est parfaitement legitime, et une zone laissee vide se lit
        // alors correctement.

        if (!manques.isEmpty()) {
            throw new BadRequestAlertException(
                    "Le dossier ne peut pas etre soumis, il manque : " + String.join(", ", manques),
                    UssdErrors.CLASS, UssdErrors.OBJECT_NOT_VALID);
        }
    }

    /** Vrai pour une chaine nulle, vide ou faite d'espaces. */
    private static boolean estVide(String v) {
        return v == null || v.trim().isEmpty();
    }

    private void exigerPiecesObligatoires(DemandeUssdInputDTO input, AclClass aclClass) {
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
                    UssdErrors.CLASS, UssdErrors.PIECES_MANQUANTES);
        }
    }

    /**
     * Les frais de dossier, rubrique 9 du formulaire : un FORFAIT de
     * 200 000 FCFA par ussd.
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
     * Il demande bien un tarif rubrique 7, mais c'est celui applique aux
     * USAGERS -- une information commerciale, pas une redevance due a l'ARCEP.
     * On ne pose donc aucun montant sur le dossier : y recopier le tarif
     * usager ferait facturer l'operateur de ce qu'il facture a ses clients.
     */
    void arreterFraisDossier(DemandeUssdInputDTO input) {
        if (input.getDeviseFrais() == null) {
            input.setDeviseFrais(DEVISE_PAR_DEFAUT);
        }
    }

    public DemandeUssdOutputDTO initAndSubmit(DemandeUssdInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(UssdErrors.ACL_CLASS_NOT_FOUND,
                    UssdErrors.CLASS, UssdErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);

        input.setCreatedDate(ZonedDateTime.now());
        input.setSendedDate(ZonedDateTime.now());
        arreterFraisDossier(input);

        DemandeUssd entity = toEntityOrLoad(input);
        entity = demandeUssdRepository.save(entity);

        DemandeUssdOutputDTO output = demandeUssdOutputMapper.toDto(entity);
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

        return demandeUssdOutputMapper.toDto(entity);
    }

    /**
     * Transitions suivantes : le dossier existe et son instance de process tourne.
     */
    public DemandeUssdOutputDTO submit(DemandeUssdInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(UssdErrors.ACL_CLASS_NOT_FOUND,
                    UssdErrors.CLASS, UssdErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);
        if (input.getId() == null) {
            throw new BadRequestAlertException(UssdErrors.OBJECT_NOT_VALID,
                    UssdErrors.CLASS, UssdErrors.OBJECT_NOT_VALID);
        }

        DemandeUssd entity = demandeUssdRepository.findById(input.getId())
                .orElseThrow(() -> new BadRequestAlertException(UssdErrors.OBJECT_NOT_FOUND,
                        UssdErrors.CLASS, UssdErrors.OBJECT_NOT_FOUND));

        // C'est le kernel qui dit qui peut faire avancer ce dossier, et lui
        // seul. Repris d'AsiService.submitAsi : sans WRITE ni INH_WRITE la
        // transition est refusee ici, quelle que soit la decision demandee.
        //
        // C'est le PENDANT SERVEUR du bandeau de decisions, qui ne s'affiche
        // cote front que sur un userPermission a WRITE. Une garde d'ecran
        // seule se contourne avec un appel direct : les deux vont ensemble.
        //
        // Le droit est rejoue a chaque transition -- appliquerRetourMoteur
        // repousse les authors et readers que le moteur rend pour la tache
        // suivante -- donc l'acteur de la tache en cours est toujours auteur.
        String permission = kernelInterface.checkSecurity(
                aclClass.getSimpleName(), entity.getId(), currentUser.getSid());
        if (!Permission.WRITE.name().equals(permission)
                && !Permission.INH_WRITE.name().equals(permission)) {
            throw new BadRequestAlertException(UssdErrors.OBJECT_NOT_AUTHORIZED,
                    UssdErrors.CLASS, UssdErrors.OBJECT_NOT_AUTHORIZED);
        }

        demandeUssdInputMapper.partialUpdate(entity, input);
        entity = demandeUssdRepository.save(entity);

        DemandeUssdOutputDTO output = demandeUssdOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        BpmJob bpmJob = workflowService._nextTask(entity.getWfProcessID(),
                input.getDecision(), input.getWfComment(), output, aclClass);

        entity = appliquerRetourMoteur(bpmJob, aclClass);

        saveWorkflowCommentaire(input.getCommentaire(), entity, aclClass);

        return demandeUssdOutputMapper.toDto(entity);
    }

    /**
     * Enregistrement sans franchir d'etape : le circuit n'est pas sollicite.
     */
    public DemandeUssdOutputDTO saveAsDraft(DemandeUssdInputDTO input, AclClass aclClass) {
        if (aclClass == null) {
            throw new BadRequestAlertException(UssdErrors.ACL_CLASS_NOT_FOUND,
                    UssdErrors.CLASS, UssdErrors.ACL_CLASS_NOT_FOUND);
        }

        DemandeUssd entity = toEntityOrLoad(input);
        entity = demandeUssdRepository.save(entity);

        DemandeUssdOutputDTO output = demandeUssdOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        // le brouillon n'est visible que de son auteur tant qu'il n'est pas soumis
        entity = persistAndApplySecurity(entity,
                Arrays.asList(currentUser.getEmployeSid()), new ArrayList<>(), aclClass);

        return demandeUssdOutputMapper.toDto(entity);
    }

    // ------------------------------------------------------------- internes

    private DemandeUssd toEntityOrLoad(DemandeUssdInputDTO input) {
        if (input.getId() != null) {
            Optional<DemandeUssd> existant = demandeUssdRepository.findById(input.getId());
            if (existant.isPresent()) {
                DemandeUssd entity = existant.get();
                demandeUssdInputMapper.partialUpdate(entity, input);
                return entity;
            }
        }
        return demandeUssdInputMapper.toEntity(input);
    }

    /**
     * Reporte sur l'entite ce que le moteur a decide, puis rejoue la securite.
     */
    private DemandeUssd appliquerRetourMoteur(BpmJob bpmJob, AclClass aclClass) throws Exception {
        DemandeUssdOutputDTO output = (DemandeUssdOutputDTO) bpmJob.getDataObject();

        // On repart de l'instance GEREE, jamais d'une entite reconstruite : le DTO
        // porterait une collection neuve, et le merge ferait lever Hibernate sur
        // orphanRemoval. Il ne porte pas non plus l'aclObjectIdentity.
        DemandeUssd entity = output.getId() != null
                ? demandeUssdRepository.findById(output.getId()).orElse(null)
                : null;
        if (entity == null) {
            entity = demandeUssdOutputMapper.toEntity(output);
        } else {
            demandeUssdOutputMapper.partialUpdate(entity, output);
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
    private DemandeUssd persistAndApplySecurity(DemandeUssd entity,
                                                        List<String> authors, List<String> readers,
                                                        AclClass aclClass) {
        if (entity.getId() != null) {
            demandeUssdRepository.findById(entity.getId()).ifPresent(recent -> {
                if (entity.getAclObjectIdentity() == null) {
                    entity.setAclObjectIdentity(recent.getAclObjectIdentity());
                }
            });
        }

        rattacherEnfants(entity);

        DemandeUssd persiste = demandeUssdRepository.save(entity);

        try {
            if (authors != null && readers != null) {
                kernelInterface.applySecurity(aclClass.getClasse(), persiste.getId(), authors, readers,
                        new ArrayList<>(), null, null, persiste.getAclObjectIdentity() == null, false);
            }
            if (persiste.getAclObjectIdentity() == null) {
                persiste = setAclObjectIdentity(persiste, aclClass);
                persiste = demandeUssdRepository.save(persiste);
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
    private void ouvrirRapportTechnique(DemandeUssd entity) {
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
        rapport.setDemandeUssd(entity);
        entity.setRapportTechnique(rapport);
    }

    private void rattacherEnfants(DemandeUssd entity) {
        if (entity.getClient() != null) {
            entity.getClient().setDemandeUssd(entity);
        }
        if (entity.getApplicant() != null) {

            entity.getApplicant().setDemandeUssd(entity);

        }
        if (entity.getCodes() != null) {
            entity.getCodes().forEach(e -> e.setDemandeUssd(entity));
        }
        if (entity.getRapportTechnique() != null) {
            entity.getRapportTechnique().setDemandeUssd(entity);
        }
        if (entity.getAttestations() != null) {
            entity.getAttestations().forEach(a -> a.setDemandeUssd(entity));
        }
    }

    public DemandeUssd setAclObjectIdentity(DemandeUssd entity, AclClass aclClass) {
        Integer aclObjectIdentityID = kernelInterface.findACLObjectIdentity(aclClass.getId(), entity.getId());
        if (aclObjectIdentityID == null) {
            return entity;
        }
        AclObjectIdentity aclObjectIdentity = new AclObjectIdentity();
        aclObjectIdentity.setId(aclObjectIdentityID.longValue());
        entity.setAclObjectIdentity(aclObjectIdentity);
        return entity;
    }

    private void publierPiecesJointes(DemandeUssdInputDTO input, DemandeUssdOutputDTO output,
                                      DemandeUssd entity, AclClass aclClass) {
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

    private void saveWorkflowCommentaire(String texte, DemandeUssd entity, AclClass aclClass) {
        if (texte == null || texte.trim().isEmpty()) {
            return;
        }
        Commentaire commentaire = new Commentaire();
        commentaire.setAuteur(currentUser.nomPourCommentaire());
        commentaire.setDescription(texte);
        commentaire.setDateSaisie(ZonedDateTime.now());
        commentaire.setClassId(aclClass.getId());
        commentaire.setObjectID(entity.getId());
        commentaire.setExterne(false);
        commentaireRepository.save(commentaire);
    }
}
