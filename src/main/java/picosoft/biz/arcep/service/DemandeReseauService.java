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
import picosoft.biz.arcep.controller.errors.ReseauErrors;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.domain.shared.Commentaire;
import picosoft.biz.arcep.repository.CommentaireRepository;
import picosoft.biz.arcep.repository.DemandeReseauRepository;
import picosoft.biz.arcep.service.criteria.DemandeReseauCriteria;
import picosoft.biz.arcep.domain.reseau.enumeration.*;
import picosoft.biz.arcep.service.dto.ApplicantDTO;
import picosoft.biz.arcep.service.dto.ClientDTO;
import picosoft.biz.arcep.service.dto.DemandeReseauDTO;

import picosoft.biz.arcep.service.dto.SiteReseauDTO;
import picosoft.biz.arcep.service.dto.TypeReseauDeclareDTO;
import picosoft.biz.arcep.service.dto.DemandeReseauInputDTO;
import picosoft.biz.arcep.service.dto.DemandeReseauOutputDTO;
import picosoft.biz.arcep.service.mapper.DemandeReseauInputMapper;
import picosoft.biz.arcep.service.mapper.DemandeReseauMapper;
import picosoft.biz.arcep.service.mapper.DemandeReseauOutputMapper;

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
public class DemandeReseauService {

    private final Logger log = LoggerFactory.getLogger(DemandeReseauService.class);

    private final DemandeReseauRepository demandeReseauRepository;
    private final DemandeReseauQueryService demandeReseauQueryService;
    private final DemandeReseauMapper demandeReseauMapper;
    private final DemandeReseauInputMapper demandeReseauInputMapper;
    private final DemandeReseauOutputMapper demandeReseauOutputMapper;
    private final CommentaireRepository commentaireRepository;
    private final KernelInterface kernelInterface;
    private final WorkflowService workflowService;
    private final CurrentUser currentUser;

    public DemandeReseauService(DemandeReseauRepository demandeReseauRepository,
                                      DemandeReseauQueryService demandeReseauQueryService,
                                      DemandeReseauMapper demandeReseauMapper,
                                      DemandeReseauInputMapper demandeReseauInputMapper,
                                      DemandeReseauOutputMapper demandeReseauOutputMapper,
                                      CommentaireRepository commentaireRepository,
                                      KernelInterface kernelInterface,
                                      WorkflowService workflowService,
                                      CurrentUser currentUser) {
        this.demandeReseauRepository = demandeReseauRepository;
        this.demandeReseauQueryService = demandeReseauQueryService;
        this.demandeReseauMapper = demandeReseauMapper;
        this.demandeReseauInputMapper = demandeReseauInputMapper;
        this.demandeReseauOutputMapper = demandeReseauOutputMapper;
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
    public DemandeReseauDTO save(DemandeReseauDTO dto) {
        if (dto.getId() != null) {
            Optional<DemandeReseau> existant = demandeReseauRepository.findById(dto.getId());
            if (existant.isPresent()) {
                DemandeReseau gere = existant.get();
                demandeReseauMapper.partialUpdate(gere, dto);
                rattacherEnfants(gere);
                return demandeReseauMapper.toDto(demandeReseauRepository.save(gere));
            }
        }
        DemandeReseau entity = demandeReseauMapper.toEntity(dto);
        rattacherEnfants(entity);
        return demandeReseauMapper.toDto(demandeReseauRepository.save(entity));
    }

    public DemandeReseauDTO update(Long id, DemandeReseauDTO dto) {
        DemandeReseau entity = demandeReseauRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(ReseauErrors.OBJECT_NOT_FOUND,
                        ReseauErrors.CLASS, ReseauErrors.OBJECT_NOT_FOUND));
        demandeReseauMapper.partialUpdate(entity, dto);
        rattacherEnfants(entity);
        return demandeReseauMapper.toDto(demandeReseauRepository.save(entity));
    }

    /**
     * Pose l'etape courante et note qui traite le dossier.
     *
     * Transpose d'AsiService.updateStep. Le circuit n'est PAS sollicite : c'est
     * un marquage, pas une transition. `sid` peut etre nul -- on retombe alors
     * sur le SID de l'employe courant.
     */
    public DemandeReseauDTO updateStep(Long id, Long step, String username, String sid) {
        DemandeReseau entity = demandeReseauRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(ReseauErrors.OBJECT_NOT_FOUND,
                        ReseauErrors.CLASS, ReseauErrors.OBJECT_NOT_FOUND));
        entity.setStep(step);
        entity.setAssignee(username);
        entity.setTraitedBy(username);
        entity.setSidTraitedBy(sid != null ? sid : currentUser.getEmployeSid());
        return demandeReseauMapper.toDto(demandeReseauRepository.save(entity));
    }

    public Page<DemandeReseauDTO> findAll(DemandeReseauCriteria criteria, Pageable pageable, Integer size) {
        return demandeReseauQueryService.findByCriteria(criteria, pageable, size);
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
    public Optional<DemandeReseauDTO> findOne(Long id) {
        AclClass aclClass = aclClassOuNull();
        return demandeReseauRepository.findById(id).map(entity -> {
            DemandeReseauDTO dto = demandeReseauMapper.toDto(entity);
            dto.setUserPermission(permissionSur(aclClass, entity.getId()));

            // L'IDENTITE ACL, et pas seulement la permission.
            //
            // `byId` la posait deja (plus bas, setClassId/setClassName) ; cette
            // lecture-ci ne la posait pas, et c'est elle que le formulaire
            // appelle -- le controleur sert `findOne` sur
            // GET /demande-reseaux/{id}, `byId` n'etant branche que sur la
            // forme au SINGULIER /demande-reseau/{id}, que personne n'appelle.
            //
            // Consequence mesuree le 24/09/2026 sur le dossier 21 : classId
            // arrivait null, `chargerAttachements()` sortait par sa garde
            // `if (!id || !classId)`, GetAllAttachement n'etait jamais appele,
            // et « Pieces du dossier » restait vide meme une fois les fichiers
            // publies. Une piece rattachee au kernel se demande par le couple
            // (classId, objectId) : sans le premier, le dossier ne sait pas
            // reclamer ses propres documents.
            //
            // Pose seulement si le kernel a repondu : on ne fabrique pas une
            // identite qu'on n'a pas, meme vide.
            if (aclClass != null) {
                dto.setClassId(aclClass.getId());
                dto.setClassName(aclClass.getClasse());
            }
            return dto;
        });
    }

    /**
     * La classe ACL du dossier reseau, ou null si le kernel se tait.
     *
     * Resolue UNE fois par lecture : `findOne` en a besoin deux fois -- pour
     * interroger checkSecurity et pour poser l'identite sur le DTO -- et deux
     * appels rendraient la meme reponse au prix d'un aller-retour de plus.
     */
    private AclClass aclClassOuNull() {
        try {
            return kernelInterface.getaclClassByClassName(DemandeReseau.class.getName());
        } catch (Exception e) {
            log.warn("classe ACL du dossier reseau indisponible : {}", e.toString());
            return null;
        }
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
    private String permissionSur(AclClass aclClass, Long id) {
        if (id == null || aclClass == null) { return null; }
        try {
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
    public DemandeReseauOutputDTO byId(Long id) {
        DemandeReseau entity = demandeReseauRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(ReseauErrors.OBJECT_NOT_FOUND,
                        ReseauErrors.CLASS, ReseauErrors.OBJECT_NOT_FOUND));

        AclClass aclClass = kernelInterface.getaclClassByClassName(DemandeReseau.class.getName());
        if (aclClass == null) {
            throw new BadRequestAlertException(ReseauErrors.ACL_CLASS_NOT_FOUND,
                    ReseauErrors.CLASS, ReseauErrors.ACL_CLASS_NOT_FOUND);
        }

        String permission = kernelInterface.checkSecurity(
                aclClass.getSimpleName(), id, currentUser.getSid());

        if (Permission.NONE.name().equals(permission) && entity.getAclObjectIdentity() != null) {
            throw new BadRequestAlertException(ReseauErrors.OBJECT_NOT_AUTHORIZED,
                    ReseauErrors.CLASS, ReseauErrors.OBJECT_NOT_AUTHORIZED);
        }

        DemandeReseauOutputDTO output = demandeReseauOutputMapper.toDto(entity);
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
        DemandeReseau entity = demandeReseauRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(ReseauErrors.OBJECT_NOT_FOUND,
                        ReseauErrors.CLASS, ReseauErrors.OBJECT_NOT_FOUND));

        if (entity.getWfProcessID() != null) {
            throw new BadRequestAlertException(ReseauErrors.OBJECT_ENGAGED,
                    ReseauErrors.CLASS, ReseauErrors.OBJECT_ENGAGED);
        }
        // Une autorisation delivree est un document officiel : le dossier qui la
        // porte ne s'efface pas, meme si son circuit est termine.
        if (entity.getAttestations() != null && !entity.getAttestations().isEmpty()) {
            throw new BadRequestAlertException(ReseauErrors.OBJECT_ENGAGED,
                    ReseauErrors.CLASS, ReseauErrors.OBJECT_ENGAGED);
        }
        // Pas de garde par site : contrairement aux stations d'une demande
        // d'implantation, les sites d'un reseau ne portent pas de circuit propre.
        // Le reseau est autorise d'un bloc, et c'est le circuit du dossier -- teste
        // juste au-dessus -- qui protege la suppression.
        demandeReseauRepository.delete(entity);
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
    public void exigerPourSoumission(DemandeReseauInputDTO input) {
        List<String> manques = new ArrayList<>();

        if (input.getNatureReseau() == null)  { manques.add("la nature du reseau (prive ou ouvert au public)"); }
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

        // Rubriques 1 et 3 : le requerant et le responsable du reseau sont la
        // MEME personne, sous deux titres. Un seul controle les couvre.
        exigerDemandeur(input, manques);

        if (input.getTypesReseau() == null || input.getTypesReseau().isEmpty()) {
            manques.add("au moins un type de reseau a exploiter");
        } else {
            // Une case « Autres » cochee sans texte ne dit rien a l'instructeur.
            boolean autreSansPrecision = input.getTypesReseau().stream()
                    .anyMatch(t -> t.getType() == TypeReseau.AUTRE && estVide(t.getPrecisionAutre()));
            if (autreSansPrecision) {
                manques.add("la precision du type de reseau « Autres »");
            }
        }

        if (input.getServices() == null || input.getServices().isEmpty()) {
            manques.add("au moins un type de service a exploiter");
        } else {
            // La telephonie se decline en portee ; les deux autres services non.
            boolean porteeManquante = input.getServices().stream()
                    .anyMatch(x -> (x.getType() == TypeService.TELEPHONIE
                                 || x.getType() == TypeService.TELEPHONIE_IP)
                                && x.getPortee() == null);
            if (porteeManquante) {
                manques.add("la portee des services de telephonie (locale, nationale ou internationale)");
            }
        }

        if (input.getSites() == null || input.getSites().isEmpty()) {
            manques.add("au moins un site d'implantation");
        } else {
            for (int i = 0; i < input.getSites().size(); i++) {
                SiteReseauDTO site = input.getSites().get(i);
                String ou = "site " + (i + 1);
                if (estVide(site.getNomSite()))  { manques.add(ou + " : le nom"); }
                if (estVide(site.getProvince())) { manques.add(ou + " : la province"); }
            }
        }

        if (!manques.isEmpty()) {
            throw new BadRequestAlertException(
                    "Le dossier ne peut pas etre soumis, il manque : " + String.join(", ", manques),
                    ReseauErrors.CLASS, ReseauErrors.OBJECT_NOT_VALID);
        }
    }

    /** Vrai pour une chaine nulle, vide ou faite d'espaces. */
    private static boolean estVide(String v) {
        return v == null || v.trim().isEmpty();
    }

    /**
     * Le demandeur : la structure et la personne qui demande pour elle.
     *
     * Meme jeu de champs que la rubrique « Demandeur » d'ASI, dont cette
     * rubrique est la transposition : la raison sociale et l'adresse du
     * titulaire (rubrique 2), l'identite et la qualite du requerant
     * (rubrique 1), et de quoi le joindre.
     *
     * La PIECE D'IDENTITE n'est plus exigee : `Applicant` ne la porte pas, et on
     * ne l'a pas ajoutee. Le document reste demande comme piece jointe.
     */
    private void exigerDemandeur(DemandeReseauInputDTO input, List<String> manques) {
        ClientDTO c = input.getClient();
        ApplicantDTO a = input.getApplicant();

        // L'identite du titulaire est deja exigee plus haut, et de facon plus
        // juste : `company` OU `clientName`, un particulier ayant le droit de
        // deposer. On ne verifie ici que ce qui n'y est pas.
        if (c == null || estVide(c.getAddress())) {
            manques.add("l'adresse du titulaire du reseau");
        }
        if (a == null || estVide(a.getApplicantName())) {
            manques.add("l'identite du requerant");
        }
        if (a == null || estVide(a.getQualification())) {
            manques.add("la qualite du requerant (fonction ou titre)");
        }
        if (a == null || estVide(a.getEmail())) {
            manques.add("l'adresse electronique du requerant");
        }
    }

    private void exigerPiecesObligatoires(DemandeReseauInputDTO input, AclClass aclClass) {
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
                    ReseauErrors.CLASS, ReseauErrors.PIECES_MANQUANTES);
        }
    }

    /** 300 000 FCFA pour un reseau PMR seul. */
    private static final BigDecimal FRAIS_PMR_SEUL = new BigDecimal("300000");
    /** 500 000 FCFA des qu'un autre type est declare. */
    private static final BigDecimal FRAIS_AUTRES = new BigDecimal("500000");
    private static final String DEVISE_PAR_DEFAUT = "XAF";

    /**
     * Arrete le montant des frais de dossier, au DEPOT et une seule fois.
     *
     * Le bareme est applique ICI, et nulle part ailleurs : le laisser au
     * navigateur reviendrait a laisser le demandeur fixer le prix de sa propre
     * demande. Le formulaire l'affiche pendant la saisie, mais a titre
     * indicatif -- c'est cette valeur-ci qui est conservee sur le dossier.
     *
     * Le montant n'est pose que s'il est absent : un dossier deja depose garde
     * le sien, faute de quoi une revision du bareme reecrirait le prix de
     * dossiers deja instruits, voire deja payes.
     *
     * `exigerPourSoumission` a deja garanti qu'au moins un type est declare.
     */
    void arreterFraisDossier(DemandeReseauInputDTO input) {
        if (input.getFraisDossier() != null) {
            return;
        }
        List<TypeReseau> types = (input.getTypesReseau() == null ? List.<TypeReseauDeclareDTO>of() : input.getTypesReseau())
                .stream()
                .map(TypeReseauDeclareDTO::getType)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        if (types.isEmpty()) {
            return;
        }
        boolean pmrSeul = types.size() == 1 && types.get(0) == TypeReseau.PMR;
        input.setFraisDossier(pmrSeul ? FRAIS_PMR_SEUL : FRAIS_AUTRES);

        if (estVide(input.getDeviseFrais())) {
            input.setDeviseFrais(DEVISE_PAR_DEFAUT);
        }
    }

    public DemandeReseauOutputDTO initAndSubmit(DemandeReseauInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(ReseauErrors.ACL_CLASS_NOT_FOUND,
                    ReseauErrors.CLASS, ReseauErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);

        input.setCreatedDate(ZonedDateTime.now());
        input.setSendedDate(ZonedDateTime.now());
        arreterFraisDossier(input);

        DemandeReseau entity = toEntityOrLoad(input);
        entity = demandeReseauRepository.save(entity);

        DemandeReseauOutputDTO output = demandeReseauOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        if (entity.getReference() == null) {
            entity.setReference(kernelInterface.getSequenceNumberByClass(
                    new JSONObject(output).toString(), aclClass.getClasse()));
            output.setReference(entity.getReference());
        }

        publierPiecesJointes(input, output, entity, aclClass);

        BpmJob bpmJob = workflowService._initAndNextTask(aclClass.getFwProcess(),
                input.getDecision(), input.getWfComment(), output, aclClass);

        entity = appliquerRetourMoteur(bpmJob, aclClass);

        saveWorkflowCommentaire(input.getCommentaire(), entity, aclClass);

        return demandeReseauOutputMapper.toDto(entity);
    }

    /**
     * Transitions suivantes : le dossier existe et son instance de process tourne.
     */
    public DemandeReseauOutputDTO submit(DemandeReseauInputDTO input, AclClass aclClass) throws Exception {
        if (aclClass == null) {
            throw new BadRequestAlertException(ReseauErrors.ACL_CLASS_NOT_FOUND,
                    ReseauErrors.CLASS, ReseauErrors.ACL_CLASS_NOT_FOUND);
        }
        if (input.getId() == null) {
            throw new BadRequestAlertException(ReseauErrors.OBJECT_NOT_VALID,
                    ReseauErrors.CLASS, ReseauErrors.OBJECT_NOT_VALID);
        }

        DemandeReseau entity = demandeReseauRepository.findById(input.getId())
                .orElseThrow(() -> new BadRequestAlertException(ReseauErrors.OBJECT_NOT_FOUND,
                        ReseauErrors.CLASS, ReseauErrors.OBJECT_NOT_FOUND));

        // Le dossier complet n'est exige qu'a la saisie -- premiere tache, ou
        // retour de la Numerotation. Aux etapes suivantes (Numerotation, Chef
        // Centre, Technique...) l'agent decide sur un dossier deja complet et
        // n'en renvoie pas forcement tout le graphe : comme isValidHomologation,
        // on ne rejoue pas la validation de depot.
        if (enSaisie(entity)) {
            exigerPourSoumission(input);
            exigerPiecesObligatoires(input, aclClass);
        }

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
            throw new BadRequestAlertException(ReseauErrors.OBJECT_NOT_AUTHORIZED,
                    ReseauErrors.CLASS, ReseauErrors.OBJECT_NOT_AUTHORIZED);
        }

        demandeReseauInputMapper.partialUpdate(entity, input);
        entity = demandeReseauRepository.save(entity);

        DemandeReseauOutputDTO output = demandeReseauOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        BpmJob bpmJob = workflowService._nextTask(entity.getWfProcessID(),
                input.getDecision(), input.getWfComment(), output, aclClass);

        entity = appliquerRetourMoteur(bpmJob, aclClass);

        saveWorkflowCommentaire(input.getCommentaire(), entity, aclClass);

        return demandeReseauOutputMapper.toDto(entity);
    }

    /**
     * Enregistrement sans franchir d'etape : le circuit n'est pas sollicite.
     */
    public DemandeReseauOutputDTO saveAsDraft(DemandeReseauInputDTO input, AclClass aclClass) {
        if (aclClass == null) {
            throw new BadRequestAlertException(ReseauErrors.ACL_CLASS_NOT_FOUND,
                    ReseauErrors.CLASS, ReseauErrors.ACL_CLASS_NOT_FOUND);
        }

        DemandeReseau entity = toEntityOrLoad(input);
        entity = demandeReseauRepository.save(entity);

        DemandeReseauOutputDTO output = demandeReseauOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        // Le brouillon n'est visible que de son auteur TANT QU'IL N'EST PAS SOUMIS.
        // Une fois le circuit demarre, les droits appartiennent au moteur : les
        // reecrire ici retirerait le groupe de la tache en cours (ex. ChefCentre)
        // et le dossier sortirait de ses listes « en cours ». Meme garde que
        // HomologationService.saveHomologationAsDraft (wfProcessID == null).
        boolean circuitDemarre = entity.getWfProcessID() != null;
        entity = persistAndApplySecurity(entity,
                circuitDemarre ? null : Arrays.asList(currentUser.getEmployeSid()),
                circuitDemarre ? null : new ArrayList<>(), aclClass);

        return demandeReseauOutputMapper.toDto(entity);
    }

    // ------------------------------------------------------------- internes

    /** Cle de la tache de saisie dans processReseau.bpmn20.xml. */
    private static final String TACHE_SAISIE = "Task_saisie";

    /** Vrai si la tache active du dossier est la saisie (ou s'il n'a pas de tache active connue). */
    private boolean enSaisie(DemandeReseau entity) {
        if (entity.getWfProcessID() == null) {
            return true;
        }
        org.flowable.task.api.Task tache = workflowService.getActifTaskOfProcessInstance(entity.getWfProcessID());
        return tache == null || TACHE_SAISIE.equals(tache.getTaskDefinitionKey());
    }

    private DemandeReseau toEntityOrLoad(DemandeReseauInputDTO input) {
        if (input.getId() != null) {
            Optional<DemandeReseau> existant = demandeReseauRepository.findById(input.getId());
            if (existant.isPresent()) {
                DemandeReseau entity = existant.get();
                demandeReseauInputMapper.partialUpdate(entity, input);
                return entity;
            }
        }
        return demandeReseauInputMapper.toEntity(input);
    }

    /**
     * Reporte sur l'entite ce que le moteur a decide, puis rejoue la securite.
     */
    private DemandeReseau appliquerRetourMoteur(BpmJob bpmJob, AclClass aclClass) throws Exception {
        DemandeReseauOutputDTO output = (DemandeReseauOutputDTO) bpmJob.getDataObject();

        // On repart de l'instance GEREE, jamais d'une entite reconstruite : le DTO
        // porterait une collection neuve, et le merge ferait lever Hibernate sur
        // orphanRemoval. Il ne porte pas non plus l'aclObjectIdentity.
        DemandeReseau entity = output.getId() != null
                ? demandeReseauRepository.findById(output.getId()).orElse(null)
                : null;
        if (entity == null) {
            entity = demandeReseauOutputMapper.toEntity(output);
        } else {
            demandeReseauOutputMapper.partialUpdate(entity, output);
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
    private DemandeReseau persistAndApplySecurity(DemandeReseau entity,
                                                        List<String> authors, List<String> readers,
                                                        AclClass aclClass) {
        if (entity.getId() != null) {
            demandeReseauRepository.findById(entity.getId()).ifPresent(recent -> {
                if (entity.getAclObjectIdentity() == null) {
                    entity.setAclObjectIdentity(recent.getAclObjectIdentity());
                }
            });
        }

        rattacherEnfants(entity);

        DemandeReseau persiste = demandeReseauRepository.save(entity);

        try {
            if (authors != null && readers != null) {
                kernelInterface.applySecurity(aclClass.getClasse(), persiste.getId(), authors, readers,
                        new ArrayList<>(), null, null, persiste.getAclObjectIdentity() == null, false);
            }
            if (persiste.getAclObjectIdentity() == null) {
                persiste = setAclObjectIdentity(persiste, aclClass);
                persiste = demandeReseauRepository.save(persiste);
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
    private void rattacherEnfants(DemandeReseau entity) {
        if (entity.getClient() != null) {
            entity.getClient().setDemandeReseau(entity);
        }
        if (entity.getApplicant() != null) {

            entity.getApplicant().setDemandeReseau(entity);

        }
        if (entity.getTypesReseau() != null) {
            entity.getTypesReseau().forEach(t -> t.setDemandeReseau(entity));
        }
        if (entity.getServices() != null) {
            entity.getServices().forEach(x -> x.setDemandeReseau(entity));
        }
        if (entity.getSites() != null) {
            entity.getSites().forEach(x -> x.setDemandeReseau(entity));
        }
        if (entity.getLiaisons() != null) {
            entity.getLiaisons().forEach(l -> l.setDemandeReseau(entity));
        }
        if (entity.getAttestations() != null) {
            entity.getAttestations().forEach(a -> a.setDemandeReseau(entity));
        }
    }

    public DemandeReseau setAclObjectIdentity(DemandeReseau entity, AclClass aclClass) {
        Integer aclObjectIdentityID = kernelInterface.findACLObjectIdentity(aclClass.getId(), entity.getId());
        if (aclObjectIdentityID == null) {
            return entity;
        }
        AclObjectIdentity aclObjectIdentity = new AclObjectIdentity();
        aclObjectIdentity.setId(aclObjectIdentityID.longValue());
        entity.setAclObjectIdentity(aclObjectIdentity);
        return entity;
    }

    private void publierPiecesJointes(DemandeReseauInputDTO input, DemandeReseauOutputDTO output,
                                      DemandeReseau entity, AclClass aclClass) {
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

    private void saveWorkflowCommentaire(String texte, DemandeReseau entity, AclClass aclClass) {
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
