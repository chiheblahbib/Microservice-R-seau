package picosoft.biz.arcep.domain.pq;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Un bloc BPQ sollicite ou restitue, rubrique 4.
 *
 * Le formulaire rappelle qu'au 6 avril 2024 le plan de numerotation national
 * est passe de 8 a 9 chiffres, au format A X B P Q M C D U -- et demande de
 * lister les BPQ souhaites. Il ouvre trois lignes libres ; on n'en fait pas
 * une limite, c'est de la place sur une page.
 *
 * Le bloc reste une CHAINE et non un entier : « 062 » et « 62 » ne sont pas le
 * meme bloc, et un entier perdrait le zero de tete -- exactement la partie qui
 * distingue un operateur d'un autre.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bloc_numeros", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BlocNumeros implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Le BPQ tel que l'operateur l'ecrit. */
    @Column(name = "bloc")
    private String bloc;

    /**
     * Rang de preference, si l'operateur en exprime un.
     *
     * Le formulaire ne le demande pas explicitement, mais il fait LISTER les
     * blocs : l'ordre de la liste porte une intention qu'un ensemble non
     * ordonne perdrait. On la rend explicite plutot que de dependre de l'ordre
     * d'insertion, que rien ne garantit apres un aller-retour en base.
     */
    @Column(name = "rang")
    private Integer rang;

    /**
     * Ce que l'ARCEP a reellement attribue, s'il differe du demande.
     *
     * Vide au depot : c'est le resultat de l'instruction. La demande et la
     * decision sont gardees COTE A COTE plutot que l'une ecrasant l'autre --
     * savoir qu'un operateur a demande 062 et recu 063 fait partie du dossier.
     */
    @Column(name = "bloc_attribue")
    private String blocAttribue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_pq_id")
    private DemandePq demandePq;
}
