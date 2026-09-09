package picosoft.biz.arcep.service;

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
import picosoft.biz.arcep.controller.errors.StationErrors;
import picosoft.biz.arcep.domain.implantation.DemandeImplantation;
import picosoft.biz.arcep.domain.implantation.Frequence;
import picosoft.biz.arcep.domain.implantation.Station;
import picosoft.biz.arcep.domain.implantation.enumeration.StatutStation;
import picosoft.biz.arcep.domain.shared.Attestation;
import picosoft.biz.arcep.domain.shared.Commentaire;
import picosoft.biz.arcep.repository.CommentaireRepository;
import picosoft.biz.arcep.repository.DemandeImplantationRepository;
import picosoft.biz.arcep.repository.StationRepository;
import picosoft.biz.arcep.service.criteria.StationCriteria;
import picosoft.biz.arcep.service.dto.StationDTO;
import picosoft.biz.arcep.service.dto.StationInputDTO;
import picosoft.biz.arcep.service.dto.StationOutputDTO;
import picosoft.biz.arcep.service.mapper.StationInputMapper;
import picosoft.biz.arcep.service.mapper.StationMapper;
import picosoft.biz.arcep.service.mapper.StationOutputMapper;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service metier de la station, qui porte son propre circuit (processStation).
 *
 * Meme transaction canonique que le dossier parent. La difference tient a
 * l'eclatement : une station devient un circuit enfant des que son statut le
 * demande, et l'autorisation d'implantation est delivree par station, sur le
 * circuit enfant, par un listener du diagramme.
 */
@Service
@Transactional
public class StationService {

    private final Logger log = LoggerFactory.getLogger(StationService.class);

    private final StationRepository stationRepository;
    private final StationQueryService stationQueryService;
    private final StationMapper stationMapper;
    private final StationInputMapper stationInputMapper;
    private final StationOutputMapper stationOutputMapper;
    private final DemandeImplantationRepository demandeImplantationRepository;
    private final CommentaireRepository commentaireRepository;
    private final KernelInterface kernelInterface;
    private final WorkflowService workflowService;
    private final CurrentUser currentUser;

    public StationService(StationRepository stationRepository,
                          StationQueryService stationQueryService,
                          StationMapper stationMapper,
                          StationInputMapper stationInputMapper,
                          StationOutputMapper stationOutputMapper,
                          DemandeImplantationRepository demandeImplantationRepository,
                          CommentaireRepository commentaireRepository,
                          KernelInterface kernelInterface,
                          WorkflowService workflowService,
                          CurrentUser currentUser) {
        this.stationRepository = stationRepository;
        this.stationQueryService = stationQueryService;
        this.stationMapper = stationMapper;
        this.stationInputMapper = stationInputMapper;
        this.stationOutputMapper = stationOutputMapper;
        this.demandeImplantationRepository = demandeImplantationRepository;
        this.commentaireRepository = commentaireRepository;
        this.kernelInterface = kernelInterface;
        this.workflowService = workflowService;
        this.currentUser = currentUser;
    }

    // ------------------------------------------------------------------ CRUD

    /** Meme regle que le dossier : si l'id existe, on charge l'instance geree. */
    public StationDTO save(StationDTO dto) {
        if (dto.getId() != null) {
            Optional<Station> existant = stationRepository.findById(dto.getId());
            if (existant.isPresent()) {
                Station gere = existant.get();
                stationMapper.partialUpdate(gere, dto);
                rattacherEnfants(gere);
                return stationMapper.toDto(stationRepository.save(gere));
            }
        }
        Station entity = stationMapper.toEntity(dto);
        rattacherEnfants(entity);
        return stationMapper.toDto(stationRepository.save(entity));
    }

    public StationDTO update(Long id, StationDTO dto) {
        Station entity = stationRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(StationErrors.OBJECT_NOT_FOUND,
                        StationErrors.CLASS, StationErrors.OBJECT_NOT_FOUND));
        stationMapper.partialUpdate(entity, dto);
        rattacherEnfants(entity);
        return stationMapper.toDto(stationRepository.save(entity));
    }

    public Page<StationDTO> findAll(StationCriteria criteria, Pageable pageable, Integer size) {
        return stationQueryService.findByCriteria(criteria, pageable, size);
    }

    public Optional<StationDTO> findOne(Long id) {
        return stationRepository.findById(id).map(stationMapper::toDto);
    }

    public List<StationDTO> findByDemandeImplantation(Long demandeImplantationId) {
        return stationMapper.toDto(stationRepository.findByDemandeImplantationId(demandeImplantationId));
    }

    public StationOutputDTO byId(Long id) {
        Station entity = stationRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(StationErrors.OBJECT_NOT_FOUND,
                        StationErrors.CLASS, StationErrors.OBJECT_NOT_FOUND));
        return stationOutputMapper.toDto(entity);
    }

    /** Meme regle que le dossier : refus si le circuit est demarre ou l'autorisation delivree. */
    public void delete(Long id) {
        Station entity = stationRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(StationErrors.OBJECT_NOT_FOUND,
                        StationErrors.CLASS, StationErrors.OBJECT_NOT_FOUND));

        if (entity.getWfProcessID() != null
                || (entity.getAttestations() != null && !entity.getAttestations().isEmpty())) {
            throw new BadRequestAlertException(StationErrors.OBJECT_ENGAGED,
                    StationErrors.CLASS, StationErrors.OBJECT_ENGAGED);
        }
        stationRepository.delete(entity);
    }

    // -------------------------------------------------------------- workflow

    /**
     * Demarre le circuit enfant pour une station donnee.
     */
    public StationOutputDTO initAndSubmit(StationInputDTO input, AclClass aclClass) throws Exception {
        if (aclClass == null) {
            throw new BadRequestAlertException(StationErrors.ACL_CLASS_NOT_FOUND,
                    StationErrors.CLASS, StationErrors.ACL_CLASS_NOT_FOUND);
        }

        Station entity = toEntityOrLoad(input);
        if (entity.getStatutStation() == null) {
            entity.setStatutStation(StatutStation.CREATED);
        }
        entity = stationRepository.save(entity);

        StationOutputDTO output = stationOutputMapper.toDto(entity);
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

        return stationOutputMapper.toDto(entity);
    }

    public StationOutputDTO submit(StationInputDTO input, AclClass aclClass) throws Exception {
        if (aclClass == null) {
            throw new BadRequestAlertException(StationErrors.ACL_CLASS_NOT_FOUND,
                    StationErrors.CLASS, StationErrors.ACL_CLASS_NOT_FOUND);
        }
        if (input.getId() == null) {
            throw new BadRequestAlertException(StationErrors.OBJECT_NOT_VALID,
                    StationErrors.CLASS, StationErrors.OBJECT_NOT_VALID);
        }

        Station entity = stationRepository.findById(input.getId())
                .orElseThrow(() -> new BadRequestAlertException(StationErrors.OBJECT_NOT_FOUND,
                        StationErrors.CLASS, StationErrors.OBJECT_NOT_FOUND));

        stationInputMapper.partialUpdate(entity, input);
        entity = stationRepository.save(entity);

        StationOutputDTO output = stationOutputMapper.toDto(entity);
        output.setClassId(aclClass.getId());
        output.setClassName(aclClass.getClasse());

        publierPiecesJointes(input, output, entity, aclClass);

        BpmJob bpmJob = workflowService._nextTask(entity.getWfProcessID(),
                input.getDecision(), input.getWfComment(), output, aclClass);

        entity = appliquerRetourMoteur(bpmJob, aclClass);

        saveWorkflowCommentaire(input.getCommentaire(), entity, aclClass);

        return stationOutputMapper.toDto(entity);
    }

    /**
     * Eclate un dossier parent en circuits enfants : une instance de processStation
     * par station encore au statut initial. Le pendant de initHomologationFromAsi.
     */
    /**
     * Point d'entree du DIAGRAMME : appele par le circuit parent a la sortie de
     * l'etude technique, par
     *     ${stationService.initFromDemande(data.id)}
     *
     * Resout lui-meme l'AclClass, que le moteur ne peut pas lui transmettre.
     * Aucun appelant Java : renommer cette methode casse le process a l'execution,
     * pas a la compilation.
     *
     * Idempotent par construction -- les stations dont le circuit tourne deja sont
     * ignorees. C'est indispensable : le flux porteur est re-traversable par la
     * boucle "Retourner" du circuit parent.
     *
     * L'exception n'est PAS avalee : si les circuits enfants ne peuvent pas demarrer,
     * la transition du parent doit echouer bruyamment plutot que de laisser un dossier
     * valide sans aucune station instruite.
     */
    public List<StationOutputDTO> initFromDemande(Long demandeImplantationId) throws Exception {
        return initFromDemande(demandeImplantationId,
                kernelInterface.getaclClassByClassName(Station.class.getName()));
    }

    public List<StationOutputDTO> initFromDemande(Long demandeImplantationId, AclClass aclClass) throws Exception {
        if (aclClass == null) {
            throw new BadRequestAlertException(StationErrors.ACL_CLASS_NOT_FOUND,
                    StationErrors.CLASS, StationErrors.ACL_CLASS_NOT_FOUND);
        }

        DemandeImplantation demande = demandeImplantationRepository.findById(demandeImplantationId)
                .orElseThrow(() -> new BadRequestAlertException(StationErrors.OBJECT_NOT_FOUND,
                        StationErrors.CLASS, StationErrors.OBJECT_NOT_FOUND));

        List<StationOutputDTO> demarrees = new ArrayList<>();
        List<Station> stations = stationRepository.findByDemandeImplantationId(demande.getId());

        for (Station station : stations) {
            if (station.getWfProcessID() != null) {
                continue; // circuit deja demarre
            }
            StationInputDTO input = new StationInputDTO();
            input.setId(station.getId());
            demarrees.add(initAndSubmit(input, aclClass));
        }
        return demarrees;
    }

    // ------------------------------------------------------------- internes

    private Station toEntityOrLoad(StationInputDTO input) {
        if (input.getId() != null) {
            Optional<Station> existant = stationRepository.findById(input.getId());
            if (existant.isPresent()) {
                Station entity = existant.get();
                stationInputMapper.partialUpdate(entity, input);
                return entity;
            }
        }
        return stationInputMapper.toEntity(input);
    }

    private Station appliquerRetourMoteur(BpmJob bpmJob, AclClass aclClass) {
        StationOutputDTO output = (StationOutputDTO) bpmJob.getDataObject();

        // Instance geree, jamais reconstruite : voir DemandeImplantationService.
        Station entity = output.getId() != null
                ? stationRepository.findById(output.getId()).orElse(null)
                : null;
        if (entity == null) {
            entity = stationOutputMapper.toEntity(output);
        } else {
            stationOutputMapper.partialUpdate(entity, output);
        }
        entity.setWfProcessID(bpmJob.getProcessID());
        entity.setActivityName(bpmJob.getActivityName());
        entity.setEndProcess(bpmJob.getEndProcess());
        entity.setAssignee(bpmJob.getAssignee());

        if (Boolean.TRUE.equals(bpmJob.getEndProcess())) {
            entity.setStatutStation(StatutStation.CLOSED);
        } else if (entity.getStatutStation() == null
                || entity.getStatutStation() == StatutStation.DRAFT) {
            entity.setStatutStation(StatutStation.ENCOURS);
        }

        return persistAndApplySecurity(entity, bpmJob.getAuthors(), bpmJob.getReaders(), aclClass);
    }

    private Station persistAndApplySecurity(Station entity, List<String> authors,
                                            List<String> readers, AclClass aclClass) {
        if (entity.getId() != null) {
            stationRepository.findById(entity.getId()).ifPresent(recent -> {
                if (entity.getAclObjectIdentity() == null) {
                    entity.setAclObjectIdentity(recent.getAclObjectIdentity());
                }
                if (entity.getDemandeImplantation() == null) {
                    entity.setDemandeImplantation(recent.getDemandeImplantation());
                }
            });
        }

        rattacherEnfants(entity);

        Station persiste = stationRepository.save(entity);

        try {
            if (authors != null && readers != null) {
                kernelInterface.applySecurity(aclClass.getClasse(), persiste.getId(), authors, readers,
                        new ArrayList<>(), null, null, persiste.getAclObjectIdentity() == null, false);
            }
            if (persiste.getAclObjectIdentity() == null) {
                persiste = setAclObjectIdentity(persiste, aclClass);
                persiste = stationRepository.save(persiste);
            }
        } catch (Exception e) {
            log.error("applySecurity a echoue pour la station {} : {}", persiste.getId(), e.toString());
        }

        return persiste;
    }


    /** Meme raison que cote dossier : c'est l'enfant qui porte la FK. */
    private void rattacherEnfants(Station entity) {
        if (entity.getSiteImplantation() != null) {
            entity.getSiteImplantation().setStation(entity);
        }
        if (entity.getFrequences() != null) {
            for (Frequence frequence : entity.getFrequences()) {
                frequence.setStation(entity);
            }
        }
        if (entity.getAttestations() != null) {
            for (Attestation attestation : entity.getAttestations()) {
                attestation.setStation(entity);
            }
        }
    }

    public Station setAclObjectIdentity(Station entity, AclClass aclClass) {
        Integer aclObjectIdentityID = kernelInterface.findACLObjectIdentity(aclClass.getId(), entity.getId());
        if (aclObjectIdentityID == null) {
            return entity;
        }
        AclObjectIdentity aclObjectIdentity = new AclObjectIdentity();
        aclObjectIdentity.setId(aclObjectIdentityID.longValue());
        entity.setAclObjectIdentity(aclObjectIdentity);
        return entity;
    }

    private void publierPiecesJointes(StationInputDTO input, StationOutputDTO output,
                                      Station entity, AclClass aclClass) {
        List<AttachementInputDTO> pieces = new ArrayList<>();
        if (input.getAttachements() != null) {
            pieces.addAll(input.getAttachements());
        }
        if (input.getAutorisationAttachments() != null) {
            pieces.addAll(input.getAutorisationAttachments());
        }
        for (AttachementInputDTO attachement : pieces) {
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
                log.error("piece jointe refusee pour la station {} : {}", entity.getId(), e.toString());
            }
        }
    }

    private void saveWorkflowCommentaire(String texte, Station entity, AclClass aclClass) {
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
