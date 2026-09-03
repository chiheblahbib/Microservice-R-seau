package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.reseau.LiaisonReseau;

/** Depot de LiaisonReseau. */
@Repository
public interface LiaisonReseauRepository extends JpaRepository<LiaisonReseau, Long>,
        JpaSpecificationExecutor<LiaisonReseau> {
}
