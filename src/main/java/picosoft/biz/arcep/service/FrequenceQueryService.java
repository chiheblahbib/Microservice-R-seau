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
import picosoft.biz.arcep.domain.implantation.Frequence;
import picosoft.biz.arcep.domain.implantation.Frequence_;
import picosoft.biz.arcep.repository.FrequenceRepository;
import picosoft.biz.arcep.service.criteria.FrequenceCriteria;
import picosoft.biz.arcep.service.dto.FrequenceDTO;
import picosoft.biz.arcep.service.mapper.FrequenceMapper;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * Service for executing complex queries for {@link Frequence} entities in the database.
 * Sous-entite : pas de filtrage ACL propre, la securite se joue sur la station porteuse.
 */
@Service
@Transactional(readOnly = true)
public class FrequenceQueryService extends QueryService<Frequence> {

    private final Logger log = LoggerFactory.getLogger(FrequenceQueryService.class);

    private final FrequenceRepository frequenceRepository;
    private final FrequenceMapper frequenceMapper;

    public FrequenceQueryService(FrequenceRepository frequenceRepository, FrequenceMapper frequenceMapper) {
        this.frequenceRepository = frequenceRepository;
        this.frequenceMapper = frequenceMapper;
    }

    @Transactional(readOnly = true)
    public List<Frequence> findByCriteria(FrequenceCriteria criteria) {
        return frequenceRepository.findAll(createSpecification(criteria));
    }

    @Transactional(readOnly = true)
    public Page<FrequenceDTO> findByCriteria(FrequenceCriteria criteria, Pageable page, Integer size) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        if (size != null && size == 0) {
            page = PageRequest.of(0, Integer.MAX_VALUE, page.getSort());
        }
        return frequenceRepository.findAll(createSpecification(criteria), page).map(frequenceMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long countByCriteria(FrequenceCriteria criteria) {
        return frequenceRepository.count(createSpecification(criteria));
    }

    protected Specification<Frequence> createSpecification(FrequenceCriteria criteria) {
        Specification<Frequence> specification = Specification.where(null);
        if (criteria == null) {
            return specification;
        }
        if (criteria.getId() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getId(), Frequence_.id));
        }
        if (criteria.getSens() != null) {
            specification = specification.and(buildSpecification(criteria.getSens(), Frequence_.sens));
        }
        if (criteria.getFrequenceCentraleMhz() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getFrequenceCentraleMhz(), Frequence_.frequenceCentraleMhz));
        }
        if (criteria.getBandeMhz() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getBandeMhz(), Frequence_.bandeMhz));
        }
        if (criteria.getZoneServiceKm() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getZoneServiceKm(), Frequence_.zoneServiceKm));
        }
        if (criteria.getParWatts() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getParWatts(), Frequence_.parWatts));
        }
        if (criteria.getStationId() != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.join(Frequence_.station, JoinType.LEFT).get("id"),
                             criteria.getStationId().getEquals()));
        }
        if (criteria.getSysdateCreated() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSysdateCreated(), Frequence_.sysdateCreated));
        }
        if (criteria.getSysdateUpdated() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSysdateUpdated(), Frequence_.sysdateUpdated));
        }
        if (criteria.getSyscreatedBy() != null) {
            specification = specification.and(buildStringSpecification(criteria.getSyscreatedBy(), Frequence_.syscreatedBy));
        }
        if (criteria.getSysupdatedBy() != null) {
            specification = specification.and(buildStringSpecification(criteria.getSysupdatedBy(), Frequence_.sysupdatedBy));
        }
        return specification;
    }
}
