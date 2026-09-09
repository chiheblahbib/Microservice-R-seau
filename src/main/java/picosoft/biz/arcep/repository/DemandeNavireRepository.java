package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.navire.DemandeNavire;

/** Depot de DemandeNavire. */
@Repository
public interface DemandeNavireRepository extends JpaRepository<DemandeNavire, Long>,
        JpaSpecificationExecutor<DemandeNavire> {
}
