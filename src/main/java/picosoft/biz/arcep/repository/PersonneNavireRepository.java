package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.navire.PersonneNavire;

/** Depot de PersonneNavire. */
@Repository
public interface PersonneNavireRepository extends JpaRepository<PersonneNavire, Long>,
        JpaSpecificationExecutor<PersonneNavire> {
}
