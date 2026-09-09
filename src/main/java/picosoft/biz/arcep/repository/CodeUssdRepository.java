package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.ussd.CodeUssd;

/** Depot de CodeUssd. */
@Repository
public interface CodeUssdRepository extends JpaRepository<CodeUssd, Long>,
        JpaSpecificationExecutor<CodeUssd> {
}
