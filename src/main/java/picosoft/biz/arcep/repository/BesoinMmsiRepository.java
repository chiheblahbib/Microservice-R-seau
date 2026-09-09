package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.mmsi.BesoinMmsi;

/** Depot de BesoinMmsi. */
@Repository
public interface BesoinMmsiRepository extends JpaRepository<BesoinMmsi, Long>,
        JpaSpecificationExecutor<BesoinMmsi> {
}
