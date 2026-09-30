package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.shared.DemandeComplement;

import java.util.List;

@Repository
public interface DemandeComplementRepository extends JpaRepository<DemandeComplement, Long>, JpaSpecificationExecutor<DemandeComplement> {

    List<DemandeComplement> findByAsiId(Long asiId);
}
