package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.installateur.PersonneInstallateur;

/** Depot de PersonneInstallateur. */
@Repository
public interface PersonneInstallateurRepository extends JpaRepository<PersonneInstallateur, Long>,
        JpaSpecificationExecutor<PersonneInstallateur> {
}
