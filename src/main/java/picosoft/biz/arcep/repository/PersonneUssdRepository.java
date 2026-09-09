package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.ussd.PersonneUssd;

/** Depot de PersonneUssd. */
@Repository
public interface PersonneUssdRepository extends JpaRepository<PersonneUssd, Long>,
        JpaSpecificationExecutor<PersonneUssd> {
}
