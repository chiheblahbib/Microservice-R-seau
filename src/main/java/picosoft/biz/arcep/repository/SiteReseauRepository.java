package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.reseau.SiteReseau;

/** Depot de SiteReseau. */
@Repository
public interface SiteReseauRepository extends JpaRepository<SiteReseau, Long>,
        JpaSpecificationExecutor<SiteReseau> {
}
