package picosoft.biz.arcep.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import picosoft.biz.arcep.controller.errors.BadRequestAlertException;
import picosoft.biz.arcep.controller.errors.ReseauErrors;
import picosoft.biz.arcep.domain.shared.RefTarif;
import picosoft.biz.arcep.repository.RefTarifRepository;
import picosoft.biz.arcep.service.criteria.RefTarifCriteria;
import picosoft.biz.arcep.service.dto.RefTarifDTO;
import picosoft.biz.arcep.service.mapper.RefTarifMapper;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Barème tarifaire.
 *
 * La table est livrée VIDE : la grille service vers montant de l'annexe tarifaire
 * doit être relue à l'œil sur le PDF avant saisie, l'extraction automatique étant
 * désalignée. Coder une grille fausse serait pire que ne rien coder.
 *
 * Codes attendus, dont deux montants sont déjà certains :
 *   FRAIS_ETUDE_DOSSIER      au dépôt
 *   REDEVANCE_IMPLANTATION   à la délivrance
 *   CONTROLE_ANNUEL          par station et par an, variable selon le service
 */
@Service
@Transactional
public class RefTarifService {

    public static final String FRAIS_ETUDE_DOSSIER = "FRAIS_ETUDE_DOSSIER";
    public static final String REDEVANCE_IMPLANTATION = "REDEVANCE_IMPLANTATION";
    public static final String CONTROLE_ANNUEL = "CONTROLE_ANNUEL";

    private final Logger log = LoggerFactory.getLogger(RefTarifService.class);

    private final RefTarifRepository refTarifRepository;
    private final RefTarifQueryService refTarifQueryService;
    private final RefTarifMapper refTarifMapper;

    public RefTarifService(RefTarifRepository refTarifRepository,
                           RefTarifQueryService refTarifQueryService,
                           RefTarifMapper refTarifMapper) {
        this.refTarifRepository = refTarifRepository;
        this.refTarifQueryService = refTarifQueryService;
        this.refTarifMapper = refTarifMapper;
    }

    // ------------------------------------------------------------------ CRUD

    public RefTarifDTO save(RefTarifDTO dto) {
        if (dto.getId() != null) {
            Optional<RefTarif> existant = refTarifRepository.findById(dto.getId());
            if (existant.isPresent()) {
                RefTarif gere = existant.get();
                refTarifMapper.partialUpdate(gere, dto);
                return refTarifMapper.toDto(refTarifRepository.save(gere));
            }
        }
        return refTarifMapper.toDto(refTarifRepository.save(refTarifMapper.toEntity(dto)));
    }

    public RefTarifDTO update(Long id, RefTarifDTO dto) {
        RefTarif entity = refTarifRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(ReseauErrors.OBJECT_NOT_FOUND,
                        ReseauErrors.CLASS, ReseauErrors.OBJECT_NOT_FOUND));
        refTarifMapper.partialUpdate(entity, dto);
        return refTarifMapper.toDto(refTarifRepository.save(entity));
    }

    public Page<RefTarifDTO> findAll(RefTarifCriteria criteria, Pageable pageable, Integer size) {
        return refTarifQueryService.findByCriteria(criteria, pageable, size);
    }

    public Optional<RefTarifDTO> findOne(Long id) {
        return refTarifRepository.findById(id).map(refTarifMapper::toDto);
    }

    public void delete(Long id) {
        refTarifRepository.deleteById(id);
    }

    // ------------------------------------------------------------- lecture

    /**
     * Le tarif applicable à une date donnée, aujourd'hui par défaut.
     *
     * Renvoie vide quand la grille n'est pas encore saisie : c'est le comportement
     * voulu. L'appelant doit traiter ce cas, jamais supposer un montant.
     */
    public Optional<RefTarifDTO> enVigueur(String code, String service, LocalDate date) {
        LocalDate quand = date != null ? date : LocalDate.now();
        List<RefTarif> lignes = refTarifRepository.enVigueur(code, service, quand);

        if (lignes.isEmpty() && service != null) {
            // repli sur le tarif forfaitaire, qui ne depend pas du service
            lignes = refTarifRepository.enVigueur(code, null, quand);
        }
        if (lignes.isEmpty()) {
            log.warn("aucun tarif en vigueur pour code={} service={} au {}", code, service, quand);
            return Optional.empty();
        }
        if (lignes.size() > 1) {
            log.warn("{} tarifs concurrents pour code={} service={} au {} : la date d'effet"
                    + " la plus recente l'emporte", lignes.size(), code, service, quand);
        }
        return Optional.of(refTarifMapper.toDto(lignes.get(0)));
    }

    // ------------------------------------------------------------- revision

    /**
     * Révise un tarif sans perdre l'historique : la ligne en cours est close la veille
     * de la prise d'effet, une ligne neuve est ouverte.
     *
     * Un montant déjà facturé reste ainsi explicable après changement de barème.
     */
    public RefTarifDTO reviser(Long id, BigDecimal montant, LocalDate dateEffet) {
        RefTarif ancien = refTarifRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(ReseauErrors.OBJECT_NOT_FOUND,
                        ReseauErrors.CLASS, ReseauErrors.OBJECT_NOT_FOUND));

        LocalDate effet = dateEffet != null ? dateEffet : LocalDate.now();
        if (ancien.getDateEffet() != null && !effet.isAfter(ancien.getDateEffet())) {
            throw new BadRequestAlertException(ReseauErrors.OBJECT_NOT_VALID,
                    ReseauErrors.CLASS, ReseauErrors.OBJECT_NOT_VALID);
        }

        ancien.setDateFin(effet.minusDays(1));
        refTarifRepository.save(ancien);

        RefTarif nouveau = new RefTarif();
        nouveau.setCode(ancien.getCode());
        nouveau.setLibelle(ancien.getLibelle());
        nouveau.setService(ancien.getService());
        nouveau.setApplication(ancien.getApplication());
        nouveau.setDevise(ancien.getDevise());
        nouveau.setMoment(ancien.getMoment());
        nouveau.setMontant(montant);
        nouveau.setDateEffet(effet);
        nouveau.setActif(true);
        nouveau.setCommentaire("Révision de la ligne #" + ancien.getId());

        return refTarifMapper.toDto(refTarifRepository.save(nouveau));
    }
}
