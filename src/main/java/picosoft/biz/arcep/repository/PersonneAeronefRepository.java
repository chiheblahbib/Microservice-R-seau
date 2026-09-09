package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.aeronef.PersonneAeronef;

/** Depot de PersonneAeronef. */
@Repository
public interface PersonneAeronefRepository extends JpaRepository<PersonneAeronef, Long>,
        JpaSpecificationExecutor<PersonneAeronef> {
}
