package picosoft.biz.arcep.domain.numerocourt;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Un numero long ou fixe auquel le numero court est rattache, rubrique 4.
 *
 * Le formulaire ouvre cinq paires de lignes -- dix emplacements. On n'en fait
 * pas une limite : c'est de la place sur une page, pas une regle. Un operateur
 * qui rattache douze lignes doit pouvoir les declarer.
 *
 * CHAINE et non entier : un numero de telephone garde ses zeros de tete et ses
 * separateurs, et ne se calcule jamais.
 */
@Getter
@Setter
@NoArgsConstructor
// NOM D'ENTITE QUALIFIE : le nom JPA est GLOBAL au service, et deux classes
// homonymes vivent dans deux paquets depuis la fusion des douze. Sans cela,
// Hibernate refuse de demarrer -- DuplicateMappingException.
@Entity(name = "NumeroRattachementCourt")
@Table(name = "numero_rattachement_court", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class NumeroRattachement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", length = 32)
    @Size(max = 32)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_numerocourt_id")
    private DemandeNumeroCourt demandeNumeroCourt;
}
