package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.ispc.FonctionPointSemaphore;

/** Depot de FonctionPointSemaphore. */
@Repository
public interface FonctionPointSemaphoreRepository extends JpaRepository<FonctionPointSemaphore, Long>,
        JpaSpecificationExecutor<FonctionPointSemaphore> {
}
