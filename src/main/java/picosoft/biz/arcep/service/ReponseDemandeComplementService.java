package picosoft.biz.arcep.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import picosoft.biz.arcep.client.currentuser.model.CurrentUser;
import picosoft.biz.arcep.client.kernel.intercomm.KernelInterface;
import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import picosoft.biz.arcep.client.kernel.model.objects.AttachementInputDTO;
import picosoft.biz.arcep.client.kernel.model.objects.PublicAttachementDto;
import picosoft.biz.arcep.client.referentiel.ReferentielInterface;
import picosoft.biz.arcep.controller.errors.BadRequestAlertException;
import picosoft.biz.arcep.controller.errors.DemandeComplementErrors;
import picosoft.biz.arcep.Workflow.service.WorkflowService;
import picosoft.biz.arcep.domain.shared.DemandeComplement;
import picosoft.biz.arcep.domain.shared.ReponseDemandeComplement;
import picosoft.biz.arcep.domain.shared.SuspensionDossier;
import picosoft.biz.arcep.repository.DemandeComplementRepository;
import picosoft.biz.arcep.repository.ReponseDemandeComplementRepository;
import picosoft.biz.arcep.service.dto.ReponseDemandeComplementDTO;
import picosoft.biz.arcep.service.dto.ReponseDemandeComplementInputDTO;
import picosoft.biz.arcep.service.dto.ReponseDemandeComplementResultDTO;
import picosoft.biz.arcep.service.mapper.ReponseDemandeComplementMapper;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReponseDemandeComplementService {

    private final Logger log = LoggerFactory.getLogger(ReponseDemandeComplementService.class);

    private final ReponseDemandeComplementRepository reponseDemandeComplementRepository;
    private final DemandeComplementRepository demandeComplementRepository;
    private final ReponseDemandeComplementMapper reponseDemandeComplementMapper;
    private final KernelInterface kernelInterface;
    private final CurrentUser currentUser;
    private final ReferentielInterface referentielInterface;
    private final SuspensionDossierService suspensionDossierService;
    private final WorkflowService workflowService;
    private final DossierDrrrsResolver dossierDrrrsResolver;

    public ReponseDemandeComplementService(
            ReponseDemandeComplementRepository reponseDemandeComplementRepository,
            DemandeComplementRepository demandeComplementRepository,
            ReponseDemandeComplementMapper reponseDemandeComplementMapper,
            KernelInterface kernelInterface,
            CurrentUser currentUser,
            ReferentielInterface referentielInterface,
            SuspensionDossierService suspensionDossierService,
            WorkflowService workflowService,
            DossierDrrrsResolver dossierDrrrsResolver
    ) {
        this.reponseDemandeComplementRepository = reponseDemandeComplementRepository;
        this.demandeComplementRepository = demandeComplementRepository;
        this.reponseDemandeComplementMapper = reponseDemandeComplementMapper;
        this.kernelInterface = kernelInterface;
        this.currentUser = currentUser;
        this.referentielInterface = referentielInterface;
        this.suspensionDossierService = suspensionDossierService;
        this.workflowService = workflowService;
        this.dossierDrrrsResolver = dossierDrrrsResolver;
    }

    @Transactional
    public ReponseDemandeComplementDTO save(ReponseDemandeComplementDTO reponseDemandeComplementDTO) {
        ReponseDemandeComplement reponseDemandeComplement = reponseDemandeComplementMapper.toEntity(reponseDemandeComplementDTO);
        reponseDemandeComplement = reponseDemandeComplementRepository.save(reponseDemandeComplement);
        return reponseDemandeComplementMapper.toDto(reponseDemandeComplement);
    }

    @Transactional
    public ReponseDemandeComplementDTO update(Long id, ReponseDemandeComplementDTO reponseDemandeComplementDTO) {
        ReponseDemandeComplement reponseDemandeComplement = reponseDemandeComplementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ReponseDemandeComplement not found"));
        reponseDemandeComplementMapper.partialUpdate(reponseDemandeComplement, reponseDemandeComplementDTO);
        reponseDemandeComplement = reponseDemandeComplementRepository.save(reponseDemandeComplement);
        return reponseDemandeComplementMapper.toDto(reponseDemandeComplement);
    }

    @Transactional
    public Page<ReponseDemandeComplementDTO> findAll(Pageable pageable) {
        return reponseDemandeComplementRepository.findAll(pageable)
                .map(reponseDemandeComplementMapper::toDto);
    }

    @Transactional
    public Optional<ReponseDemandeComplementDTO> findOne(Long id) {
        return reponseDemandeComplementRepository.findById(id)
                .map(reponseDemandeComplementMapper::toDto);
    }

    @Transactional
    public Optional<ReponseDemandeComplementDTO> findByDemandeComplementId(Long demandeComplementId) {
        return reponseDemandeComplementRepository.findByDemandeComplementId(demandeComplementId)
                .map(reponseDemandeComplementMapper::toDto);
    }

    public void delete(Long id) {
        reponseDemandeComplementRepository.deleteById(id);
    }

    /**
     * Crée une réponse avec gestion des attachements - même pattern que firstSubmitDemandeComplement
     * Retourne un ResultDTO avec les informations sur la reprise du dossier ASI.
     */
    @Transactional
    public ReponseDemandeComplementResultDTO createReponseWithAttachements(ReponseDemandeComplementInputDTO inputDTO, AclClass aclClass) throws Exception {
        log.info("createReponseWithAttachements called for demandeComplementId={}, attachements count={}",
                inputDTO.getDemandeComplementId(),
                inputDTO.getAttachements() != null ? inputDTO.getAttachements().size() : 0);

        if (aclClass == null) {
            throw new BadRequestAlertException(DemandeComplementErrors.ACL_CLASS_NOT_FOUND, DemandeComplementErrors.CLASS, DemandeComplementErrors.ACL_CLASS_NOT_FOUND);
        }

        // Vérifier que la demande de complément existe
        DemandeComplement demandeComplement = demandeComplementRepository.findById(inputDTO.getDemandeComplementId())
                .orElseThrow(() -> new BadRequestAlertException(DemandeComplementErrors.OBJECT_NOT_FOUND, DemandeComplementErrors.CLASS, DemandeComplementErrors.OBJECT_NOT_FOUND));

        // Créer ou mettre à jour la réponse
        ReponseDemandeComplement reponseDemandeComplement;
        if (inputDTO.getId() != null) {
            Optional<ReponseDemandeComplement> existing = reponseDemandeComplementRepository.findById(inputDTO.getId());
            if (existing.isPresent()) {
                reponseDemandeComplement = existing.get();
                reponseDemandeComplement.setReponse(inputDTO.getReponse());
                reponseDemandeComplement.setDateReponse(inputDTO.getDateReponse());
                reponseDemandeComplement.setNomIntervenant(inputDTO.getNomIntervenant());
            } else {
                reponseDemandeComplement = new ReponseDemandeComplement();
                reponseDemandeComplement.setReponse(inputDTO.getReponse());
                reponseDemandeComplement.setDateReponse(inputDTO.getDateReponse());
                reponseDemandeComplement.setNomIntervenant(inputDTO.getNomIntervenant());
                reponseDemandeComplement.setDemandeComplement(demandeComplement);
            }
        } else {
            reponseDemandeComplement = new ReponseDemandeComplement();
            reponseDemandeComplement.setReponse(inputDTO.getReponse());
            reponseDemandeComplement.setDateReponse(inputDTO.getDateReponse());
            reponseDemandeComplement.setNomIntervenant(inputDTO.getNomIntervenant());
            reponseDemandeComplement.setDemandeComplement(demandeComplement);
        }

        // Stocker la classId pour éviter d'interroger KernelInterface à chaque lecture
        if (reponseDemandeComplement.getClassId() == null && aclClass != null) {
            reponseDemandeComplement.setClassId(aclClass.getId());
        }

        reponseDemandeComplement = reponseDemandeComplementRepository.save(reponseDemandeComplement);

        // Préparer le DTO pour publicAttachement (même pattern que DemandeComplementOutputDTO)
        ReponseDemandeComplementDTO outputDTO = reponseDemandeComplementMapper.toDto(reponseDemandeComplement);

        // Gestion des attachements - même pattern que firstSubmitDemandeComplement
        if (inputDTO.getAttachements() != null && !inputDTO.getAttachements().isEmpty()) {

            // applySecurity pour les attachements (uniquement si pas encore de workflow sur la demande)
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

            // Mettre à jour le nombre d'attachements sur la demande de complément
            try {
                Long count = kernelInterface.countAttachements(demandeComplement.getId(), aclClass.getId());
                demandeComplement.setNumberOfattachments(count);
                demandeComplementRepository.save(demandeComplement);
            } catch (Exception e) {
                log.error("Error counting attachments", e);
            }
        }

        // === REPRISE DU PROCESS ASI PARENT ===
        RepriseInfo repriseInfo = reprendreProcessAsiSiSuspendu(demandeComplement);

        // Construire le DTO de résultat
        ReponseDemandeComplementResultDTO resultDTO = new ReponseDemandeComplementResultDTO(outputDTO);
        if (repriseInfo != null && repriseInfo.repriseEffectuee) {
            resultDTO.marquerAsiReprie(
                repriseInfo.asiId,
                repriseInfo.processInstanceId,
                repriseInfo.ancienDueDate,
                repriseInfo.nouveauDueDate,
                repriseInfo.dureeSuspensionSecondes,
                repriseInfo.suspensionId
            );
        } else {
            resultDTO.marquerSansReprise(
                repriseInfo != null ? repriseInfo.raison : "Aucune information de reprise disponible"
            );
        }

        return resultDTO;
    }

    /**
     * Classe interne pour transporter les informations de reprise du dossier ASI
     */
    private static class RepriseInfo {
        boolean repriseEffectuee = false;
        String raison;
        Long asiId;
        String processInstanceId;
        ZonedDateTime ancienDueDate;
        ZonedDateTime nouveauDueDate;
        Long dureeSuspensionSecondes;
        Long suspensionId;

        static RepriseInfo echec(String raison) {
            RepriseInfo info = new RepriseInfo();
            info.repriseEffectuee = false;
            info.raison = raison;
            return info;
        }

        static RepriseInfo succes(Long asiId, String processInstanceId,
                                   ZonedDateTime ancienDueDate, ZonedDateTime nouveauDueDate,
                                   Long dureeSuspensionSecondes, Long suspensionId) {
            RepriseInfo info = new RepriseInfo();
            info.repriseEffectuee = true;
            info.asiId = asiId;
            info.processInstanceId = processInstanceId;
            info.ancienDueDate = ancienDueDate;
            info.nouveauDueDate = nouveauDueDate;
            info.dureeSuspensionSecondes = dureeSuspensionSecondes;
            info.suspensionId = suspensionId;
            return info;
        }
    }

    /**
     * Reprend le process ASI parent si celui-ci a été suspendu par une demande de complément.
     * Recalcule le dueDate en tenant compte de la durée de suspension.
     * @return Les informations sur la reprise effectuée, ou null si aucune reprise
     */
    private RepriseInfo reprendreProcessAsiSiSuspendu(DemandeComplement demandeComplement) {
        try {
            // Le dossier DRRRS parent : son type et son identifiant sont portes par la demande.
            String typeDossier = demandeComplement.getTypeDossier();
            Long asiId = demandeComplement.getObjectIdDossier();
            if (!dossierDrrrsResolver.estDrrrs(typeDossier) || asiId == null) {
                String msg = "Pas de dossier DRRRS lié à la demande de complément id=" + demandeComplement.getId();
                log.debug(msg);
                return RepriseInfo.echec(msg);
            }
            String wfDossier = dossierDrrrsResolver.wfProcessID(typeDossier, asiId).orElse(null);
            if (wfDossier == null) {
                String msg = "Dossier " + typeDossier + " sans wfProcessID: id=" + asiId;
                log.warn(msg);
                return RepriseInfo.echec(msg);
            }

            // Récupérer la suspension active de l'ASI
            Optional<SuspensionDossier> optSuspension = suspensionDossierService.getSuspensionActive(typeDossier, asiId);
            if (!optSuspension.isPresent()) {
                String msg = "Pas de suspension active pour l'ASI id=" + asiId + " (déjà repris ou jamais suspendu)";
                log.debug(msg);
                return RepriseInfo.echec(msg);
            }

            SuspensionDossier suspension = optSuspension.get();
            log.info("Suspension active trouvée pour ASI id={}, suspensionId={}, dateSuspension={}",
                    asiId, suspension.getId(), suspension.getDateSuspension());

            // Calculer la durée de suspension
            ZonedDateTime now = ZonedDateTime.now();
            long dureeSuspensionSecondes = java.time.temporal.ChronoUnit.SECONDS.between(
                    suspension.getDateSuspension(), now);

            // Calculer le nouveau dueDate
            ZonedDateTime nouveauDueDate = null;
            if (suspension.getDueDateAvantSuspension() != null) {
                nouveauDueDate = suspension.getDueDateAvantSuspension().plusSeconds(dureeSuspensionSecondes);
                log.info("Nouveau dueDate calculé: ancien={}, dureeSuspension={}s, nouveau={}",
                        suspension.getDueDateAvantSuspension(), dureeSuspensionSecondes, nouveauDueDate);
            }

            // Réactiver le process ASI
            workflowService.activateProcessInstance(wfDossier);
            log.info("Process ASI réactivé: asiId={}, wfProcessID={}", asiId, wfDossier);

            // Mettre à jour le dueDate de la tâche active
            if (nouveauDueDate != null) {
                java.util.Date newDueDate = java.util.Date.from(nouveauDueDate.toInstant());
                workflowService.updateDueDateOfActiveTask(wfDossier, newDueDate);
            }

            // Clôturer l'enregistrement de suspension
            suspensionDossierService.cloturerSuspension(suspension.getId(), nouveauDueDate);

            return RepriseInfo.succes(
                asiId,
                wfDossier,
                suspension.getDueDateAvantSuspension(),
                nouveauDueDate,
                dureeSuspensionSecondes,
                suspension.getId()
            );

        } catch (Exception e) {
            String msg = "Erreur lors de la reprise du process ASI pour demandeComplement id=" + demandeComplement.getId();
            log.error(msg, e);
            return RepriseInfo.echec(msg + " : " + e.getMessage());
        }
    }
}
