package picosoft.biz.arcep.service;

import picosoft.biz.arcep.client.kernel.model.objects.AttachementInputDTO;
import picosoft.biz.arcep.client.kernel.model.global.GetRequestFileDefinitionDTO;
import picosoft.biz.arcep.client.kernel.model.global.AclClassFilesDto;
import java.util.Set;
import java.util.HashSet;
import picosoft.biz.arcep.service.dto.StationDTO;
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
import picosoft.biz.arcep.controller.errors.ImplantationErrors;
import picosoft.biz.arcep.domain.implantation.DemandeImplantation;
import picosoft.biz.arcep.domain.implantation.Station;
import picosoft.biz.arcep.domain.shared.Commentaire;
import picosoft.biz.arcep.repository.CommentaireRepository;
import picosoft.biz.arcep.repository.DemandeImplantationRepository;
import picosoft.biz.arcep.service.criteria.DemandeImplantationCriteria;
import picosoft.biz.arcep.service.dto.DemandeImplantationDTO;
import picosoft.biz.arcep.service.dto.DemandeImplantationInputDTO;
import picosoft.biz.arcep.service.dto.DemandeImplantationOutputDTO;
import picosoft.biz.arcep.service.mapper.DemandeImplantationInputMapper;
import picosoft.biz.arcep.service.mapper.DemandeImplantationMapper;
import picosoft.biz.arcep.service.mapper.DemandeImplantationOutputMapper;

import javax.transaction.Transactional;
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
public class DemandeImplantationService {

    private final Logger log = LoggerFactory.getLogger(DemandeImplantationService.class);

    private final DemandeImplantationRepository demandeImplantationRepository;
    private final DemandeImplantationQueryService demandeImplantationQueryService;
    private final DemandeImplantationMapper demandeImplantationMapper;
    private final DemandeImplantationInputMapper demandeImplantationInputMapper;
    private final DemandeImplantationOutputMapper demandeImplantationOutputMapper;
    private final CommentaireRepository commentaireRepository;
    private final KernelInterface kernelInterface;
    private final WorkflowService workflowService;
    private final CurrentUser currentUser;

    public DemandeImplantationService(DemandeImplantationRepository demandeImplantationRepository,
                                      DemandeImplantationQueryService demandeImplantationQueryService,
                                      DemandeImplantationMapper demandeImplantationMapper,
                                      DemandeImplantationInputMapper demandeImplantationInputMapper,
                                      DemandeImplantationOutputMapper demandeImplantationOutputMapper,
                                      CommentaireRepository commentaireRepository,
                                      KernelInterface kernelInterface,
                                      WorkflowService workflowService,
                                      CurrentUser currentUser) {
        this.demandeImplantationRepository = demandeImplantationRepository;
        this.demandeImplantationQueryService = demandeImplantationQueryService;
        this.demandeImplantationMapper = demandeImplantationMapper;
        this.demandeImplantationInputMapper = demandeImplantationInputMapper;
        this.demandeImplantationOutputMapper = demandeImplantationOutputMapper;
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
    public DemandeImplantationDTO save(DemandeImplantationDTO dto) {
        if (dto.getId() != null) {
            Optional<DemandeImplantation> existant = demandeImplantationRepository.findById(dto.getId());
            if (existant.isPresent()) {
                DemandeImplantation gere = existant.get();
                demandeImplantationMapper.partialUpdate(gere, dto);
                rattacherEnfants(gere);
                return demandeImplantationMapper.toDto(demandeImplantationRepository.save(gere));
            }
        }
        DemandeImplantation entity = demandeImplantationMapper.toEntity(dto);
        rattacherEnfants(entity);
        return demandeImplantationMapper.toDto(demandeImplantationRepository.save(entity));
    }

    public DemandeImplantationDTO update(Long id, DemandeImplantationDTO dto) {
        DemandeImplantation entity = demandeImplantationRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(ImplantationErrors.OBJECT_NOT_FOUND,
                        ImplantationErrors.CLASS, ImplantationErrors.OBJECT_NOT_FOUND));
        demandeImplantationMapper.partialUpdate(entity, dto);
        rattacherEnfants(entity);
        return demandeImplantationMapper.toDto(demandeImplantationRepository.save(entity));
    }

    public Page<DemandeImplantationDTO> findAll(DemandeImplantationCriteria criteria, Pageable pageable, Integer size) {
        return demandeImplantationQueryService.findByCriteria(criteria, pageable, size);
    }

    public Optional<DemandeImplantationDTO> findOne(Long id) {
        return demandeImplantationRepository.findById(id).map(demandeImplantationMapper::toDto);
    }

    public DemandeImplantationOutputDTO byId(Long id) {
        DemandeImplantation entity = demandeImplantationRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(ImplantationErrors.OBJECT_NOT_FOUND,
                        ImplantationErrors.CLASS, ImplantationErrors.OBJECT_NOT_FOUND));
        return demandeImplantationOutputMapper.toDto(entity);
    }

    /**
     * Supprime le dossier et ses enfants structurels (client, demandeur, station,
     * site, frequences) par cascade.
     *
     * Refuse si le dossier est engage : un circuit demarre laisse une instance
     * Flowable et des entrees ACL derriere lui, et une autorisation delivree est
     * un document qui ne doit pas disparaitre en silence.
     */
    public void delete(Long id) {
        DemandeImplantation entity = demandeImplantationRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(ImplantationErrors.OBJECT_NOT_FOUND,
                        ImplantationErrors.CLASS, ImplantationErrors.OBJECT_NOT_FOUND));

        if (entity.getWfProcessID() != null) {
            throw new BadRequestAlertException(ImplantationErrors.OBJECT_ENGAGED,
                    ImplantationErrors.CLASS, ImplantationErrors.OBJECT_ENGAGED);
        }
        Station station = entity.getStation();
        if (station != null
                && (station.getWfProcessID() != null
                    || (station.getAttestations() != null && !station.getAttestations().isEmpty()))) {
            throw new BadRequestAlertException(ImplantationErrors.OBJECT_ENGAGED,
                    ImplantationErrors.CLASS, ImplantationErrors.OBJECT_ENGAGED);
        }
        demandeImplantationRepository.delete(entity);
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
    public void exigerPourSoumission(DemandeImplantationInputDTO input) {
        List<String> manques = new ArrayList<>();

        if (estVide(input.getTypeDossier())) { manques.add("le type de dossier"); }
        if (input.getClient() == null || estVide(input.getClient().getCompany())) {
            manques.add("la raison sociale de l'operateur");
        }
        if (!Acteurs.nomme(input.getApplicants(), Acteurs.REQUERANT)) {
            manques.add("le nom du demandeur");
        }

        // UNE AUTORISATION VISE UNE SEULE STATION : la relation est un-a-un, il
        // n'y a plus rien a compter ni a rejeter. Le controle de cardinalite qui
        // se trouvait ici est devenu impossible a enfreindre par construction.
        StationDTO st = input.getStation();
        if (st == null) {
            manques.add("la station");
        } else {
            // La reference de la station n'est PAS exigee : le kernel l'attribue
            // au demarrage du circuit de la station, bien apres la soumission.
            // L'exiger ici rendait toute soumission impossible.
            if (st.getTypeStation() == null)         { manques.add("la station : le type de station"); }
            if (st.getNatureImplantation() == null)  { manques.add("la station : la nature de l'implantation"); }
            if (st.getSiteImplantation() == null
                    || estVide(st.getSiteImplantation().getNomSite())) {
                manques.add("la station : le nom du site");
            }
        }

        if (!manques.isEmpty()) {
            throw new BadRequestAlertException(
                    "Le dossier ne peut pas etre soumis, il manque : " + String.join(", ", manques),
                    ImplantationErrors.CLASS, ImplantationErrors.OBJECT_NOT_VALID);
        }
    }

    private static boolean estVide(String v) {
        return v == null || v.trim().isEmpty();
    }

    /**
     * Verifie que toute piece declaree OBLIGATOIRE par le referentiel est fournie.
     *
     * La liste n'est pas ecrite ici : elle vient du kernel, qui la tient par classe
     * ACL. La modifier se fait donc dans le referentiel, sans toucher au code ni
     * redeployer -- c'est tout l'interet de l'y avoir mise.
     *
     *
     * DEUX SOURCES DE COUVERTURE, ET ELLES N'ONT PAS LE MEME POIDS
     *
     * Les pieces de la requete sont verifiees par le serveur lui-meme. Celles deja
     * publiees lors d'un enregistrement precedent ne s'y trouvent pas, et le seul
     * moyen de les connaitre ici est labelsPostAttachments, que le CLIENT renseigne.
     * Un client qui mentirait sur ce champ franchirait donc la garde.
     *
     * Fermer cette faille demande de lire les pieces publiees directement aupres du
     * kernel : GetAllAttachement le permet, mais exige un fileAccessToken dont ce
     * service ne dispose d'aucune source. A traiter quand ce jeton sera disponible.
     *
     *
     * Un referentiel injoignable ne BLOQUE PAS la soumission : refuser un dossier
     * complet parce que le kernel ne repond pas punirait l'usager d'une panne
     * d'infrastructure. L'incident est journalise, la soumission passe.
     */
    private void exigerPiecesObligatoires(DemandeImplantationInputDTO input, AclClass aclClass) {
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
                    ImplantationErrors.CLASS, ImplantationErrors.PIECES_MANQUANTES);
        }
    }

    public DemandeImplantationOutputDTO initAndSubmit(DemandeImplantationInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(ImplantationErrors.ACL_CLASS_NOT_FOUND,
                    ImplantationErrors.CLASS, ImplantationErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);

        input.setCreatedDate(ZonedDateTime.now());
        input.setSendedDate(ZonedDateTime.now());

        DemandeImplantation entity = toEntityOrLoad(input);
        entity = demandeImplantationRepository.save(entity);

        DemandeImplantationOutputDTO output = demandeImplantationOutputMapper.toDto(entity);
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

        return demandeImplantationOutputMapper.toDto(entity);
    }

    /**
     * Transitions suivantes : le dossier existe et son instance de process tourne.
     */
    public DemandeImplantationOutputDTO submit(DemandeImplantationInputDTO input, AclClass aclClass) throws Exception {
        exigerPourSoumission(input);
        if (aclClass == null) {
            throw new BadRequestAlertException(ImplantationErrors.ACL_CLASS_NOT_FOUND,
                    ImplantationErrors.CLASS, ImplantationErrors.ACL_CLASS_NOT_FOUND);
        }
        exigerPiecesObligatoires(input, aclClass);
        if (input.getId() == null) {
            throw new BadRequestAlertException(ImplantationErrors.OBJECT_NOT_VALID,
                    ImplantationErrors.CLASS, ImplantationErrors.OBJECT_NOT_VALID);
        }

        DemandeImplantation entity = demandeImplantationRepository.findById(input.getId())
                .orElseThrow(() -> new BadRequestAlertException(ImplantationErrors.OBJECT_NOT_FOUND,
                        ImplantationErrors.CLASS, ImplantationErrors.OBJECT_NOT_FOUND));

        demandeImplantationInputMapper.partialUpdate(entity, input);
        entity = demandeImplantationRepository.save(entity);

        DemandeImplantationOutputDTO output = demandeImplantationOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        BpmJob bpmJob = workflowService._nextTask(entity.getWfProcessID(),
                input.getDecision(), input.getWfComment(), output, aclClass);

        entity = appliquerRetourMoteur(bpmJob, aclClass);

        saveWorkflowCommentaire(input.getCommentaire(), entity, aclClass);

        return demandeImplantationOutputMapper.toDto(entity);
    }

    /**
     * Enregistrement sans franchir d'etape : le circuit n'est pas sollicite.
     */
    public DemandeImplantationOutputDTO saveAsDraft(DemandeImplantationInputDTO input, AclClass aclClass) {
        if (aclClass == null) {
            throw new BadRequestAlertException(ImplantationErrors.ACL_CLASS_NOT_FOUND,
                    ImplantationErrors.CLASS, ImplantationErrors.ACL_CLASS_NOT_FOUND);
        }

        DemandeImplantation entity = toEntityOrLoad(input);
        entity = demandeImplantationRepository.save(entity);

        DemandeImplantationOutputDTO output = demandeImplantationOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        // le brouillon n'est visible que de son auteur tant qu'il n'est pas soumis
        entity = persistAndApplySecurity(entity,
                Arrays.asList(currentUser.getEmployeSid()), new ArrayList<>(), aclClass);

        return demandeImplantationOutputMapper.toDto(entity);
    }

    // ------------------------------------------------------------- internes

    private DemandeImplantation toEntityOrLoad(DemandeImplantationInputDTO input) {
        if (input.getId() != null) {
            Optional<DemandeImplantation> existant = demandeImplantationRepository.findById(input.getId());
            if (existant.isPresent()) {
                DemandeImplantation entity = existant.get();
                demandeImplantationInputMapper.partialUpdate(entity, input);
                return entity;
            }
        }
        return demandeImplantationInputMapper.toEntity(input);
    }

    /**
     * Reporte sur l'entite ce que le moteur a decide, puis rejoue la securite.
     */
    private DemandeImplantation appliquerRetourMoteur(BpmJob bpmJob, AclClass aclClass) throws Exception {
        DemandeImplantationOutputDTO output = (DemandeImplantationOutputDTO) bpmJob.getDataObject();

        // On repart de l'instance GEREE, jamais d'une entite reconstruite : le DTO
        // porterait une collection neuve, et le merge ferait lever Hibernate sur
        // orphanRemoval. Il ne porte pas non plus l'aclObjectIdentity.
        DemandeImplantation entity = output.getId() != null
                ? demandeImplantationRepository.findById(output.getId()).orElse(null)
                : null;
        if (entity == null) {
            entity = demandeImplantationOutputMapper.toEntity(output);
        } else {
            demandeImplantationOutputMapper.partialUpdate(entity, output);
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
    private DemandeImplantation persistAndApplySecurity(DemandeImplantation entity,
                                                        List<String> authors, List<String> readers,
                                                        AclClass aclClass) {
        if (entity.getId() != null) {
            demandeImplantationRepository.findById(entity.getId()).ifPresent(recent -> {
                if (entity.getAclObjectIdentity() == null) {
                    entity.setAclObjectIdentity(recent.getAclObjectIdentity());
                }
            });
        }

        rattacherEnfants(entity);

        DemandeImplantation persiste = demandeImplantationRepository.save(entity);

        try {
            if (authors != null && readers != null) {
                kernelInterface.applySecurity(aclClass.getClasse(), persiste.getId(), authors, readers,
                        new ArrayList<>(), null, null, persiste.getAclObjectIdentity() == null, false);
            }
            if (persiste.getAclObjectIdentity() == null) {
                persiste = setAclObjectIdentity(persiste, aclClass);
                persiste = demandeImplantationRepository.save(persiste);
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
    private void rattacherEnfants(DemandeImplantation entity) {
        if (entity.getClient() != null) {
            entity.getClient().setDemandeImplantation(entity);
        }
        if (entity.getApplicants() != null) {
            entity.getApplicants().forEach(a -> a.setDemandeImplantation(entity));
        }
        Station station = entity.getStation();
        if (station != null) {
            station.setDemandeImplantation(entity);
            if (station.getSiteImplantation() != null) {
                station.getSiteImplantation().setStation(station);
            }
            if (station.getFrequences() != null) {
                station.getFrequences().forEach(f -> f.setStation(station));
            }
            if (station.getAttestations() != null) {
                station.getAttestations().forEach(a -> a.setStation(station));
            }
        }
    }

    public DemandeImplantation setAclObjectIdentity(DemandeImplantation entity, AclClass aclClass) {
        Integer aclObjectIdentityID = kernelInterface.findACLObjectIdentity(aclClass.getId(), entity.getId());
        if (aclObjectIdentityID == null) {
            return entity;
        }
        AclObjectIdentity aclObjectIdentity = new AclObjectIdentity();
        aclObjectIdentity.setId(aclObjectIdentityID.longValue());
        entity.setAclObjectIdentity(aclObjectIdentity);
        return entity;
    }

    private void publierPiecesJointes(DemandeImplantationInputDTO input, DemandeImplantationOutputDTO output,
                                      DemandeImplantation entity, AclClass aclClass) {
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

    private void saveWorkflowCommentaire(String texte, DemandeImplantation entity, AclClass aclClass) {
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
