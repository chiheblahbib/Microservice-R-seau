package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.navire.AutorisationAnterieure;

/** Depot de AutorisationAnterieure. */
@Repository
public interface AutorisationAnterieureRepository extends JpaRepository<AutorisationAnterieure, Long>,
        JpaSpecificationExecutor<AutorisationAnterieure> {
}
