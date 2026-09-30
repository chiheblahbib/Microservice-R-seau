package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.shared.ReponseDemandeComplement;

import java.util.Optional;

@Repository
public interface ReponseDemandeComplementRepository extends JpaRepository<ReponseDemandeComplement, Long>, JpaSpecificationExecutor<ReponseDemandeComplement> {

    Optional<ReponseDemandeComplement> findByDemandeComplementId(Long demandeComplementId);
}
