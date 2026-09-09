package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.aeronef.VerificationControle;

/** Depot de VerificationControle. */
@Repository
public interface VerificationControleRepository extends JpaRepository<VerificationControle, Long>,
        JpaSpecificationExecutor<VerificationControle> {
}
