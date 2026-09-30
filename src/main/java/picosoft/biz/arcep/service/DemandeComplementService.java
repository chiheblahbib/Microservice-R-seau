package picosoft.biz.arcep.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import picosoft.biz.arcep.Workflow.domain.BpmJob;
import picosoft.biz.arcep.Workflow.service.WorkflowService;
import picosoft.biz.arcep.client.currentuser.model.CurrentUser;
import picosoft.biz.arcep.client.kernel.intercomm.KernelInterface;
import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import picosoft.biz.arcep.client.kernel.model.acl.AclObjectIdentity;
import picosoft.biz.arcep.client.kernel.model.acl.Permission;
import picosoft.biz.arcep.client.kernel.model.acl.dto.ContactDTO;
import picosoft.biz.arcep.client.kernel.model.objects.AttachementInputDTO;
import picosoft.biz.arcep.client.kernel.model.objects.PublicAttachementDto;
import picosoft.biz.arcep.client.referentiel.ReferentielInterface;
import picosoft.biz.arcep.controller.errors.BadRequestAlertException;
import picosoft.biz.arcep.controller.errors.DemandeComplementErrors;
import picosoft.biz.arcep.domain.shared.DemandeComplement;
import picosoft.biz.arcep.domain.shared.Destinataire;
import picosoft.biz.arcep.repository.DemandeComplementRepository;
import picosoft.biz.arcep.repository.DestinataireRepository;
import picosoft.biz.arcep.service.dto.DemandeComplementDTO;
import picosoft.biz.arcep.service.dto.DemandeComplementInputDTO;
import picosoft.biz.arcep.service.dto.DemandeComplementOutputDTO;
import picosoft.biz.arcep.service.mapper.DemandeComplementInputMapper;
import picosoft.biz.arcep.service.mapper.DemandeComplementMapper;
import picosoft.biz.arcep.service.mapper.DemandeComplementOutputMapper;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@Transactional
public class DemandeComplementService {

    private final Logger log = LoggerFactory.getLogger(DemandeComplementService.class);

    private final DemandeComplementRepository demandeComplementRepository;
    private final DestinataireRepository destinataireRepository;
    private final DemandeComplementMapper demandeComplementMapper;
    private final DemandeComplementInputMapper demandeComplementInputMapper;
    private final DemandeComplementOutputMapper demandeComplementOutputMapper;
    private final KernelInterface kernelInterface;
    private final WorkflowService workflowService;
    private final CurrentUser currentUser;
    private final ReferentielInterface referentielInterface;
    private final DossierDrrrsResolver dossierDrrrsResolver;
    private final SuspensionDossierService suspensionDossierService;

    public DemandeComplementService(
            DemandeComplementRepository demandeComplementRepository,
            DestinataireRepository destinataireRepository,
            DemandeComplementMapper demandeComplementMapper,
            DemandeComplementInputMapper demandeComplementInputMapper,
            DemandeComplementOutputMapper demandeComplementOutputMapper,
            KernelInterface kernelInterface,
            WorkflowService workflowService,
            CurrentUser currentUser,
            ReferentielInterface referentielInterface,
            DossierDrrrsResolver dossierDrrrsResolver,
            SuspensionDossierService suspensionDossierService
    ) {
        this.demandeComplementRepository = demandeComplementRepository;
        this.destinataireRepository = destinataireRepository;
        this.demandeComplementMapper = demandeComplementMapper;
        this.demandeComplementInputMapper = demandeComplementInputMapper;
        this.demandeComplementOutputMapper = demandeComplementOutputMapper;
        this.kernelInterface = kernelInterface;
        this.workflowService = workflowService;
        this.currentUser = currentUser;
        this.referentielInterface = referentielInterface;
        this.dossierDrrrsResolver = dossierDrrrsResolver;
        this.suspensionDossierService = suspensionDossierService;
    }

    @Transactional
    public DemandeComplementDTO save(DemandeComplementDTO demandeComplementDTO) {
        DemandeComplement demandeComplement = demandeComplementMapper.toEntity(demandeComplementDTO);
        demandeComplement = demandeComplementRepository.save(demandeComplement);
        return demandeComplementMapper.toDto(demandeComplement);
    }

    @Transactional
    public DemandeComplementDTO update(Long id, DemandeComplementDTO demandeComplementDTO) {
        DemandeComplement demandeComplement = demandeComplementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DemandeComplement not found"));
        demandeComplementMapper.partialUpdate(demandeComplement, demandeComplementDTO);
        demandeComplement = demandeComplementRepository.save(demandeComplement);
        return demandeComplementMapper.toDto(demandeComplement);
    }

    @Transactional
    public Page<DemandeComplementDTO> findAll(Pageable pageable) {
        // Table partagée avec homologation : seules les demandes des dossiers DRRRS.
        return demandeComplementRepository.findAll(
                (root, query, cb) -> root.get("typeDossier").in(DossierDrrrsResolver.typesDrrrs()), pageable)
                .map(demandeComplementMapper::toDto);
    }

    @Transactional
    public Optional<DemandeComplementDTO> findOne(Long id) {
        return demandeComplementRepository.findById(id)
                .map(demandeComplementMapper::toDto);
    }

    @Transactional
    public DemandeComplementOutputDTO findOneWithPermission(Long id) {
        Optional<DemandeComplement> demandeComplementOpt = demandeComplementRepository.findById(id);
        if (!demandeComplementOpt.isPresent()) {
            throw new BadRequestAlertException(DemandeComplementErrors.OBJECT_NOT_FOUND, DemandeComplementErrors.CLASS, DemandeComplementErrors.OBJECT_NOT_FOUND);
        }

        DemandeComplement demandeComplement = demandeComplementOpt.get();

        // Récupérer la classe ACL
        AclClass aclClass = kernelInterface.getaclClassByClassName(DemandeComplement.class.getName());
        if (aclClass == null) {
            throw new BadRequestAlertException(DemandeComplementErrors.ACL_CLASS_NOT_FOUND, DemandeComplementErrors.CLASS, DemandeComplementErrors.ACL_CLASS_NOT_FOUND);
        }

        // Vérifier la permission
        String permission = kernelInterface.checkSecurity(aclClass.getSimpleName(), id, currentUser.getSid());

        // Si pas de permission et l'objet a un ACL, refuser l'accès
        if (Permission.NONE.name().equals(permission) && demandeComplement.getAclObjectIdentity() != null) {
            throw new BadRequestAlertException(DemandeComplementErrors.OBJECT_NOT_AUTHORIZED, DemandeComplementErrors.CLASS, DemandeComplementErrors.OBJECT_NOT_AUTHORIZED);
        }

        // Construire le DTO avec permission
        DemandeComplementOutputDTO outputDTO = demandeComplementOutputMapper.toDto(demandeComplement);
        outputDTO.setClassId(aclClass.getId());
        outputDTO.setClassName(aclClass.getClasse());
        outputDTO.setUserPermission(permission);

        // Charger et mapper l'objet ASI complet si présent

        return outputDTO;
    }

    @Transactional
    public List<DemandeComplementDTO> findByAsiId(Long asiId) {
        return demandeComplementMapper.toDto(demandeComplementRepository.findByAsiId(asiId));
    }

    public void delete(Long id) {
        demandeComplementRepository.deleteById(id);
    }

    @Transactional
    public DemandeComplementOutputDTO saveDraftDemandeComplement(DemandeComplementInputDTO inputDTO, AclClass aclClass) throws Exception {
        if (aclClass == null) {
            throw new BadRequestAlertException(DemandeComplementErrors.ACL_CLASS_NOT_FOUND, DemandeComplementErrors.CLASS, DemandeComplementErrors.ACL_CLASS_NOT_FOUND);
        }


        if (inputDTO.getSidExterne() == null) {
            try {
                String sidExterne = referentielInterface.getByUserId(currentUser.getKeycloakId()).getBody().getSid();
                inputDTO.setSidExterne(sidExterne);
            } catch (Exception e) {
                log.warn("Could not retrieve sidExterne for user {}", currentUser.getKeycloakId(), e);
            }
        }

        DemandeComplement demandeComplement;
        if (inputDTO.getId() != null) {
            Optional<DemandeComplement> existing = demandeComplementRepository.findById(inputDTO.getId());
            if (existing.isPresent()) {
                demandeComplement = existing.get();
                demandeComplementInputMapper.partialUpdate(demandeComplement, inputDTO);
            } else {
                demandeComplement = demandeComplementInputMapper.toEntity(inputDTO);
            }
        } else {
            demandeComplement = demandeComplementInputMapper.toEntity(inputDTO);
        }

        if (demandeComplement.getReference() == null) {
            demandeComplement.setReference(kernelInterface.getSequenceNumberByClass(new JSONObject(inputDTO).toString(), aclClass.getClasse()));
        }

        demandeComplement = demandeComplementRepository.save(demandeComplement);

        handleDestinataire(demandeComplement, inputDTO.getDestinataire());

        DemandeComplementOutputDTO outputDTO = demandeComplementOutputMapper.toDto(demandeComplement);
        outputDTO.setClassId(aclClass.getId());
        outputDTO.setClassName(aclClass.getClasse());

        if (inputDTO.getId() == null) {
            handleAttachments(inputDTO.getAttachements(), demandeComplement, aclClass, outputDTO);
        }

        demandeComplement = saveDemandeComplement(demandeComplement, Arrays.asList(currentUser.getEmployeSid()), new ArrayList<>(), aclClass);

        DemandeComplementOutputDTO result = demandeComplementOutputMapper.toDto(demandeComplement);
        result.setClassId(aclClass.getId());
        result.setClassName(aclClass.getClasse());
        return result;
    }

    @Transactional
    public DemandeComplementOutputDTO firstSubmitDemandeComplement(DemandeComplementInputDTO inputDTO, AclClass aclClass) throws Exception {
        log.info("firstSubmitDemandeComplement called for id={}, attachements count={}",
                inputDTO.getId(),
                inputDTO.getAttachements() != null ? inputDTO.getAttachements().size() : 0);

        if (aclClass == null) {
            throw new BadRequestAlertException(DemandeComplementErrors.ACL_CLASS_NOT_FOUND, DemandeComplementErrors.CLASS, DemandeComplementErrors.ACL_CLASS_NOT_FOUND);
        }


        inputDTO.setCreatedDate(java.time.ZonedDateTime.now());
        inputDTO.setSendedDate(java.time.ZonedDateTime.now());

        DemandeComplement demandeComplement;
        if (inputDTO.getId() != null) {
            Optional<DemandeComplement> existing = demandeComplementRepository.findById(inputDTO.getId());
            if (existing.isPresent()) {
                demandeComplement = existing.get();
                demandeComplementInputMapper.partialUpdate(demandeComplement, inputDTO);
            } else {
                demandeComplement = demandeComplementInputMapper.toEntity(inputDTO);
            }
        } else {
            demandeComplement = demandeComplementInputMapper.toEntity(inputDTO);
        }

        if (demandeComplement.getReference() == null) {
            demandeComplement.setReference(kernelInterface.getSequenceNumberByClass(new JSONObject(inputDTO).toString(), aclClass.getClasse()));
        }

        demandeComplement = demandeComplementRepository.save(demandeComplement);

        handleDestinataire(demandeComplement, inputDTO.getDestinataire());

        DemandeComplementOutputDTO outputDTO = demandeComplementOutputMapper.toDto(demandeComplement);
        outputDTO.setClassId(aclClass.getId());
        outputDTO.setClassName(aclClass.getClasse());

        // Gestion des attachements - même pattern que AsiService.initAndSubmitAsi
        // Les attachements ne sont traités que pour les nouveaux objets (inputDTO.getId() == null)
        // Pour les objets existants (brouillon déjà sauvegardé), les attachements doivent être gérés séparément
        if (inputDTO.getId() == null && inputDTO.getAttachements() != null && !inputDTO.getAttachements().isEmpty()) {

            // applySecurity pour les attachements (uniquement si pas encore de workflow)
            if (demandeComplement.getWfProcessID() == null) {
                List<String> sids = currentUser.getEmployeSid() != null ? Arrays.asList(currentUser.getEmployeSid()) : new ArrayList<>();
                try {
                    sids.add(referentielInterface.getByUserId(currentUser.getKeycloakId()).getBody().getSid());
                } catch (Exception e) {
                    log.warn("Could not retrieve sid from referentiel", e);
                }
                try {
                    kernelInterface.applySecurity(
                            aclClass.getClasse(),
                            demandeComplement.getId(),
                            sids,
                            new ArrayList<>(),
                            new ArrayList<>(),
                            null,
                            null,
                            demandeComplement.getAclObjectIdentity() == null,
                            false
                    );
                    if (demandeComplement.getAclObjectIdentity() == null) {
                        demandeComplement = setAclObjectIdentity(demandeComplement, aclClass);
                    }
                } catch (Exception e) {
                    log.error("Error applying security for attachments", e);
                }
            }

            // Upload des attachements - inline comme dans AsiService (pas de try-catch pour publicAttachement)
            for (AttachementInputDTO attachementInputDTO : inputDTO.getAttachements()) {
                if (attachementInputDTO == null) {
                    continue;
                }
                if (attachementInputDTO.getUuid() == null) {
                    if (attachementInputDTO.getFileBase64() == null) {
                        continue;
                    }
                    PublicAttachementDto publicAttachementDto = new PublicAttachementDto();
                    publicAttachementDto.setFileName(attachementInputDTO.getFileName());
                    publicAttachementDto.setReqFileDefName(attachementInputDTO.getReqFileDefName());
                    publicAttachementDto.setClassId(aclClass.getId());
                    publicAttachementDto.setObjectId(outputDTO.getId());
                    publicAttachementDto.setDecodedBytes(Base64.getDecoder().decode(attachementInputDTO.getFileBase64()));
                    publicAttachementDto.setObjectData(new JSONObject(outputDTO).toString());

                    log.info("Uploading attachment fileName={}, objectId={}, classId={}",
                            publicAttachementDto.getFileName(),
                            publicAttachementDto.getObjectId(),
                            publicAttachementDto.getClassId());

                    kernelInterface.publicAttachement(publicAttachementDto);

                    log.info("Attachment uploaded successfully");
                } else if (attachementInputDTO.getAction() != null && "DELETE".equalsIgnoreCase(attachementInputDTO.getAction())) {
                    kernelInterface.deleteFileRessource(attachementInputDTO.getUuid(), "");
                }
            }
        }

        String contactSid = null;
        Destinataire destinataire = demandeComplement.getDestinataire();
        if (destinataire != null && destinataire.getEmail() != null) {
            picosoft.biz.arcep.client.kernel.model.acl.dto.ContactDTO contact = findContactByEmail(destinataire.getEmail());
            if (contact != null && contact.getSid() != null) {
                outputDTO.setSidExterne(contact.getSid());
                contactSid = contact.getSid();
                log.info("Contact référentiel trouvé pour destinataire email={}, contactId={}, sid={}",
                        destinataire.getEmail(), contact.getId(), contactSid);
            } else {
                log.info("Aucun contact référentiel trouvé pour destinataire email={}", destinataire.getEmail());
            }
        }

        // === SUSPENSION DU DOSSIER DRRRS PARENT ===
        // Comme pour l'ASI chez homologation : le circuit du dossier est mis en pause
        // le temps que le complement soit fourni, et son echeance decalee a la reprise.
        if (dossierDrrrsResolver.estDrrrs(inputDTO.getTypeDossier()) && inputDTO.getObjectIdDossier() != null) {
            try {
                String wfDossier = dossierDrrrsResolver.wfProcessID(inputDTO.getTypeDossier(), inputDTO.getObjectIdDossier()).orElse(null);
                if (wfDossier != null) {
                    java.util.Date echeance = workflowService.getDueDateOfActiveTask(wfDossier);
                    ZonedDateTime dueDateAvantSuspension = echeance != null
                            ? ZonedDateTime.ofInstant(echeance.toInstant(), java.time.ZoneId.systemDefault())
                            : null;
                    workflowService.suspendProcessInstance(wfDossier);
                    log.info("Dossier {} suspendu : id={}, wfProcessID={}", inputDTO.getTypeDossier(), inputDTO.getObjectIdDossier(), wfDossier);
                    suspensionDossierService.creerSuspension(
                            inputDTO.getTypeDossier(),
                            inputDTO.getObjectIdDossier(),
                            wfDossier,
                            dueDateAvantSuspension,
                            "Demande de complément lancée",
                            currentUser.getEmployeSid()
                    );
                }
            } catch (Exception e) {
                log.error("Suspension impossible du dossier {} id={}", inputDTO.getTypeDossier(), inputDTO.getObjectIdDossier(), e);
            }
        }

        BpmJob bpmJob = workflowService._initAndNextTask(aclClass.getFwProcess(), inputDTO.getDecision(), inputDTO.getWfComment(), outputDTO, aclClass);

        outputDTO = safeCastOutput(bpmJob.getDataObject(), outputDTO);

        DemandeComplement workflowDemandeComplement = demandeComplementOutputMapper.toEntity(outputDTO);
        demandeComplement.setWfProcessID(bpmJob.getProcessID());
        demandeComplement.setActivityName(bpmJob.getActivityName());
        demandeComplement.setEndProcess(bpmJob.getEndProcess());
        demandeComplement.setAssignee(bpmJob.getAssignee() != null ? bpmJob.getAssignee() : null);
        demandeComplement.setState(workflowDemandeComplement.getState());
        demandeComplement.setStateDemande(workflowDemandeComplement.getStateDemande());
        demandeComplement.setStep(workflowDemandeComplement.getStep());

        List<String> readersWithSidExterne = new ArrayList<>(bpmJob.getReaders());

        if(contactSid != null && !contactSid.isEmpty())
            readersWithSidExterne.add(contactSid);


        // Chercher le contact destinataire et ajouter son SID comme lecteur


        // Si le processus est terminé, ajouter l'initiator comme lecteur
        if (Boolean.TRUE.equals(bpmJob.getEndProcess())) {
            String initiatorSid = currentUser.getEmployeSid();
            if (initiatorSid != null && !readersWithSidExterne.contains(initiatorSid)) {
                readersWithSidExterne.add(initiatorSid);
                log.info("Initiator ajouté comme lecteur pour demandeComplement id={} (processus terminé), sid={}",
                         demandeComplement.getId(), initiatorSid);
            }
        }

        demandeComplement = saveDemandeComplement(demandeComplement, bpmJob.getAuthors(), readersWithSidExterne, aclClass);

        DemandeComplementOutputDTO result = demandeComplementOutputMapper.toDto(demandeComplement);
        result.setClassId(aclClass.getId());
        result.setClassName(aclClass.getClasse());
        return result;
    }

    @Transactional
    public DemandeComplementOutputDTO submitDemandeComplement(DemandeComplementInputDTO inputDTO, AclClass aclClass) throws Exception {

        DemandeComplement demandeComplement = demandeComplementRepository.findById(inputDTO.getId()).orElseThrow(() ->
                new BadRequestAlertException(DemandeComplementErrors.OBJECT_NOT_FOUND, DemandeComplementErrors.CLASS, DemandeComplementErrors.OBJECT_NOT_FOUND)
        );

        String permission = kernelInterface.checkSecurity(aclClass.getSimpleName(), demandeComplement.getId(), currentUser.getSid());
        if (!Permission.WRITE.name().equals(permission) && !Permission.INH_WRITE.name().equals(permission)) {
            throw new BadRequestAlertException(DemandeComplementErrors.OBJECT_NOT_AUTHORIZED, DemandeComplementErrors.CLASS, DemandeComplementErrors.OBJECT_NOT_AUTHORIZED);
        }

        demandeComplementInputMapper.partialUpdate(demandeComplement, inputDTO);

        if (demandeComplement.getReference() == null) {
            demandeComplement.setReference(kernelInterface.getSequenceNumberByClass(new JSONObject(inputDTO).toString(), aclClass.getClasse()));
        }

        demandeComplement = demandeComplementRepository.save(demandeComplement);

        handleDestinataire(demandeComplement, inputDTO.getDestinataire());

        DemandeComplementOutputDTO outputDTO = demandeComplementOutputMapper.toDto(demandeComplement);
        outputDTO.setClassId(aclClass.getId());
        outputDTO.setClassName(aclClass.getClasse());

        // Gestion des attachements - même pattern que AsiService
        // ATTENTION: Si le frontend envoie des attachements déjà uploadés (uuid != null) avec fileBase64,
        // ils seront re-uploadés. Le frontend doit envoyer uuid sans fileBase64 pour les attachements existants.
        if (inputDTO.getAttachements() != null && !inputDTO.getAttachements().isEmpty()) {
            for (AttachementInputDTO attachementInputDTO : inputDTO.getAttachements()) {
                if (attachementInputDTO == null) {
                    continue;
                }
                if (attachementInputDTO.getUuid() == null) {
                    if (attachementInputDTO.getFileBase64() == null) {
                        continue;
                    }

                    log.warn("Submitting attachment without uuid - this may cause duplication if already uploaded. " +
                            "fileName={}, objectId={}. Ensure frontend sends uuid for existing attachments.",
                            attachementInputDTO.getFileName(), outputDTO.getId());

                    PublicAttachementDto publicAttachementDto = new PublicAttachementDto();
                    publicAttachementDto.setFileName(attachementInputDTO.getFileName());
                    publicAttachementDto.setReqFileDefName(attachementInputDTO.getReqFileDefName());
                    publicAttachementDto.setClassId(aclClass.getId());
                    publicAttachementDto.setObjectId(outputDTO.getId());
                    publicAttachementDto.setDecodedBytes(Base64.getDecoder().decode(attachementInputDTO.getFileBase64()));
                    publicAttachementDto.setObjectData(new JSONObject(outputDTO).toString());

                    log.info("Uploading attachment fileName={}, objectId={}, classId={}",
                            publicAttachementDto.getFileName(),
                            publicAttachementDto.getObjectId(),
                            publicAttachementDto.getClassId());

                    kernelInterface.publicAttachement(publicAttachementDto);

                    log.info("Attachment uploaded successfully");
                } else if (attachementInputDTO.getAction() != null && "DELETE".equalsIgnoreCase(attachementInputDTO.getAction())) {
                    kernelInterface.deleteFileRessource(attachementInputDTO.getUuid(), "");
                }
            }
        }
        String contactSid = null;
        Destinataire destinataireSubmit = demandeComplement.getDestinataire();
        if (destinataireSubmit != null && destinataireSubmit.getEmail() != null) {
            picosoft.biz.arcep.client.kernel.model.acl.dto.ContactDTO contact = findContactByEmail(destinataireSubmit.getEmail());
            if (contact != null && contact.getSid() != null) {
                contactSid = contact.getSid();
                outputDTO.setSidExterne(contact.getKeycloakId());


                log.info("Contact référentiel trouvé pour destinataire email={}, contactId={}, sid={}",
                        destinataireSubmit.getEmail(), contact.getId(), contactSid);
            } else {
                log.info("Aucun contact référentiel trouvé pour destinataire email={}", destinataireSubmit.getEmail());
            }
        }
        BpmJob bpmJob = workflowService._nextTask(demandeComplement.getWfProcessID(), inputDTO.getDecision(), inputDTO.getWfComment(), outputDTO, aclClass);

        outputDTO = safeCastOutput(bpmJob.getDataObject(), outputDTO);

        DemandeComplement workflowDemandeComplement = demandeComplementOutputMapper.toEntity(outputDTO);
        demandeComplement.setWfProcessID(bpmJob.getProcessID());
        demandeComplement.setActivityName(bpmJob.getActivityName());
        demandeComplement.setEndProcess(bpmJob.getEndProcess());
        demandeComplement.setAssignee(bpmJob.getAssignee() != null ? bpmJob.getAssignee() : null);
        demandeComplement.setState(workflowDemandeComplement.getState());
        demandeComplement.setStateDemande(workflowDemandeComplement.getStateDemande());
        demandeComplement.setStep(workflowDemandeComplement.getStep());

        Boolean delaiReponse = calculateDelaiReponse(demandeComplement);
        demandeComplement.setDelaiReponse(delaiReponse);

        // Chercher le contact destinataire et ajouter son SID comme lecteur
        List<String> readersWithContactSid = new ArrayList<>(bpmJob.getReaders());
        if(contactSid != null && !contactSid.isEmpty())
            readersWithContactSid.add(contactSid);

        // Si le processus est terminé, ajouter l'initiator comme lecteur
        if (Boolean.TRUE.equals(bpmJob.getEndProcess())) {
            String initiatorSid = currentUser.getEmployeSid();
            if (initiatorSid != null && !readersWithContactSid.contains(initiatorSid)) {
                readersWithContactSid.add(initiatorSid);
                log.info("Initiator ajouté comme lecteur pour demandeComplement id={} (processus terminé), sid={}",
                        demandeComplement.getId(), initiatorSid);
            }
        }

        demandeComplement = saveDemandeComplement(demandeComplement, bpmJob.getAuthors(), readersWithContactSid, aclClass);

        DemandeComplementOutputDTO result = demandeComplementOutputMapper.toDto(demandeComplement);
        result.setClassId(aclClass.getId());
        result.setClassName(aclClass.getClasse());
        result.setUserPermission(permission);
        return result;
    }

    private picosoft.biz.arcep.client.kernel.model.acl.dto.ContactDTO findContactByEmail(String email) {
        if (email == null || email.isEmpty()) return null;
        try {
            ResponseEntity<picosoft.biz.arcep.client.referentiel.model.ContactDTO> response =
                referentielInterface.getByEmail(email);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                picosoft.biz.arcep.client.referentiel.model.ContactDTO referentielContact = response.getBody();
                log.info("Contact trouvé dans référentiel pour email={}, id={}, contactKernelId={}",
                         email, referentielContact.getId(), referentielContact.getContactKernelId());

                if (referentielContact.getId() != null) {
                    try {
                        ResponseEntity<ContactDTO> kernelResponse =
                            kernelInterface.getContact(referentielContact.getContactKernelId());
                        if (kernelResponse.getStatusCode().is2xxSuccessful() && kernelResponse.getBody() != null) {
                            picosoft.biz.arcep.client.kernel.model.acl.dto.ContactDTO kernelContact = kernelResponse.getBody();
                            if (kernelContact.getSid() != null) {
                                log.info("SID récupéré du kernel pour contact email={}, sid={}", email, kernelContact.getSid());
                                return kernelContact;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Impossible de récupérer le contact du kernel pour email={}, id={}", email, referentielContact.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Impossible de trouver le contact dans le référentiel pour email={}", email, e);
        }
        return null;
    }

    private DemandeComplementOutputDTO safeCastOutput(Object dataObject, DemandeComplementOutputDTO fallback) {
        if (dataObject instanceof DemandeComplementOutputDTO) {
            return (DemandeComplementOutputDTO) dataObject;
        }
        return fallback;
    }

    private Boolean calculateDelaiReponse(DemandeComplement demandeComplement) {
        if (demandeComplement.getWfProcessID() == null) {
            return true;
        }
        try {
            org.flowable.task.api.Task task = workflowService.getActifTaskOfProcessInstance(demandeComplement.getWfProcessID());
            if (task == null || task.getDueDate() == null) {
                return true;
            }
            java.time.ZonedDateTime dueDate = java.time.ZonedDateTime.ofInstant(
                    task.getDueDate().toInstant(),
                    java.time.ZoneId.systemDefault()
            );
            java.time.ZonedDateTime now = java.time.ZonedDateTime.now();
            long diff = java.time.temporal.ChronoUnit.DAYS.between(now.toLocalDate(), dueDate.toLocalDate());
            return diff >= 0;
        } catch (Exception e) {
            log.error("Error calculating delaiReponse for demandeComplement id={}", demandeComplement.getId(), e);
            return true;
        }
    }

    private DemandeComplement saveDemandeComplement(DemandeComplement demandeComplement, List<String> authors, List<String> readers, AclClass aclClass) {
        log.debug("saveDemandeComplement called for id={}, current aclObjectIdentity={}",
                demandeComplement.getId(),
                demandeComplement.getAclObjectIdentity() != null ? demandeComplement.getAclObjectIdentity().getId() : "null");

        Optional<DemandeComplement> recentOpt = demandeComplementRepository.findById(demandeComplement.getId());
        if (recentOpt.isPresent()) {
            DemandeComplement recent = recentOpt.get();

            if (recent.getAclObjectIdentity() != null) {
                log.debug("Restoring aclObjectIdentity from recent for id={}", demandeComplement.getId());
                demandeComplement.setAclObjectIdentity(recent.getAclObjectIdentity());
            }
            if (recent.getWfProcessID() != null) {
                demandeComplement.setWfProcessID(recent.getWfProcessID());
            }
            if (recent.getReference() != null) {
                demandeComplement.setReference(recent.getReference());
            }
            if (demandeComplement.getActivityName() == null && recent.getActivityName() != null) {
                demandeComplement.setActivityName(recent.getActivityName());
            }
            if (demandeComplement.getState() == null && recent.getState() != null) {
                demandeComplement.setState(recent.getState());
            }
            if (demandeComplement.getEndProcess() == null && recent.getEndProcess() != null) {
                demandeComplement.setEndProcess(recent.getEndProcess());
            }
            if (demandeComplement.getStep() == null && recent.getStep() != null) {
                demandeComplement.setStep(recent.getStep());
            }
            if (demandeComplement.getDestinataire() == null && recent.getDestinataire() != null) {
                demandeComplement.setDestinataire(recent.getDestinataire());
            }
        }

        if (demandeComplement.getClassId() == null && aclClass != null) {
            demandeComplement.setClassId(aclClass.getId());
        }

        try {
            Long count = kernelInterface.countAttachements(demandeComplement.getId(), aclClass.getId());
            demandeComplement.setNumberOfattachments(count);
        } catch (Exception e) {
            log.error("Error counting attachments", e);
        }

        demandeComplement = demandeComplementRepository.save(demandeComplement);

        if (authors != null && readers != null) {
            try {
                kernelInterface.applySecurity(
                        aclClass.getClasse(),
                        demandeComplement.getId(),
                        authors,
                        readers,
                        new ArrayList<>(),
                        null,
                        null,
                        demandeComplement.getAclObjectIdentity() == null,
                        false
                );
            } catch (Exception e) {
                log.error("Error applying security", e);
            }
        }

        if (demandeComplement.getAclObjectIdentity() == null) {
            try {
                demandeComplement = setAclObjectIdentity(demandeComplement, aclClass);
                demandeComplement = demandeComplementRepository.save(demandeComplement);
            } catch (Exception e) {
                log.error("Error setting aclObjectIdentity", e);
            }
        }

        log.debug("saveDemandeComplement finished for id={}, final aclObjectIdentity={}",
                demandeComplement.getId(),
                demandeComplement.getAclObjectIdentity() != null ? demandeComplement.getAclObjectIdentity().getId() : "null");

        return demandeComplement;
    }

    private DemandeComplement setAclObjectIdentity(DemandeComplement demandeComplement, AclClass aclClass) {
        Integer aclObjectIdentityID = null;
        int retries = 3;
        for (int i = 0; i < retries; i++) {
            aclObjectIdentityID = kernelInterface.findACLObjectIdentity(aclClass.getId(), demandeComplement.getId());
            if (aclObjectIdentityID != null) {
                break;
            }
            log.warn("findACLObjectIdentity returned null for classId={}, objectId={}, retry {}/{}",
                    aclClass.getId(), demandeComplement.getId(), i + 1, retries);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (aclObjectIdentityID != null) {
            AclObjectIdentity aclObjectIdentity = new AclObjectIdentity();
            aclObjectIdentity.setId(aclObjectIdentityID.longValue());
            demandeComplement.setAclObjectIdentity(aclObjectIdentity);
            log.info("AclObjectIdentity set for DemandeComplement id={}, aclObjectIdentityId={}",
                    demandeComplement.getId(), aclObjectIdentityID);
        } else {
            log.error("Failed to find ACLObjectIdentity for DemandeComplement id={}, classId={} after {} retries",
                    demandeComplement.getId(), aclClass.getId(), retries);
        }
        return demandeComplement;
    }

    private void handleDestinataire(DemandeComplement demandeComplement, picosoft.biz.arcep.service.dto.DestinataireDTO destinataireDTO) {
        if (destinataireDTO == null) {
            return;
        }

        log.debug("handleDestinataire called for demandeComplement id={}", demandeComplement.getId());

        Destinataire destinataire;
        Optional<Destinataire> existingDestinataire = destinataireRepository.findByDemandeComplementId(demandeComplement.getId());

        if (existingDestinataire.isPresent()) {
            log.debug("Found existing destinataire id={} for demandeComplement id={}",
                    existingDestinataire.get().getId(), demandeComplement.getId());
            destinataire = existingDestinataire.get();
            destinataire.setNom(destinataireDTO.getNom());
            destinataire.setEmail(destinataireDTO.getEmail());
            destinataire.setTelephone(destinataireDTO.getTelephone());
            destinataire.setSociete(destinataireDTO.getSociete());
            destinataire.setNationalite(destinataireDTO.getNationalite());
            destinataire.setAdresse(destinataireDTO.getAdresse());
        } else {
            log.debug("Creating new destinataire for demandeComplement id={}", demandeComplement.getId());
            destinataire = new Destinataire();
            destinataire.setNom(destinataireDTO.getNom());
            destinataire.setEmail(destinataireDTO.getEmail());
            destinataire.setTelephone(destinataireDTO.getTelephone());
            destinataire.setSociete(destinataireDTO.getSociete());
            destinataire.setNationalite(destinataireDTO.getNationalite());
            destinataire.setAdresse(destinataireDTO.getAdresse());
        }

        // IMPORTANT: Utiliser le DemandeComplement original (pas un proxy du mapper)
        destinataire.setDemandeComplement(demandeComplement);

        // IMPORTANT: Mettre à jour la relation bidirectionnelle pour éviter les doublons avec CascadeType
        demandeComplement.setDestinataire(destinataire);

        destinataire = destinataireRepository.save(destinataire);
        log.debug("Destinataire saved id={} for demandeComplement id={}", destinataire.getId(), demandeComplement.getId());
    }

    private void handleAttachments(List<AttachementInputDTO> attachements, DemandeComplement demandeComplement, AclClass aclClass, DemandeComplementOutputDTO outputDTO) {
        if (attachements == null || attachements.isEmpty()) {
            log.debug("handleAttachments: no attachments to process");
            return;
        }

        log.debug("handleAttachments called for demandeComplement id={}, outputDTO id={}, aclClass id={}, number of attachments={}",
                demandeComplement.getId(), outputDTO.getId(), aclClass.getId(), attachements.size());

        if (demandeComplement.getWfProcessID() == null) {
            try {
                kernelInterface.applySecurity(
                        aclClass.getClasse(),
                        demandeComplement.getId(),
                        Arrays.asList(currentUser.getEmployeSid()),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        null,
                        null,
                        demandeComplement.getAclObjectIdentity() == null,
                        false
                );
                if (demandeComplement.getAclObjectIdentity() == null) {
                    demandeComplement = setAclObjectIdentity(demandeComplement, aclClass);
                    demandeComplementRepository.save(demandeComplement);
                }
            } catch (Exception e) {
                log.error("Error applying temporary security for attachments", e);
            }
        }

        for (AttachementInputDTO attachementInputDTO : attachements) {
            if (attachementInputDTO == null) {
                log.debug("handleAttachments: null attachment skipped");
                continue;
            }
            log.debug("handleAttachments: processing attachment uuid={}, action={}, fileName={}, hasFileBase64={}",
                    attachementInputDTO.getUuid(),
                    attachementInputDTO.getAction(),
                    attachementInputDTO.getFileName(),
                    attachementInputDTO.getFileBase64() != null);

            if (attachementInputDTO.getUuid() == null) {
                if (attachementInputDTO.getFileBase64() == null) {
                    log.debug("handleAttachments: uuid is null and fileBase64 is null, skipping");
                    continue;
                }
                PublicAttachementDto publicAttachementDto = new PublicAttachementDto();
                publicAttachementDto.setFileName(attachementInputDTO.getFileName());
                publicAttachementDto.setReqFileDefName(attachementInputDTO.getReqFileDefName());
                publicAttachementDto.setClassId(aclClass.getId());
                publicAttachementDto.setObjectId(outputDTO.getId());
                publicAttachementDto.setDecodedBytes(Base64.getDecoder().decode(attachementInputDTO.getFileBase64()));
                publicAttachementDto.setObjectData(new JSONObject(outputDTO).toString());

                log.info("handleAttachments: uploading attachment fileName={}, objectId={}, classId={}, reqFileDefName={}",
                        publicAttachementDto.getFileName(),
                        publicAttachementDto.getObjectId(),
                        publicAttachementDto.getClassId(),
                        publicAttachementDto.getReqFileDefName());

                try {
                    kernelInterface.publicAttachement(publicAttachementDto);
                    log.info("handleAttachments: attachment uploaded successfully");
                } catch (Exception e) {
                    log.error("Error uploading attachment fileName={}, objectId={}, classId={}",
                            attachementInputDTO.getFileName(), outputDTO.getId(), aclClass.getId(), e);
                }
            } else if ("DELETE".equalsIgnoreCase(attachementInputDTO.getAction())) {
                try {
                    kernelInterface.deleteFileRessource(attachementInputDTO.getUuid(), "");
                    log.info("handleAttachments: attachment deleted successfully uuid={}", attachementInputDTO.getUuid());
                } catch (Exception e) {
                    log.error("Error deleting attachment uuid={}", attachementInputDTO.getUuid(), e);
                }
            } else {
                log.debug("handleAttachments: unknown action={}, uuid={}, skipping",
                        attachementInputDTO.getAction(), attachementInputDTO.getUuid());
            }
        }
    }
}
