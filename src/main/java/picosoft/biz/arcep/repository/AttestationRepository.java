package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.shared.Attestation;

import java.util.List;

@Repository
public interface AttestationRepository extends JpaRepository<Attestation, Long>, JpaSpecificationExecutor<Attestation> {

    /**
     * Les autorisations d'un dossier.
     *
     * Le reseau est autorise D'UN BLOC : contrairement a l'implantation, qui
     * delivrait une autorisation par station, la cle de regroupement est le
     * dossier lui-meme.
     *
     * Spring Data resout ce nom AU DEMARRAGE, contre les proprietes de
     * l'entite : la version heritee, findByStationId, faisait echouer tout le
     * contexte Spring alors que le code compilait.
     */
    List<Attestation> findByDemandeReseauId(Long demandeReseauId);
}
