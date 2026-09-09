package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.numerocourturgence.DemandeNumeroCourtUrgence;

/** Depot de DemandeNumeroCourtUrgence. */
@Repository
public interface DemandeNumeroCourtUrgenceRepository extends JpaRepository<DemandeNumeroCourtUrgence, Long>,
        JpaSpecificationExecutor<DemandeNumeroCourtUrgence> {
}
