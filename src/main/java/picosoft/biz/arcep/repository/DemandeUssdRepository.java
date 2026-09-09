package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.ussd.DemandeUssd;

/** Depot de DemandeUssd. */
@Repository
public interface DemandeUssdRepository extends JpaRepository<DemandeUssd, Long>,
        JpaSpecificationExecutor<DemandeUssd> {
}
