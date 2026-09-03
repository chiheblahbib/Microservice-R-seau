package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.reseau.RefTarif;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefTarifRepository extends JpaRepository<RefTarif, Long>, JpaSpecificationExecutor<RefTarif> {

    /**
     * Le tarif en vigueur a une date donnee, pour un code et eventuellement un service.
     * Une ligne est en vigueur si elle est active, si sa date d'effet est passee, et
     * si sa date de fin est nulle ou future.
     */
    @Query("select t from RefTarif t where t.code = :code"
         + " and (:service is null or t.service = :service)"
         + " and t.actif = true"
         + " and (t.dateEffet is null or t.dateEffet <= :date)"
         + " and (t.dateFin is null or t.dateFin >= :date)"
         // NULLS LAST est indispensable : la clause ci-dessus admet les lignes
         // sans date d'effet, et PostgreSQL place les NULL EN TETE en tri
         // descendant. Sans lui, une ligne non datee l'emportait sur toute
         // ligne datee -- une revision tarifaire n'aurait jamais pris effet,
         // et le service aurait continue de servir l'ancien montant en silence.
         + " order by t.dateEffet desc nulls last")
    List<RefTarif> enVigueur(@Param("code") String code,
                             @Param("service") String service,
                             @Param("date") LocalDate date);

    Optional<RefTarif> findFirstByCodeAndActifTrue(String code);

    List<RefTarif> findByCode(String code);
}
