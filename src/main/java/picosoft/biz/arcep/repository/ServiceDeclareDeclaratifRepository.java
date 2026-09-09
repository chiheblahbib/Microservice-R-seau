package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.declaratif.ServiceDeclare;

/** Depot de ServiceDeclare. */
@Repository
public interface ServiceDeclareDeclaratifRepository extends JpaRepository<ServiceDeclare, Long>,
        JpaSpecificationExecutor<ServiceDeclare> {
}
