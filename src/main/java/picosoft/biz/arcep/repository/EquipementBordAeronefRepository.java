package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.aeronef.EquipementBord;

/** Depot de EquipementBord. */
@Repository
public interface EquipementBordAeronefRepository extends JpaRepository<EquipementBord, Long>,
        JpaSpecificationExecutor<EquipementBord> {
}
