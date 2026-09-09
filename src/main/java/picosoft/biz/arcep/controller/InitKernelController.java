package picosoft.biz.arcep.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import picosoft.biz.arcep.client.kernel.intercomm.KernelService;

/**
 * Les declarations au kernel qui valent pour TOUT le service, et non pour un
 * formulaire.
 *
 * POURQUOI ELLES SONT ICI. Chaque formulaire portait sa copie de
 * `/api/initSequences` du temps ou il etait un service a part : il etait seul
 * sur son contexte. Reunis sous `/drrrs`, ils etaient douze a revendiquer le
 * meme chemin, et Spring refuse de demarrer sur une adresse ambigue. Le
 * rapport technique posait le meme probleme a dix voix, et pour une raison de
 * plus : sa classe ACL est desormais UNIQUE, il n'y a plus dix rapports a
 * declarer mais un seul.
 *
 * CE QUI RESTE PAR FORMULAIRE : `/api/initClassDemande<X>`, qui declare la
 * classe ACL d'un dossier precis. Douze chemins distincts, aucun conflit.
 *
 * A INVOQUER UNE FOIS, A LA MAIN, apres un deploiement sur un nouvel
 * environnement. Ce n'est pas un demarrage automatique : rejouer ces appels a
 * chaque lancement reecrirait la configuration du kernel et masquerait une
 * declaration faite entre-temps par un administrateur.
 *
 * AUCUN @PreAuthorize, volontairement, et c'est aussi ce que fait homologation :
 * les roles que ces methodes controleraient sont precisement ceux
 * qu'`initVaraible()` CREE. Les exiger rendrait la declaration impossible -- on
 * ne pourrait jamais franchir le premier appel.
 *
 * CONSEQUENCE A TRAITER AVANT LA PRODUCTION : ces adresses sont ouvertes. Elles
 * doivent etre fermees au public par le reverse proxy, au meme titre que celles
 * d'homologation.
 */
@RestController
@RequestMapping("/api")
public class InitKernelController {

    private final KernelService kernelService;

    public InitKernelController(KernelService kernelService) {
        this.kernelService = kernelService;
    }

    /**
     * Les quinze formats de sequence du service.
     *
     * Sans eux, getSequenceNumberByClass ne peut attribuer de reference, et
     * les dossiers restent sans numero.
     */
    @GetMapping("/initSequences")
    public void initSequences() {
        kernelService.initSequences();
    }

    /**
     * Declare au kernel la classe ACL du rapport technique.
     *
     * UNE SEULE FOIS POUR LES DIX FORMULAIRES qui en produisent un : la table
     * et la classe sont partagees. Comme les autres declarations, elle cree la
     * ligne `acl_class` mais ne pose pas `fw_process` -- le rattachement du
     * circuit reste un second geste, a faire a la main.
     */
    @GetMapping("/initClassRapportTechnique")
    public void initClassRapportTechnique() {
        kernelService.initClassRapportTechnique();
    }
}
