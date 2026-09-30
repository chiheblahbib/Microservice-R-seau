package picosoft.biz.arcep.controller;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import picosoft.biz.arcep.client.currentuser.model.CurrentUser;
import picosoft.biz.arcep.client.kernel.intercomm.KernelInterface;
import picosoft.biz.arcep.client.kernel.intercomm.KernelService;
import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.service.DemandeReseauQueryService;
import picosoft.biz.arcep.service.DemandeReseauService;
import picosoft.biz.arcep.service.criteria.DemandeReseauCriteria;
import picosoft.biz.arcep.service.dto.DemandeReseauDTO;
import picosoft.biz.arcep.service.dto.DemandeReseauInputDTO;
import picosoft.biz.arcep.service.dto.DemandeReseauOutputDTO;

import io.github.jhipster.service.filter.StringFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Dossier de reseau. Mince par construction : il resout l'AclClass
 * et delegue au service, comme les controleurs d'homologation.
 */
@RestController
@RequestMapping("/api")
public class DemandeReseauController {

    private final DemandeReseauService demandeReseauService;
    private final DemandeReseauQueryService demandeReseauQueryService;
    private final KernelInterface kernelInterface;
    private final KernelService kernelService;
    private final CurrentUser currentUser;

    public DemandeReseauController(DemandeReseauService demandeReseauService,
                                         DemandeReseauQueryService demandeReseauQueryService,
                                         KernelInterface kernelInterface,
                                         KernelService kernelService,
                                         CurrentUser currentUser) {
        this.demandeReseauService = demandeReseauService;
        this.demandeReseauQueryService = demandeReseauQueryService;
        this.kernelInterface = kernelInterface;
        this.kernelService = kernelService;
        this.currentUser = currentUser;
    }

    private AclClass aclClass() {
        return kernelInterface.getaclClassByClassName(DemandeReseau.class.getName());
    }

    // ------------------------------------------------------------------ CRUD

    @PostMapping("/demande-reseaux")
    public ResponseEntity<DemandeReseauDTO> create(@RequestBody @Valid DemandeReseauDTO dto) {
        return ResponseEntity.ok(demandeReseauService.save(dto));
    }

    @PutMapping("/demande-reseaux/{id}")
    public ResponseEntity<DemandeReseauDTO> update(@PathVariable Long id,
                                                         @RequestBody @Valid DemandeReseauDTO dto) {
        return ResponseEntity.ok(demandeReseauService.update(id, dto));
    }

    /**
     * `search` est un RACCOURCI, pas une capacite nouvelle.
     *
     * Le filtre existe deja : `DemandeReseauCriteria` porte un `StringFilter
     * search` que le QueryService traduit en OU sur les colonnes principales.
     * Un appelant peut donc deja ecrire `?search.contains=foo`. Ce parametre
     * plat accepte `?search=foo` en plus -- c'est la forme qu'emploie
     * homologation, et celle que le front connait.
     */
    @GetMapping("/demande-reseaux")
    public ResponseEntity<Page<DemandeReseauDTO>> getAll(DemandeReseauCriteria criteria,
                                                               Pageable pageable,
                                                               @RequestParam(value = "size", required = false) Integer size,
                                                               @RequestParam(value = "search", required = false) String search) {
        appliquerRecherche(criteria, search);
        return ResponseEntity.ok(demandeReseauService.findAll(criteria, pageable, size));
    }

    /**
     * Le total, sans la page. Homologation en expose un par liste ; le front
     * s'en sert pour afficher un compteur sans rapatrier de lignes.
     */
    @GetMapping("/demande-reseaux/count")
    public ResponseEntity<Long> countAll(DemandeReseauCriteria criteria,
                                         @RequestParam(value = "search", required = false) String search) {
        appliquerRecherche(criteria, search);
        return ResponseEntity.ok(demandeReseauQueryService.countByCriteria(criteria));
    }

    /**
     * Meme requete, restreinte aux dossiers que l'utilisateur courant a le droit de voir.
     */
    @GetMapping("/demande-reseaux/mine")
    public ResponseEntity<Page<DemandeReseauDTO>> getMine(DemandeReseauCriteria criteria,
                                                                Pageable pageable,
                                                                @RequestParam(value = "size", required = false) Integer size,
                                                                @RequestParam(value = "masks", required = false) List<Integer> masks,
                                                                @RequestParam(value = "search", required = false) String search) {
        appliquerRecherche(criteria, search);
        List<Integer> masquesEffectifs = (masks == null || masks.isEmpty()) ? List.of(1, 2, 4) : masks;
        return ResponseEntity.ok(demandeReseauQueryService.findByCriteriaAcl(
                criteria, pageable, size, currentUser.getSid(), masquesEffectifs));
    }

    @GetMapping("/demande-reseaux/mine/count")
    public ResponseEntity<Long> countMine(DemandeReseauCriteria criteria,
                                          @RequestParam(value = "masks", required = false) List<Integer> masks,
                                          @RequestParam(value = "search", required = false) String search) {
        appliquerRecherche(criteria, search);
        List<Integer> masquesEffectifs = (masks == null || masks.isEmpty()) ? List.of(1, 2, 4) : masks;
        return ResponseEntity.ok(demandeReseauQueryService.countByCriteriaAcl(
                criteria, currentUser.getSid(), masquesEffectifs));
    }

    // ------------------------------------------- les listes nommees d'ASI
    //
    // Elles n'ajoutent AUCUNE capacite : `/demande-reseaux/mine?masks=...`
    // fait deja exactement cela. Ce sont les NOMS qu'emploie homologation, et
    // que le front d'ASI connait -- d'ou leur presence, pour que les ecrans se
    // transposent sans reecrire leurs appels.
    //
    //   findAllDemandeReseaux        -> masques 1, 2 et 4 (tout ce qu'on peut voir)
    //   findAllDemandeReseauxEncours -> masque 2 seul (les dossiers en cours)

    @GetMapping("/findAllDemandeReseaux")
    public ResponseEntity<Page<DemandeReseauDTO>> findAllDemandeReseaux(
            DemandeReseauCriteria criteria, Pageable pageable,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "search", required = false) String search) {
        appliquerRecherche(criteria, search);
        return ResponseEntity.ok(demandeReseauQueryService.findByCriteriaAcl(
                criteria, pageable, size, currentUser.getSid(), masquesTous()));
    }

    @GetMapping("/findAllDemandeReseaux/count")
    public ResponseEntity<Long> countAllDemandeReseaux(
            DemandeReseauCriteria criteria,
            @RequestParam(value = "search", required = false) String search) {
        appliquerRecherche(criteria, search);
        return ResponseEntity.ok(demandeReseauQueryService.countByCriteriaAcl(
                criteria, currentUser.getSid(), masquesTous()));
    }

    @GetMapping("/findAllDemandeReseauxEncours")
    public ResponseEntity<Page<DemandeReseauDTO>> findAllDemandeReseauxEncours(
            DemandeReseauCriteria criteria, Pageable pageable,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "uuid", required = false) String uuid) {
        appliquerRecherche(criteria, search);
        return ResponseEntity.ok(demandeReseauQueryService.findByCriteriaAcl(
                criteria, pageable, size, sidsDe(uuid), masquesEnCours()));
    }

    @GetMapping("/findAllDemandeReseauxEncours/count")
    public ResponseEntity<Long> countAllDemandeReseauxEncours(
            DemandeReseauCriteria criteria,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "uuid", required = false) String uuid) {
        appliquerRecherche(criteria, search);
        return ResponseEntity.ok(demandeReseauQueryService.countByCriteriaAcl(
                criteria, sidsDe(uuid), masquesEnCours()));
    }

    /**
     * Change l'etape du dossier et note qui le traite.
     *
     * En GET, comme chez homologation, bien que la methode ecrive. La forme est
     * heritee ; elle est conservee pour que le front n'ait pas deux conventions
     * a suivre selon le module.
     */
    @GetMapping("/updateStepDemandeReseau")
    public ResponseEntity<DemandeReseauDTO> updateStep(
            @RequestParam(value = "idDossier") Long idDossier,
            @RequestParam(value = "step") Long step,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "sid", required = false) String sid) {
        return ResponseEntity.ok(demandeReseauService.updateStep(idDossier, step, username, sid));
    }

    // ------------------------------------------------------------- internes

    /**
     * `search` est lu a la main (appliquerRecherche) : on l'ote du binding pour
     * que `?search=foo` ne tente pas de construire un StringFilter depuis une
     * chaine. Meme garde que HomologationController.initBinder.
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        if (binder.getTarget() instanceof DemandeReseauCriteria) {
            binder.setDisallowedFields("search");
        }
    }

    /**
     * Les dossiers « en cours » d'un autre employe (filtre du Chef Centre),
     * sinon ceux de l'utilisateur courant -- comme findAllHomologationsEncours.
     */
    private List<String> sidsDe(String uuid) {
        if (uuid != null && !uuid.isBlank()) {
            return kernelInterface.getSids(uuid);
        }
        return currentUser.getSid();
    }

    private void appliquerRecherche(DemandeReseauCriteria criteria, String search) {
        if (search != null && !search.isBlank()) {
            StringFilter sf = new StringFilter();
            sf.setContains(search.trim());
            criteria.setSearch(sf);
        }
    }

    private List<Integer> masquesTous() {
        List<Integer> m = new ArrayList<>();
        m.add(1); m.add(2); m.add(4);
        return m;
    }

    private List<Integer> masquesEnCours() {
        List<Integer> m = new ArrayList<>();
        m.add(2);
        return m;
    }

    @GetMapping("/demande-reseaux/{id}")
    public ResponseEntity<DemandeReseauDTO> getOne(@PathVariable Long id) {
        return demandeReseauService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority(@kernelService.reseau_role_canReadReseau())")
    @GetMapping("/demande-reseau/{id}")
    public DemandeReseauOutputDTO detail(@PathVariable Long id) {
        return demandeReseauService.byId(id);
    }

    @PreAuthorize("hasAuthority(@kernelService.reseau_role_canEditReseau())")
    @DeleteMapping("/demande-reseaux/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        demandeReseauService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- workflow

    @PreAuthorize("hasAuthority(@kernelService.reseau_role_canCreateReseau())")
    @PatchMapping("/firstSubmitDemandeReseau")
    public DemandeReseauOutputDTO firstSubmit(@RequestBody @Valid DemandeReseauInputDTO input) throws Exception {
        // Valide AVANT aclClass() : celui-ci est un appel Feign au kernel. Sans cet
        // ordre, un dossier incomplet exigerait un aller-retour reseau pour se voir
        // refuser, et une panne du kernel masquerait la vraie raison du refus.
        demandeReseauService.exigerPourSoumission(input);
        return demandeReseauService.initAndSubmit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.reseau_role_canEditReseau())")
    @PatchMapping("/submitDemandeReseau")
    public DemandeReseauOutputDTO submit(@RequestBody @Valid DemandeReseauInputDTO input) throws Exception {
        // La validation de depot est faite par le service, et seulement a la saisie.
        return demandeReseauService.submit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.reseau_role_canEditReseau())")
    @PatchMapping("/saveDemandeReseauAsDraft")
    public DemandeReseauOutputDTO saveAsDraft(@RequestBody @Valid DemandeReseauInputDTO input) {
        return demandeReseauService.saveAsDraft(input, aclClass());
    }

    // ------------------------------------------------------------------
    // Declaration du service aupres du kernel
    //
    // A INVOQUER UNE FOIS, A LA MAIN, apres un deploiement sur un nouvel
    // environnement. Ce n'est pas un demarrage automatique : rejouer ces
    // appels a chaque lancement reecrirait la configuration du kernel, et
    // masquerait une declaration faite entre-temps par un administrateur.
    //
    // AUCUN @PreAuthorize, volontairement, et c'est aussi ce que fait
    // homologation : les roles que ces methodes controleraient sont
    // precisement ceux que `initVaraible()` CREE. Les exiger ici rendrait la
    // declaration impossible -- on ne pourrait jamais franchir le premier
    // appel.
    //
    // CONSEQUENCE A TRAITER AVANT LA PRODUCTION : ces trois adresses sont
    // ouvertes. Elles doivent etre fermees au public par le reverse proxy,
    // au meme titre que celles d'homologation.
    // ------------------------------------------------------------------

    /**
     * Declare la classe ACL du DOSSIER, ses variables et ses sequences.
     *
     * L'ordre compte : les variables portent les roles, les sequences
     * alimentent la numerotation, et la classe s'appuie sur les deux.
     */
    @GetMapping("/initClassDemandeReseau")
    public void initClassDemandeReseau() {
        kernelService.initVaraible();
        kernelService.initSequences();
        kernelService.initClassDemandeReseau();
    }
}
