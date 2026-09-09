package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.pq.DemandePq;

/** Depot de DemandePq. */
@Repository
public interface DemandePqRepository extends JpaRepository<DemandePq, Long>,
        JpaSpecificationExecutor<DemandePq> {
}
