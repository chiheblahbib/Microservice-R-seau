package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.implantation.SiteImplantation;

@Repository
public interface SiteImplantationRepository extends JpaRepository<SiteImplantation, Long>, JpaSpecificationExecutor<SiteImplantation> {
}
