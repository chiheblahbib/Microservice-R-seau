package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.mmsi.DemandeMmsi;

/** Depot de DemandeMmsi. */
@Repository
public interface DemandeMmsiRepository extends JpaRepository<DemandeMmsi, Long>,
        JpaSpecificationExecutor<DemandeMmsi> {
}
