package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.declaratif.DemandeDeclaratif;

/** Depot de DemandeDeclaratif. */
@Repository
public interface DemandeDeclaratifRepository extends JpaRepository<DemandeDeclaratif, Long>,
        JpaSpecificationExecutor<DemandeDeclaratif> {
}
