package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.declaratif.InfrastructureDeclaree;

/** Depot de InfrastructureDeclaree. */
@Repository
public interface InfrastructureDeclareeRepository extends JpaRepository<InfrastructureDeclaree, Long>,
        JpaSpecificationExecutor<InfrastructureDeclaree> {
}
