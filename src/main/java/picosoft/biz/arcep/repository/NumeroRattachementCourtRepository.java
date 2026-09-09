package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.numerocourt.NumeroRattachement;

/** Depot de NumeroRattachement. */
@Repository
public interface NumeroRattachementCourtRepository extends JpaRepository<NumeroRattachement, Long>,
        JpaSpecificationExecutor<NumeroRattachement> {
}
