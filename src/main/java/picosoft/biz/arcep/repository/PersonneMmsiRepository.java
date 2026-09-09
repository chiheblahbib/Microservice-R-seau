package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.mmsi.PersonneMmsi;

/** Depot de PersonneMmsi. */
@Repository
public interface PersonneMmsiRepository extends JpaRepository<PersonneMmsi, Long>,
        JpaSpecificationExecutor<PersonneMmsi> {
}
