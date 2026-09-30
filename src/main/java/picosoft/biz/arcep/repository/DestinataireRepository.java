package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.shared.Destinataire;

import java.util.Optional;

@Repository
public interface DestinataireRepository extends JpaRepository<Destinataire, Long> {

    Optional<Destinataire> findByDemandeComplementId(Long demandeComplementId);
}
