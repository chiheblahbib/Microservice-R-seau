package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.reseau.TypeReseauDeclare;

/** Depot de TypeReseauDeclare. */
@Repository
public interface TypeReseauDeclareRepository extends JpaRepository<TypeReseauDeclare, Long>,
        JpaSpecificationExecutor<TypeReseauDeclare> {
}
