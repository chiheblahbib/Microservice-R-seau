package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.ispc.DemandeIspc;

/** Depot de DemandeIspc. */
@Repository
public interface DemandeIspcRepository extends JpaRepository<DemandeIspc, Long>,
        JpaSpecificationExecutor<DemandeIspc> {
}
