package picosoft.biz.arcep.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import picosoft.biz.arcep.domain.shared.SuspensionDossier;
import picosoft.biz.arcep.repository.SuspensionDossierRepository;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@Transactional
public class SuspensionDossierService {

    private final Logger log = LoggerFactory.getLogger(SuspensionDossierService.class);

    private final SuspensionDossierRepository suspensionDossierRepository;

    public SuspensionDossierService(SuspensionDossierRepository suspensionDossierRepository) {
        this.suspensionDossierRepository = suspensionDossierRepository;
    }

    /**
     * Crée une suspension pour un dossier (ASI ou autre)
     */
    @Transactional
    public SuspensionDossier creerSuspension(String typeDossier, Long dossierId,
                                              String processInstanceId, ZonedDateTime dueDateAvantSuspension,
                                              String motif, String createdBy) {
        SuspensionDossier suspension = new SuspensionDossier();
        suspension.setTypeDossier(typeDossier);
        suspension.setDossierId(dossierId);
        suspension.setProcessInstanceId(processInstanceId);
        suspension.setDateSuspension(ZonedDateTime.now());
        suspension.setDueDateAvantSuspension(dueDateAvantSuspension);
        suspension.setMotif(motif);
        suspension.setCreatedBy(createdBy);

        SuspensionDossier saved = suspensionDossierRepository.save(suspension);
        log.info("Suspension créée: id={}, typeDossier={}, dossierId={}, processInstanceId={}, dateSuspension={}",
                saved.getId(), typeDossier, dossierId, processInstanceId, saved.getDateSuspension());
        return saved;
    }

    /**
     * Récupère la suspension active (non reprise) pour un dossier
     */
    @Transactional(readOnly = true)
    public Optional<SuspensionDossier> getSuspensionActive(String typeDossier, Long dossierId) {
        return suspensionDossierRepository
                .findFirstByTypeDossierAndDossierIdAndDateRepriseIsNullOrderByDateSuspensionDesc(typeDossier, dossierId);
    }

    /**
     * Récupère la suspension active par processInstanceId
     */
    @Transactional(readOnly = true)
    public Optional<SuspensionDossier> getSuspensionActiveByProcessInstanceId(String processInstanceId) {
        return suspensionDossierRepository
                .findFirstByProcessInstanceIdAndDateRepriseIsNullOrderByDateSuspensionDesc(processInstanceId);
    }

    /**
     * Clôture une suspension en calculant la durée et mettant à jour le dueDate
     */
    @Transactional
    public SuspensionDossier cloturerSuspension(Long suspensionId, ZonedDateTime dueDateApresReprise) {
        Optional<SuspensionDossier> opt = suspensionDossierRepository.findById(suspensionId);
        if (!opt.isPresent()) {
            log.warn("Suspension non trouvée pour cloture: id={}", suspensionId);
            return null;
        }

        SuspensionDossier suspension = opt.get();
        ZonedDateTime now = ZonedDateTime.now();
        suspension.setDateReprise(now);

        long dureeSecondes = ChronoUnit.SECONDS.between(suspension.getDateSuspension(), now);
        suspension.setDureeSuspensionSecondes(dureeSecondes);
        suspension.setDueDateApresReprise(dueDateApresReprise);

        SuspensionDossier saved = suspensionDossierRepository.save(suspension);
        log.info("Suspension cloturée: id={}, duree={} secondes, dueDateApresReprise={}",
                saved.getId(), dureeSecondes, dueDateApresReprise);
        return saved;
    }

    /**
     * Calcule le nouveau dueDate en ajoutant la durée de suspension
     */
    public ZonedDateTime calculerNouveauDueDate(ZonedDateTime dueDateInitial, long dureeSuspensionSecondes) {
        if (dueDateInitial == null) {
            return null;
        }
        return dueDateInitial.plusSeconds(dureeSuspensionSecondes);
    }
}
