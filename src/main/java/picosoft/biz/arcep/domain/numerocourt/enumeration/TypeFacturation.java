package picosoft.biz.arcep.domain.numerocourt.enumeration;

/**
 * Rubrique 4 : comment l'appelant est facture.
 *
 * N'a de sens QUE si le mode d'exploitation est NORMAL : un numero vert ne
 * facture pas l'appelant, la question ne se pose pas.
 */
public enum TypeFacturation {
    TARIF_OPERATEURS,
    TARIF_SURTAXE
}
