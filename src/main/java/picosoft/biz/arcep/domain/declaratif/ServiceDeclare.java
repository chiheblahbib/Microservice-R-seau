package picosoft.biz.arcep.domain.declaratif;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import picosoft.biz.arcep.domain.declaratif.enumeration.TypeServiceDeclare;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Un service declare, rubrique 7.
 *
 * Le formulaire liste treize services et ouvre a cote de CHACUN une ligne pour
 * « preciser la nature des services fournis en precisant le calendrier de mise
 * en service ». Un service coche porte donc deux informations, pas une : d'ou
 * une entite, et non treize colonnes booleennes qui auraient perdu le
 * calendrier.
 */
@Getter
@Setter
@NoArgsConstructor
// NOM D'ENTITE QUALIFIE : le nom JPA est GLOBAL au service, et deux classes
// homonymes vivent dans deux paquets depuis la fusion des douze. Sans cela,
// Hibernate refuse de demarrer -- DuplicateMappingException.
@Entity(name = "ServiceDeclareDeclaratif")
@Table(name = "service_declare_declaratif", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ServiceDeclare implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_service", length = 40)
    private TypeServiceDeclare typeService;

    /** N'a de sens que si typeService vaut AUTRE : le formulaire dit « preciser ». */
    @Column(name = "precision_autre", length = 255)
    @Size(max = 255)
    private String precisionAutre;

    /**
     * Le calendrier de mise en service, tel que l'exploitant l'ecrit.
     *
     * TEXTE LIBRE : le formulaire ne fixe aucun format, et « T3 2026 »,
     * « des l'obtention du certificat » ou une date precise y sont tous
     * recevables. Une colonne de date aurait refuse les deux premiers.
     */
    @Column(name = "calendrier", length = 255)
    @Size(max = 255)
    private String calendrier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_declaratif_id")
    private DemandeDeclaratif demandeDeclaratif;
}
