package picosoft.biz.arcep.domain.ussd;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Un code USSD sollicite, rubrique 5.
 *
 * Le formulaire demande « Code(s) USSD sollicite(s) PAR PREFERENCE » : l'ordre
 * porte donc une intention, qu'on rend explicite par `rang` plutot que de
 * dependre de l'ordre d'insertion, que rien ne garantit apres un aller-retour
 * en base.
 *
 * CHAINE et non entier : un code USSD s'ecrit avec ses signes -- *123#, *4*2#.
 * Un entier perdrait l'etoile, le diese et les zeros de tete, c'est-a-dire
 * tout ce qui en fait un code.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "code_ussd", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CodeUssd implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Le code tel que l'operateur l'ecrit : *123#, *4*2#... */
    @Column(name = "code")
    private String code;

    /** Rang de preference : 1 pour le plus souhaite. */
    @Column(name = "rang")
    private Integer rang;

    /**
     * Ce que l'ARCEP a reellement attribue, s'il differe du demande.
     *
     * Garde A COTE de la demande plutot qu'a sa place : savoir qu'un operateur
     * a demande *123# et recu *124# fait partie du dossier.
     */
    @Column(name = "code_attribue")
    private String codeAttribue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_ussd_id")
    private DemandeUssd demandeUssd;
}
