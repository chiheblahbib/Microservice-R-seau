package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.drrrs.RapportTechnique;

/** Depot du rapport technique d'instruction, commun aux dix formulaires qui en produisent un. */
@Repository
public interface RapportTechniqueRepository extends JpaRepository<RapportTechnique, Long>,
        JpaSpecificationExecutor<RapportTechnique> {
}
