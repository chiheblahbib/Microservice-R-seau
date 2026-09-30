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
 * La PERSONNE PHYSIQUE qui agit pour le compte d'une structure.
 *
 * MOITIE D'UNE PAIRE, et non un acteur isole : `Client` porte la structure --
 * raison sociale, registre de commerce, nature d'activite -- et `Applicant` la
 * personne qui agit pour elle. C'est ainsi que le formulaire d'ASI les emploie,
 * dans UNE SEULE rubrique `SectionDemandeur` etalee sur les deux tables :
 * « Identite du demandeur » sur `applicantName`, « Agissant en qualite de » sur
 * `qualification`, mais « Demande pour le compte de la structure » sur
 * `client.company`. Le partage est en partie arbitraire -- le telephone DU
 * DEMANDEUR est range dans `client.phone` -- et les champs de `client` sont
 * masques quand le demandeur est un particulier.
 *
 * DANS DRRRS, l'operateur de la rubrique 1 est le `Client` ; son representant,
 * son responsable, le proprietaire ou le correspondant des rubriques suivantes
 * sont des `Applicant`. Les onze tables `personne_<x>` qui les portaient ont
 * disparu le 9 septembre 2026.
 *
 * L'OBJECTION QU'ELLES PORTAIENT etait qu'`applicant` decrit une STRUCTURE
 * quand l'imprime demande une personne, et qu'il n'a pas de champ de PRENOM.
 * La reponse est celle d'ASI, qui n'en a pas davantage : nom et prenoms se
 * fondent dans `applicantName`. Le reste se retrouve champ pour champ --
 * `fonction` sur `qualification`, `adressePermanente` sur `address`,
 * `telephone` sur `phone`, `nationalite` sur `nationality`.
 *
 * UN-A-UN, comme chez ASI : un dossier n'a qu'une personne. Les imprimes qui
 * semblaient en nommer plusieurs -- le requerant et le responsable du reseau,
 * le demandeur et le proprietaire du navire, les correspondants du declaratif
 * -- designent la MEME, sous le titre que leur rubrique lui donne. Il n'y a
 * donc pas de role a porter, et pas de tiers a loger ailleurs.
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

    @Column(name = "qualification", nullable = true)
    private String qualification;

    @Column(name = "applicant_name", nullable = true)
    private String applicantName;

    @Column(name = "company")
    private String company;

    @Column(name = "trade_register_number", nullable = true)
    private String tradeRegisterNumber;

    @Column(name = "nationality", nullable = true)
    private String nationality;

    @Column(name = "nationality_complet", nullable = true)
    private String nationalityComplet;

    @Column(name = "address", nullable = true)
    private String address;

    @Column(name = "phone", nullable = true)
    private String phone;

    @Column(name = "fax")
    private String fax;

    @Column(name = "email", nullable = true)
    private String email;

    @Column(name = "website")
    private String website;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_aeronef_id", nullable = true)
    private DemandeAeronef demandeAeronef;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_declaratif_id", nullable = true)
    private DemandeDeclaratif demandeDeclaratif;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_implantation_id", nullable = true)
    private DemandeImplantation demandeImplantation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_installateur_id", nullable = true)
    private DemandeInstallateur demandeInstallateur;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_ispc_id", nullable = true)
    private DemandeIspc demandeIspc;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_mmsi_id", nullable = true)
    private DemandeMmsi demandeMmsi;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_navire_id", nullable = true)
    private DemandeNavire demandeNavire;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_numerocourt_id", nullable = true)
    private DemandeNumeroCourt demandeNumeroCourt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_numerocourturgence_id", nullable = true)
    private DemandeNumeroCourtUrgence demandeNumeroCourtUrgence;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_pq_id", nullable = true)
    private DemandePq demandePq;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_reseau_id", nullable = true)
    private DemandeReseau demandeReseau;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_ussd_id", nullable = true)
    private DemandeUssd demandeUssd;
}
