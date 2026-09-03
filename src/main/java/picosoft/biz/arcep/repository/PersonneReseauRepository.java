package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.reseau.PersonneReseau;

/** Depot de PersonneReseau. */
@Repository
public interface PersonneReseauRepository extends JpaRepository<PersonneReseau, Long>,
        JpaSpecificationExecutor<PersonneReseau> {
}
