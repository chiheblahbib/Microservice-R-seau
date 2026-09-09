package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.numerocourt.PersonneNumeroCourt;

/** Depot de PersonneNumeroCourt. */
@Repository
public interface PersonneNumeroCourtRepository extends JpaRepository<PersonneNumeroCourt, Long>,
        JpaSpecificationExecutor<PersonneNumeroCourt> {
}
