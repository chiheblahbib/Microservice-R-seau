package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.pq.PersonnePq;

/** Depot de PersonnePq. */
@Repository
public interface PersonnePqRepository extends JpaRepository<PersonnePq, Long>,
        JpaSpecificationExecutor<PersonnePq> {
}
