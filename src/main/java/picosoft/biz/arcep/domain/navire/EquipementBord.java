package picosoft.biz.arcep.domain.navire;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

/**
 * Une ligne du tableau « Equipements de bord », rubrique 5 du formulaire.
 *
 * Le formulaire papier en offre dix-huit lignes sur deux pages ; rien n'impose
 * cette limite ici, l'ecran en ajoute autant que necessaire.
 *
 * Les grandeurs restent des CHAINES et non des nombres : le formulaire ne fixe
 * ni unite ni format. « 25 W », « 5 W PEP », « 118-137 MHz » sont tous des
 * saisies legitimes, et les convertir en decimal obligerait a inventer une
 * unite que l'operateur n'a pas donnee.
 */
@Getter
@Setter
@NoArgsConstructor
// NOM D'ENTITE QUALIFIE : le nom JPA est GLOBAL au service, et deux classes
// homonymes vivent dans deux paquets depuis la fusion des douze. Sans cela,
// Hibernate refuse de demarrer -- DuplicateMappingException.
@Entity(name = "EquipementBordNavire")
@Table(name = "equipement_bord_navire", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class EquipementBord extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "uuid", updatable = false)
    private UUID uuid;

    @Column(name = "designation")
    private String designation;

    @Column(name = "marque")
    private String marque;

    @Column(name = "type_materiel")
    private String typeMateriel;

    @Column(name = "puissance")
    private String puissance;

    @Column(name = "bandes_frequences")
    private String bandesFrequences;

    /**
     * Presence de l'AIS et de l'ASN sur cet equipement.
     *
     * Le formulaire ouvre une colonne « AIS/ASN » avec la note « Presence du
     * AIS (Systeme d'identification automatique) et ASN (Appel Selectif
     * Numerique) a renseigner ». Il ne dit pas comment : deux cases, une
     * mention, un numero. On garde donc du TEXTE LIBRE plutot que deux
     * booleens -- les booleens auraient impose une lecture que le formulaire
     * ne prescrit pas, et perdu un numero MMSI si l'armateur en inscrit un.
     *
     * Propre au navire : l'aeronef n'a pas cette colonne.
     */
    @Column(name = "ais_asn")
    private String aisAsn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_navire_id")
    private DemandeNavire demandeNavire;

    @PrePersist
    public void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }
}
