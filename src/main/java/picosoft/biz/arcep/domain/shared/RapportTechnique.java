package picosoft.biz.arcep.domain.shared;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.aeronef.DemandeAeronef;
import picosoft.biz.arcep.domain.declaratif.DemandeDeclaratif;
import picosoft.biz.arcep.domain.installateur.DemandeInstallateur;
import picosoft.biz.arcep.domain.ispc.DemandeIspc;
import picosoft.biz.arcep.domain.mmsi.DemandeMmsi;
import picosoft.biz.arcep.domain.navire.DemandeNavire;
import picosoft.biz.arcep.domain.numerocourt.DemandeNumeroCourt;
import picosoft.biz.arcep.domain.numerocourturgence.DemandeNumeroCourtUrgence;
import picosoft.biz.arcep.domain.pq.DemandePq;
import picosoft.biz.arcep.domain.ussd.DemandeUssd;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Le rapport technique d'instruction du dossier.
 *
 * REPRIS DE homologation, dont il garde les champs et la forme de la relation.
 *
 *
 * UNE SEULE TABLE, DIX CLES ETRANGERES
 *
 * Chaque service en portait une copie dans son propre schema, et l'en-tete de
 * ces copies disait pourquoi : « chaque service est deployable seul, une table
 * partagee creerait une dependance que l'architecture -- un microservice par
 * formulaire -- ne veut pas ». Cette architecture n'est plus : les douze
 * formulaires sont un seul service. La raison de la copie tombe avec elle, et
 * on revient a la forme d'homologation -- une table, une cle etrangere
 * nullable par type de dossier.
 *
 * DIX ET NON DOUZE : l'implantation et le reseau n'ont pas de rapport
 * technique. Leur instruction ne produit pas cette piece.
 *
 *
 * IL PORTE SA PROPRE CLASSE ACL
 *
 * `classId` n'est pas decoratif : c'est ce qui permet au rapport d'avoir SES
 * pieces jointes, distinctes de celles du dossier. Le front interroge le
 * referentiel avec la classe « RapportTechnique » et attache les fichiers a
 * (classId, id) du rapport. La classe doit donc etre declaree au kernel.
 *
 *
 * PAS DE CASCADE REMOVE : un rapport signe est une piece d'instruction, il ne
 * disparait pas avec un nettoyage de dossier. La suppression est de toute
 * facon refusee des que le circuit est engage -- et un rapport n'existe pas
 * avant.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "rapport_technique", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class RapportTechnique extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Numero du rapport, pose par le kernel a l'ouverture. */
    @Column(name = "reference")
    private String reference;

    /**
     * La conclusion de l'instruction.
     *
     * Chaine et non enumeration, comme dans homologation et pour la meme raison
     * que statutDossier : les passerelles du circuit comparent des chaines
     * nues. Le vocabulaire est tenu au front.
     */
    @Column(name = "conclusion")
    private String conclusion;

    /** Qui a arrete la conclusion. */
    @Column(name = "approvedBy")
    private String approvedBy;

    @Column(name = "date_rapport")
    private ZonedDateTime dateRapport;

    /** Sa classe ACL propre -- voir l'en-tete. */
    @Column(name = "class_id")
    private Long classId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_aeronef_id", nullable = true, unique = true)
    private DemandeAeronef demandeAeronef;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_declaratif_id", nullable = true, unique = true)
    private DemandeDeclaratif demandeDeclaratif;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_installateur_id", nullable = true, unique = true)
    private DemandeInstallateur demandeInstallateur;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_ispc_id", nullable = true, unique = true)
    private DemandeIspc demandeIspc;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_mmsi_id", nullable = true, unique = true)
    private DemandeMmsi demandeMmsi;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_navire_id", nullable = true, unique = true)
    private DemandeNavire demandeNavire;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_numerocourt_id", nullable = true, unique = true)
    private DemandeNumeroCourt demandeNumeroCourt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_numerocourturgence_id", nullable = true, unique = true)
    private DemandeNumeroCourtUrgence demandeNumeroCourtUrgence;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_pq_id", nullable = true, unique = true)
    private DemandePq demandePq;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_ussd_id", nullable = true, unique = true)
    private DemandeUssd demandeUssd;
}
