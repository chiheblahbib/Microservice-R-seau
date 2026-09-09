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
import picosoft.biz.arcep.domain.implantation.SiteImplantation;
import picosoft.biz.arcep.domain.implantation.SiteImplantation_;
import picosoft.biz.arcep.repository.SiteImplantationRepository;
import picosoft.biz.arcep.service.criteria.SiteImplantationCriteria;
import picosoft.biz.arcep.service.dto.SiteImplantationDTO;
import picosoft.biz.arcep.service.mapper.SiteImplantationMapper;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * Service for executing complex queries for {@link SiteImplantation} entities in the database.
 * Sous-entite : pas de filtrage ACL propre, la securite se joue sur la station porteuse.
 */
@Service
@Transactional(readOnly = true)
public class SiteImplantationQueryService extends QueryService<SiteImplantation> {

    private final Logger log = LoggerFactory.getLogger(SiteImplantationQueryService.class);

    private final SiteImplantationRepository siteImplantationRepository;
    private final SiteImplantationMapper siteImplantationMapper;

    public SiteImplantationQueryService(SiteImplantationRepository siteImplantationRepository, SiteImplantationMapper siteImplantationMapper) {
        this.siteImplantationRepository = siteImplantationRepository;
        this.siteImplantationMapper = siteImplantationMapper;
    }

    @Transactional(readOnly = true)
    public List<SiteImplantation> findByCriteria(SiteImplantationCriteria criteria) {
        return siteImplantationRepository.findAll(createSpecification(criteria));
    }

    @Transactional(readOnly = true)
    public Page<SiteImplantationDTO> findByCriteria(SiteImplantationCriteria criteria, Pageable page, Integer size) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        if (size != null && size == 0) {
            page = PageRequest.of(0, Integer.MAX_VALUE, page.getSort());
        }
        return siteImplantationRepository.findAll(createSpecification(criteria), page).map(siteImplantationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long countByCriteria(SiteImplantationCriteria criteria) {
        return siteImplantationRepository.count(createSpecification(criteria));
    }

    protected Specification<SiteImplantation> createSpecification(SiteImplantationCriteria criteria) {
        Specification<SiteImplantation> specification = Specification.where(null);
        if (criteria == null) {
            return specification;
        }
        if (criteria.getId() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getId(), SiteImplantation_.id));
        }
        if (criteria.getNomSite() != null) {
            specification = specification.and(buildStringSpecification(criteria.getNomSite(), SiteImplantation_.nomSite));
        }
        if (criteria.getProvince() != null) {
            specification = specification.and(buildStringSpecification(criteria.getProvince(), SiteImplantation_.province));
        }
        if (criteria.getVilleQuartier() != null) {
            specification = specification.and(buildStringSpecification(criteria.getVilleQuartier(), SiteImplantation_.villeQuartier));
        }
        if (criteria.getDepartementCantonVillage() != null) {
            specification = specification.and(buildStringSpecification(criteria.getDepartementCantonVillage(), SiteImplantation_.departementCantonVillage));
        }
        if (criteria.getAltitude() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getAltitude(), SiteImplantation_.altitude));
        }
        if (criteria.getPylonePresent() != null) {
            specification = specification.and(buildSpecification(criteria.getPylonePresent(), SiteImplantation_.pylonePresent));
        }
        if (criteria.getZoneClassee() != null) {
            specification = specification.and(buildSpecification(criteria.getZoneClassee(), SiteImplantation_.zoneClassee));
        }
        if (criteria.getStationId() != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.join(SiteImplantation_.station, JoinType.LEFT).get("id"),
                             criteria.getStationId().getEquals()));
        }
        if (criteria.getSysdateCreated() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSysdateCreated(), SiteImplantation_.sysdateCreated));
        }
        if (criteria.getSysdateUpdated() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSysdateUpdated(), SiteImplantation_.sysdateUpdated));
        }
        if (criteria.getSyscreatedBy() != null) {
            specification = specification.and(buildStringSpecification(criteria.getSyscreatedBy(), SiteImplantation_.syscreatedBy));
        }
        if (criteria.getSysupdatedBy() != null) {
            specification = specification.and(buildStringSpecification(criteria.getSysupdatedBy(), SiteImplantation_.sysupdatedBy));
        }
        return specification;
    }
}
