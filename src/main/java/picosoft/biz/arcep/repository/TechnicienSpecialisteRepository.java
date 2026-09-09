package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.installateur.TechnicienSpecialiste;

/** Depot de TechnicienSpecialiste. */
@Repository
public interface TechnicienSpecialisteRepository extends JpaRepository<TechnicienSpecialiste, Long>,
        JpaSpecificationExecutor<TechnicienSpecialiste> {
}
