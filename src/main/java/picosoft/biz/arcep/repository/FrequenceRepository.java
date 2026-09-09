package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.implantation.Frequence;

import java.util.List;

@Repository
public interface FrequenceRepository extends JpaRepository<Frequence, Long>, JpaSpecificationExecutor<Frequence> {

    List<Frequence> findByStationId(Long stationId);
}
