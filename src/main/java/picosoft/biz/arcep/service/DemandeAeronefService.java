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
import picosoft.biz.arcep.controller.errors.AeronefErrors;
import picosoft.biz.arcep.domain.aeronef.DemandeAeronef;
import picosoft.biz.arcep.domain.shared.Commentaire;
import picosoft.biz.arcep.domain.drrrs.RapportTechnique;
import picosoft.biz.arcep.service.dto.RapportTechniqueDTO;
import picosoft.biz.arcep.repository.CommentaireRepository;
import picosoft.biz.arcep.repository.DemandeAeronefRepository;
import picosoft.biz.arcep.service.criteria.DemandeAeronefCriteria;
import picosoft.biz.arcep.domain.aeronef.enumeration.*;
import picosoft.biz.arcep.service.dto.DemandeAeronefDTO;
import picosoft.biz.arcep.service.dto.ApplicantDTO;
import picosoft.biz.arcep.service.dto.EquipementBordAeronefDTO;
import picosoft.biz.arcep.service.dto.DemandeAeronefInputDTO;
import picosoft.biz.arcep.service.dto.DemandeAeronefOutputDTO;
import picosoft.biz.arcep.service.mapper.DemandeAeronefInputMapper;
import picosoft.biz.arcep.service.mapper.DemandeAeronefMapper;
import picosoft.biz.arcep.service.mapper.DemandeAeronefOutputMapper;

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
public class DemandeAeronefService {

    private final Logger log = LoggerFactory.getLogger(DemandeAeronefService.class);

    private final DemandeAeronefRepository demandeAeronefRepository;
    private final DemandeAeronefQueryService demandeAeronefQueryService;
    private final DemandeAeronefMapper demandeAeronefMapper;
    private final DemandeAeronefInputMapper demandeAeronefInputMapper;
    private final DemandeAeronefOutputMapper demandeAeronefOutputMapper;
    private final CommentaireRepository commentaireRepository;
    private final KernelInterface kernelInterface;
    private final WorkflowService workflowService;
    private final CurrentUser currentUser;

    public DemandeAeronefService(DemandeAeronefRepository demandeAeronefRepository,
                                      DemandeAeronefQueryService demandeAeronefQueryService,
                                      DemandeAeronefMapper demandeAeronefMapper,
                                      DemandeAeronefInputMapper demandeAeronefInputMapper,
                                      DemandeAeronefOutputMapper demandeAeronefOutputMapper,
                                      CommentaireRepository commentaireRepository,
                                      KernelInterface kernelInterface,
                                      WorkflowService workflowService,
                                      CurrentUser currentUser) {
        this.demandeAeronefRepository = demandeAeronefRepository;
        this.demandeAeronefQueryService = demandeAeronefQueryService;
        this.demandeAeronefMapper = demandeAeronefMapper;
        this.demandeAeronefInputMapper = demandeAeronefInputMapper;
        this.demandeAeronefOutputMapper = demandeAeronefOutputMapper;
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
    public DemandeAeronefDTO save(DemandeAeronefDTO dto) {
        if (dto.getId() != null) {
            Optional<DemandeAeronef> existant = demandeAeronefRepository.findById(dto.getId());
            if (existant.isPresent()) {
                DemandeAeronef gere = existant.get();
                demandeAeronefMapper.partialUpdate(gere, dto);
                rattacherEnfants(gere);
                return demandeAeronefMapper.toDto(demandeAeronefRepository.save(gere));
            }
        }
        DemandeAeronef entity = demandeAeronefMapper.toEntity(dto);
        rattacherEnfants(entity);
        return demandeAeronefMapper.toDto(demandeAeronefRepository.save(entity));
    }

    public DemandeAeronefDTO update(Long id, DemandeAeronefDTO dto) {
        DemandeAeronef entity = demandeAeronefRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(AeronefErrors.OBJECT_NOT_FOUND,
                        AeronefErrors.CLASS, AeronefErrors.OBJECT_NOT_FOUND));
        demandeAeronefMapper.partialUpdate(entity, dto);
        rattacherEnfants(entity);
        return demandeAeronefMapper.toDto(demandeAeronefRepository.save(entity));
    }

    public Page<DemandeAeronefDTO> findAll(DemandeAeronefCriteria criteria, Pageable pageable, Integer size) {
        return demandeAeronefQueryService.findByCriteria(criteria, pageable, size);
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
    public Optional<DemandeAeronefDTO> findOne(Long id) {
        return demandeAeronefRepository.findById(id).map(entity -> {
            DemandeAeronefDTO dto = demandeAeronefMapper.toDto(entity);
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
            AclClass aclClass = kernelInterface.getaclClassByClassName(DemandeAeronef.class.getName());
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
    public DemandeAeronefOutputDTO byId(Long id) {
        DemandeAeronef entity = demandeAeronefRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(AeronefErrors.OBJECT_NOT_FOUND,
                        AeronefErrors.CLASS, AeronefErrors.OBJECT_NOT_FOUND));

        AclClass aclClass = kernelInterface.getaclClassByClassName(DemandeAeronef.class.getName());
        if (aclClass == null) {
            throw new BadRequestAlertException(AeronefErrors.ACL_CLASS_NOT_FOUND,
                    AeronefErrors.CLASS, AeronefErrors.ACL_CLASS_NOT_FOUND);
        }

        String permission = kernelInterface.checkSecurity(
                aclClass.getSimpleName(), id, currentUser.getSid());

        if (Permission.NONE.name().equals(permission) && entity.getAclObjectIdentity() != null) {
            throw new BadRequestAlertException(AeronefErrors.OBJECT_NOT_AUTHORIZED,
                    AeronefErrors.CLASS, AeronefErrors.OBJECT_NOT_AUTHORIZED);
        }

        DemandeAeronefOutputDTO output = demandeAeronefOutputMapper.toDto(entity);
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
        DemandeAeronef entity = demandeAeronefRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(AeronefErrors.OBJECT_NOT_FOUND,
                        AeronefErrors.CLASS, AeronefErrors.OBJECT_NOT_FOUND));

        if (entity.getWfProcessID() != null) {
            throw new BadRequestAlertException(AeronefErrors.OBJECT_ENGAGED,
                    AeronefErrors.CLASS, AeronefErrors.OBJECT_ENGAGED);
        }
        // Une autorisation delivree est un document officiel : le dossier qui la
        // porte ne s'efface pas, meme si son circuit est termine.
        if (entity.getAttestations() != null && !entity.getAttestations().isEmpty()) {
            throw new BadRequestAlertException(AeronefErrors.OBJECT_ENGAGED,
                    AeronefErrors.CLASS, AeronefErrors.OBJECT_ENGAGED);
        }
        // Pas de garde par site : contrairement aux stations d'une demande
        // d'implantation, les sites d'un reseau ne portent pas de circuit propre.
        // Le reseau est autorise d'un bloc, et c'est le circuit du dossier -- teste
        // juste au-dessus -- qui protege la suppression.
        demandeAeronefRepository.delete(entity);
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
    public void exigerPourSoumission(DemandeAeronefInputDTO input) {
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

        // ----- les equipements de bord, rubrique 5
        //
        // Une station sans aucun equipement declare n'a rien a autoriser :
        // c'est le coeur technique du dossier.
        if (input.getEquipements() == null || input.getEquipements().isEmpty()) {
            manques.add("au moins un equipement de bord");
        } else {
            for (int i = 0; i < input.getEquipements().size(); i++) {
                EquipementBordAeronefDTO e = input.getEquipements().get(i);
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
                    AeronefErrors.CLASS, AeronefErrors.OBJECT_NOT_VALID);
        }
    }

    /** Vrai pour une chaine nulle, vide ou faite d'espaces. */
    private static boolean estVide(String v) {
        return v == null || v.trim().isEmpty();
    }

    private void exigerPiecesObligatoires(DemandeAeronefInputDTO input, AclClass aclClass) {
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
                    AeronefErrors.CLASS, AeronefErrors.PIECES_MANQUANTES);
        }
    }

    /**
     * Les frais de dossier, rubrique 9 du formulaire : un FORFAIT de
     * 200 000 FCFA par aeronef.
     *
     * Pas de grille par service comme le reseau -- l'annexe de ce formulaire
     * ne connait qu'un montant. Il est arrete au depot et conserve sur le
     * dossier : un bareme qui change ne doit pas reecrire un dossier deja
     * instruit.
     */
    private static final BigDecimal FRAIS_DOSSIER_AERONEF = new BigDecimal("200000");
    private static final String DEVISE_PAR_DEFAUT = "XAF";

    /**
     * Arrete les frais du dossier, une fois pour toutes.
     *
     * Ne fait rien si le montant est deja pose : un dossier repris garde celui
     * qui lui a ete reclame, meme si le bareme a change depuis.
     */
    void arreterFraisDossier(DemandeAeronefInputDTO input) {
        if (input.getFraisDossier() != null) {
            return;
        }
        input.setFraisDossier(FRAIS_DOSSIER_AERONEF);
        if (input.getDeviseFrais() == null) {
            input.setDeviseFrais(DEVISE_PAR_DEFAUT);
        }
    }

    public DemandeAeronefOutputDTO initAndSubmit(DemandeAeronefInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(AeronefErrors.ACL_CLASS_NOT_FOUND,
                    AeronefErrors.CLASS, AeronefErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);

        input.setCreatedDate(ZonedDateTime.now());
        input.setSendedDate(ZonedDateTime.now());
        arreterFraisDossier(input);

        DemandeAeronef entity = toEntityOrLoad(input);
        entity = demandeAeronefRepository.save(entity);

        DemandeAeronefOutputDTO output = demandeAeronefOutputMapper.toDto(entity);
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

        return demandeAeronefOutputMapper.toDto(entity);
    }

    /**
     * Transitions suivantes : le dossier existe et son instance de process tourne.
     */
    public DemandeAeronefOutputDTO submit(DemandeAeronefInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(AeronefErrors.ACL_CLASS_NOT_FOUND,
                    AeronefErrors.CLASS, AeronefErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);
        if (input.getId() == null) {
            throw new BadRequestAlertException(AeronefErrors.OBJECT_NOT_VALID,
                    AeronefErrors.CLASS, AeronefErrors.OBJECT_NOT_VALID);
        }

        DemandeAeronef entity = demandeAeronefRepository.findById(input.getId())
                .orElseThrow(() -> new BadRequestAlertException(AeronefErrors.OBJECT_NOT_FOUND,
                        AeronefErrors.CLASS, AeronefErrors.OBJECT_NOT_FOUND));

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
            throw new BadRequestAlertException(AeronefErrors.OBJECT_NOT_AUTHORIZED,
                    AeronefErrors.CLASS, AeronefErrors.OBJECT_NOT_AUTHORIZED);
        }

        demandeAeronefInputMapper.partialUpdate(entity, input);
        entity = demandeAeronefRepository.save(entity);

        DemandeAeronefOutputDTO output = demandeAeronefOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        BpmJob bpmJob = workflowService._nextTask(entity.getWfProcessID(),
                input.getDecision(), input.getWfComment(), output, aclClass);

        entity = appliquerRetourMoteur(bpmJob, aclClass);

        saveWorkflowCommentaire(input.getCommentaire(), entity, aclClass);

        return demandeAeronefOutputMapper.toDto(entity);
    }

    /**
     * Enregistrement sans franchir d'etape : le circuit n'est pas sollicite.
     */
    public DemandeAeronefOutputDTO saveAsDraft(DemandeAeronefInputDTO input, AclClass aclClass) {
        if (aclClass == null) {
            throw new BadRequestAlertException(AeronefErrors.ACL_CLASS_NOT_FOUND,
                    AeronefErrors.CLASS, AeronefErrors.ACL_CLASS_NOT_FOUND);
        }

        DemandeAeronef entity = toEntityOrLoad(input);
        entity = demandeAeronefRepository.save(entity);

        DemandeAeronefOutputDTO output = demandeAeronefOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        // le brouillon n'est visible que de son auteur tant qu'il n'est pas soumis
        entity = persistAndApplySecurity(entity,
                Arrays.asList(currentUser.getEmployeSid()), new ArrayList<>(), aclClass);

        return demandeAeronefOutputMapper.toDto(entity);
    }

    // ------------------------------------------------------------- internes

    private DemandeAeronef toEntityOrLoad(DemandeAeronefInputDTO input) {
        if (input.getId() != null) {
            Optional<DemandeAeronef> existant = demandeAeronefRepository.findById(input.getId());
            if (existant.isPresent()) {
                DemandeAeronef entity = existant.get();
                demandeAeronefInputMapper.partialUpdate(entity, input);
                return entity;
            }
        }
        return demandeAeronefInputMapper.toEntity(input);
    }

    /**
     * Reporte sur l'entite ce que le moteur a decide, puis rejoue la securite.
     */
    private DemandeAeronef appliquerRetourMoteur(BpmJob bpmJob, AclClass aclClass) throws Exception {
        DemandeAeronefOutputDTO output = (DemandeAeronefOutputDTO) bpmJob.getDataObject();

        // On repart de l'instance GEREE, jamais d'une entite reconstruite : le DTO
        // porterait une collection neuve, et le merge ferait lever Hibernate sur
        // orphanRemoval. Il ne porte pas non plus l'aclObjectIdentity.
        DemandeAeronef entity = output.getId() != null
                ? demandeAeronefRepository.findById(output.getId()).orElse(null)
                : null;
        if (entity == null) {
            entity = demandeAeronefOutputMapper.toEntity(output);
        } else {
            demandeAeronefOutputMapper.partialUpdate(entity, output);
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
    private DemandeAeronef persistAndApplySecurity(DemandeAeronef entity,
                                                        List<String> authors, List<String> readers,
                                                        AclClass aclClass) {
        if (entity.getId() != null) {
            demandeAeronefRepository.findById(entity.getId()).ifPresent(recent -> {
                if (entity.getAclObjectIdentity() == null) {
                    entity.setAclObjectIdentity(recent.getAclObjectIdentity());
                }
            });
        }

        rattacherEnfants(entity);

        DemandeAeronef persiste = demandeAeronefRepository.save(entity);

        try {
            if (authors != null && readers != null) {
                kernelInterface.applySecurity(aclClass.getClasse(), persiste.getId(), authors, readers,
                        new ArrayList<>(), null, null, persiste.getAclObjectIdentity() == null, false);
            }
            if (persiste.getAclObjectIdentity() == null) {
                persiste = setAclObjectIdentity(persiste, aclClass);
                persiste = demandeAeronefRepository.save(persiste);
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
     * Appele au premier depot, quand le circuit demarre : c'est la que
     * l'instruction commence, donc que le rapport a lieu d'exister. Il est cree
     * vide -- la conclusion est le travail de l'instructeur --, mais avec sa
     * classe ACL et sa reference, sans lesquelles il ne pourrait pas porter de
     * pieces.
     *
     * Il ne remplace pas les verifications de la rubrique 7, qui restent le
     * journal des controles -- voir RapportTechnique.
     *
     * Silencieux si la classe n'est pas declaree au kernel : un referentiel
     * incomplet ne doit pas faire echouer un depot. Le rapport sera ouvert au
     * depot suivant, une fois la classe declaree.
     */
    private void ouvrirRapportTechnique(DemandeAeronef entity) {
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
        rapport.setDemandeAeronef(entity);
        entity.setRapportTechnique(rapport);
    }

    private void rattacherEnfants(DemandeAeronef entity) {
        if (entity.getClient() != null) {
            entity.getClient().setDemandeAeronef(entity);
        }
        if (entity.getApplicant() != null) {

            entity.getApplicant().setDemandeAeronef(entity);

        }
        if (entity.getEquipements() != null) {
            entity.getEquipements().forEach(e -> e.setDemandeAeronef(entity));
        }
        if (entity.getVerifications() != null) {
            entity.getVerifications().forEach(v -> v.setDemandeAeronef(entity));
        }
        if (entity.getRapportTechnique() != null) {
            entity.getRapportTechnique().setDemandeAeronef(entity);
        }
        if (entity.getAttestations() != null) {
            entity.getAttestations().forEach(a -> a.setDemandeAeronef(entity));
        }
    }

    public DemandeAeronef setAclObjectIdentity(DemandeAeronef entity, AclClass aclClass) {
        Integer aclObjectIdentityID = kernelInterface.findACLObjectIdentity(aclClass.getId(), entity.getId());
        if (aclObjectIdentityID == null) {
            return entity;
        }
        AclObjectIdentity aclObjectIdentity = new AclObjectIdentity();
        aclObjectIdentity.setId(aclObjectIdentityID.longValue());
        entity.setAclObjectIdentity(aclObjectIdentity);
        return entity;
    }

    private void publierPiecesJointes(DemandeAeronefInputDTO input, DemandeAeronefOutputDTO output,
                                      DemandeAeronef entity, AclClass aclClass) {
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

    private void saveWorkflowCommentaire(String texte, DemandeAeronef entity, AclClass aclClass) {
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
