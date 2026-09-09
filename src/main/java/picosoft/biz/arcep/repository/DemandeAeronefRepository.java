package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.aeronef.DemandeAeronef;

/** Depot de DemandeAeronef. */
@Repository
public interface DemandeAeronefRepository extends JpaRepository<DemandeAeronef, Long>,
        JpaSpecificationExecutor<DemandeAeronef> {
}
