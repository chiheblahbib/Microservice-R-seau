package picosoft.biz.arcep.service;

import picosoft.biz.arcep.service.dto.ApplicantDTO;

import java.util.List;

/**
 * Retrouver une personne d'un dossier par son role.
 *
 * Depuis le 9 septembre 2026, les douze formulaires rangent TOUTES leurs
 * personnes physiques dans `applicant`, la table partagee : requerant,
 * responsable, representant, correspondant. Ce qui les distingue n'est plus
 * une relation ni une table, mais la colonne `role` de la ligne.
 *
 * Les libelles de role sont ceux que chaque imprime emploie -- le declaratif
 * nomme un correspondant de DECLARATION et un de PAIEMENT, l'installateur un
 * REPRESENTANT et un RESPONSABLE, le navire un DEMANDEUR. On ne les a pas
 * uniformises : ils viennent des formulaires, et deux imprimes n'appellent pas
 * la meme chose du meme nom.
 *
 * CHAINES et non enumeration, pour la meme raison que `statutDossier` : une
 * rubrique qui ajoute un role ne doit pas obliger a republier le service.
 */
public final class Acteurs {

    private Acteurs() {
    }

    /** Le role du demandeur, commun aux douze formulaires. */
    public static final String REQUERANT = "REQUERANT";

    public static final String RESPONSABLE = "RESPONSABLE";
    public static final String REPRESENTANT = "REPRESENTANT";
    public static final String CONTACT = "CONTACT";
    public static final String DEMANDEUR = "DEMANDEUR";
    public static final String DECLARATION = "DECLARATION";
    public static final String PAIEMENT = "PAIEMENT";

    /**
     * La premiere personne portant ce role, ou null.
     *
     * PREMIERE et non unique : rien n'interdit au front d'en envoyer deux, et
     * refuser le dossier pour cela serait plus severe que l'imprime. La
     * validation qui suit dira si celle-ci est complete.
     */
    public static ApplicantDTO parRole(List<ApplicantDTO> personnes, String role) {
        if (personnes == null) {
            return null;
        }
        return personnes.stream()
                .filter(p -> p != null && role.equals(p.getRole()))
                .findFirst()
                .orElse(null);
    }

    /** Vrai si une personne de ce role est nommee -- presente ET identifiee. */
    public static boolean nomme(List<ApplicantDTO> personnes, String role) {
        ApplicantDTO p = parRole(personnes, role);
        return p != null && p.getApplicantName() != null
                && !p.getApplicantName().trim().isEmpty();
    }
}
