package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.installateur.QualiteDemandee;

/** Depot de QualiteDemandee. */
@Repository
public interface QualiteDemandeeRepository extends JpaRepository<QualiteDemandee, Long>,
        JpaSpecificationExecutor<QualiteDemandee> {
}
