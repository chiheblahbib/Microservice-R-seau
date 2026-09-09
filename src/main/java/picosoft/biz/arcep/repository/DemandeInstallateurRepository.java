package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.installateur.DemandeInstallateur;

/** Depot de DemandeInstallateur. */
@Repository
public interface DemandeInstallateurRepository extends JpaRepository<DemandeInstallateur, Long>,
        JpaSpecificationExecutor<DemandeInstallateur> {
}
