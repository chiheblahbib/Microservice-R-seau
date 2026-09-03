package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;

/** Depot de DemandeReseau. */
@Repository
public interface DemandeReseauRepository extends JpaRepository<DemandeReseau, Long>,
        JpaSpecificationExecutor<DemandeReseau> {
}
