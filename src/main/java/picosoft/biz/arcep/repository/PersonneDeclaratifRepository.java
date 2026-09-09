package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.declaratif.PersonneDeclaratif;

/** Depot de PersonneDeclaratif. */
@Repository
public interface PersonneDeclaratifRepository extends JpaRepository<PersonneDeclaratif, Long>,
        JpaSpecificationExecutor<PersonneDeclaratif> {
}
