package picosoft.biz.arcep.domain.shared;


import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.aeronef.DemandeAeronef;
import picosoft.biz.arcep.domain.declaratif.DemandeDeclaratif;
import picosoft.biz.arcep.domain.implantation.DemandeImplantation;
import picosoft.biz.arcep.domain.installateur.DemandeInstallateur;
import picosoft.biz.arcep.domain.ispc.DemandeIspc;
import picosoft.biz.arcep.domain.mmsi.DemandeMmsi;
import picosoft.biz.arcep.domain.navire.DemandeNavire;
import picosoft.biz.arcep.domain.numerocourt.DemandeNumeroCourt;
import picosoft.biz.arcep.domain.numerocourturgence.DemandeNumeroCourtUrgence;
import picosoft.biz.arcep.domain.pq.DemandePq;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.domain.ussd.DemandeUssd;
import picosoft.biz.arcep.domain.shared.enumeration.ApplicantType;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "applicant", schema = "homologation")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
/**
 * Une personne physique rattachee a un dossier.
 *
 * LA TABLE PARTAGEE, celle d'ASI et d'homologation, et desormais la SEULE ou
 * DRRRS range ses personnes -- requerant, responsable, representant, contact.
 * Les onze tables `personne_<x>` qui les portaient ont disparu le 9 septembre
 * 2026.
 *
 * L'OBJECTION QU'ELLES PORTAIENT etait qu'`applicant` decrit une STRUCTURE
 * quand l'imprime demande une personne, et qu'il n'a pas de champ de PRENOM.
 * La reponse est celle d'ASI, qui n'en a pas davantage : nom et prenoms se
 * fondent dans `applicantName`. Le reste se retrouve champ pour champ --
 * `fonction` sur `qualification`, `adressePermanente` sur `address`,
 * `telephone` sur `phone`, `nationalite` sur `nationality`.
 *
 * PLUSIEURS-A-UN, et non un-a-un comme chez ASI : un dossier DRRRS porte
 * plusieurs personnes. C'est `role` qui dit laquelle est laquelle.
 */
public class Applicant extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicant_type", nullable = true)
    private ApplicantType applicantType;

    /**
     * Ce que cette personne est POUR CE DOSSIER : REQUERANT, RESPONSABLE,
     * REPRESENTANT, CONTACT...
     *
     * CHAINE et non enumeration, pour la meme raison que `statutDossier` : le
     * vocabulaire varie d'un imprime a l'autre -- le reseau nomme un
     * responsable technique, le declaratif un correspondant de paiement -- et
     * une enumeration aurait oblige a republier le service des qu'une rubrique
     * ajoute un role.
     *
     * NULLE pour homologation et ASI, qui n'ont qu'un demandeur et n'ont donc
     * rien a distinguer. La colonne s'ajoute a leur table sans les concerner.
     */
    @Column(name = "role", length = 32, nullable = true)
    private String role;

    @Column(name = "qualification", length = 100, nullable = true)
    private String qualification;

    @Column(name = "applicant_name", length = 100, nullable = true)
    private String applicantName;

    @Column(name = "company", length = 100)
    private String company;

    @Column(name = "trade_register_number", length = 50, nullable = true)
    private String tradeRegisterNumber;

    @Column(name = "nationality", length = 50, nullable = true)
    private String nationality;

    @Column(name = "nationality_complet", length = 100, nullable = true)
    private String nationalityComplet;

    @Column(name = "address", length = 200, nullable = true)
    private String address;

    @Column(name = "phone", length = 20, nullable = true)
    private String phone;

    @Column(name = "fax", length = 20)
    private String fax;

    @Column(name = "email", length = 100, nullable = true)
    private String email;

    @Column(name = "website", length = 100)
    private String website;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_aeronef_id", nullable = true)
    private DemandeAeronef demandeAeronef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_declaratif_id", nullable = true)
    private DemandeDeclaratif demandeDeclaratif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_implantation_id", nullable = true)
    private DemandeImplantation demandeImplantation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_installateur_id", nullable = true)
    private DemandeInstallateur demandeInstallateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_ispc_id", nullable = true)
    private DemandeIspc demandeIspc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_mmsi_id", nullable = true)
    private DemandeMmsi demandeMmsi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_navire_id", nullable = true)
    private DemandeNavire demandeNavire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_numerocourt_id", nullable = true)
    private DemandeNumeroCourt demandeNumeroCourt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_numerocourturgence_id", nullable = true)
    private DemandeNumeroCourtUrgence demandeNumeroCourtUrgence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_pq_id", nullable = true)
    private DemandePq demandePq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_reseau_id", nullable = true)
    private DemandeReseau demandeReseau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_ussd_id", nullable = true)
    private DemandeUssd demandeUssd;
}
