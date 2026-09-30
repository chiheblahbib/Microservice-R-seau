package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.shared.SuspensionDossier;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuspensionDossierRepository extends JpaRepository<SuspensionDossier, Long> {

    Optional<SuspensionDossier> findFirstByTypeDossierAndDossierIdAndDateRepriseIsNullOrderByDateSuspensionDesc(
            String typeDossier, Long dossierId);

    List<SuspensionDossier> findByTypeDossierAndDossierIdOrderByDateSuspensionDesc(
            String typeDossier, Long dossierId);

    Optional<SuspensionDossier> findFirstByProcessInstanceIdAndDateRepriseIsNullOrderByDateSuspensionDesc(
            String processInstanceId);
}
