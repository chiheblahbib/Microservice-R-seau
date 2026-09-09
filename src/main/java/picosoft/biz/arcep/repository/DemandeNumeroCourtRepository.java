package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.numerocourt.DemandeNumeroCourt;

/** Depot de DemandeNumeroCourt. */
@Repository
public interface DemandeNumeroCourtRepository extends JpaRepository<DemandeNumeroCourt, Long>,
        JpaSpecificationExecutor<DemandeNumeroCourt> {
}
