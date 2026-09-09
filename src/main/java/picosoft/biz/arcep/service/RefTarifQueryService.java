package picosoft.biz.arcep.service;

import io.github.jhipster.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import picosoft.biz.arcep.domain.shared.RefTarif;
import picosoft.biz.arcep.domain.shared.RefTarif_;
import picosoft.biz.arcep.repository.RefTarifRepository;
import picosoft.biz.arcep.service.criteria.RefTarifCriteria;
import picosoft.biz.arcep.service.dto.RefTarifDTO;
import picosoft.biz.arcep.service.mapper.RefTarifMapper;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RefTarifQueryService extends QueryService<RefTarif> {

    private final Logger log = LoggerFactory.getLogger(RefTarifQueryService.class);

    private final RefTarifRepository refTarifRepository;
    private final RefTarifMapper refTarifMapper;

    public RefTarifQueryService(RefTarifRepository refTarifRepository, RefTarifMapper refTarifMapper) {
        this.refTarifRepository = refTarifRepository;
        this.refTarifMapper = refTarifMapper;
    }

    @Transactional(readOnly = true)
    public List<RefTarif> findByCriteria(RefTarifCriteria criteria) {
        return refTarifRepository.findAll(createSpecification(criteria));
    }

    @Transactional(readOnly = true)
    public Page<RefTarifDTO> findByCriteria(RefTarifCriteria criteria, Pageable page, Integer size) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        if (size != null && size == 0) {
            page = PageRequest.of(0, Integer.MAX_VALUE, page.getSort());
        }
        return refTarifRepository.findAll(createSpecification(criteria), page).map(refTarifMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long countByCriteria(RefTarifCriteria criteria) {
        return refTarifRepository.count(createSpecification(criteria));
    }

    protected Specification<RefTarif> createSpecification(RefTarifCriteria criteria) {
        Specification<RefTarif> specification = Specification.where(null);
        if (criteria == null) {
            return specification;
        }
        if (criteria.getId() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getId(), RefTarif_.id));
        }
        if (criteria.getCode() != null) {
            specification = specification.and(buildStringSpecification(criteria.getCode(), RefTarif_.code));
        }
        if (criteria.getLibelle() != null) {
            specification = specification.and(buildStringSpecification(criteria.getLibelle(), RefTarif_.libelle));
        }
        if (criteria.getService() != null) {
            specification = specification.and(buildStringSpecification(criteria.getService(), RefTarif_.service));
        }
        if (criteria.getApplication() != null) {
            specification = specification.and(buildStringSpecification(criteria.getApplication(), RefTarif_.application));
        }
        if (criteria.getMontant() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getMontant(), RefTarif_.montant));
        }
        if (criteria.getDevise() != null) {
            specification = specification.and(buildStringSpecification(criteria.getDevise(), RefTarif_.devise));
        }
        if (criteria.getMoment() != null) {
            specification = specification.and(buildSpecification(criteria.getMoment(), RefTarif_.moment));
        }
        if (criteria.getDateEffet() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getDateEffet(), RefTarif_.dateEffet));
        }
        if (criteria.getDateFin() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getDateFin(), RefTarif_.dateFin));
        }
        if (criteria.getActif() != null) {
            specification = specification.and(buildSpecification(criteria.getActif(), RefTarif_.actif));
        }
        if (criteria.getSysdateCreated() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSysdateCreated(), RefTarif_.sysdateCreated));
        }
        if (criteria.getSysdateUpdated() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSysdateUpdated(), RefTarif_.sysdateUpdated));
        }
        if (criteria.getSyscreatedBy() != null) {
            specification = specification.and(buildStringSpecification(criteria.getSyscreatedBy(), RefTarif_.syscreatedBy));
        }
        if (criteria.getSysupdatedBy() != null) {
            specification = specification.and(buildStringSpecification(criteria.getSysupdatedBy(), RefTarif_.sysupdatedBy));
        }
        return specification;
    }
}
