package picosoft.biz.arcep.service;

import io.github.jhipster.service.QueryService;
import picosoft.biz.arcep.controller.errors.BadRequestAlertException;
import picosoft.biz.arcep.controller.errors.StationErrors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import picosoft.biz.arcep.client.kernel.model.acl.*;
import picosoft.biz.arcep.client.kernel.model.acl.repository.AclSidRepository;
import picosoft.biz.arcep.domain.implantation.Station;
import picosoft.biz.arcep.domain.implantation.Station_;
import picosoft.biz.arcep.domain.implantation.SiteImplantation_;
import picosoft.biz.arcep.repository.StationRepository;
import picosoft.biz.arcep.service.criteria.StationCriteria;
import picosoft.biz.arcep.service.dto.StationDTO;
import picosoft.biz.arcep.service.mapper.StationMapper;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.List;

/**
 * Service for executing complex queries for {@link Station} entities in the database.
 * The main input is a {@link StationCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 */
@Service
@Transactional(readOnly = true)
public class StationQueryService extends QueryService<Station> {

    private final Logger log = LoggerFactory.getLogger(StationQueryService.class);

    private final StationRepository stationRepository;
    private final StationMapper stationMapper;
    private final AclSidRepository aclSidRepository;

    public StationQueryService(StationRepository stationRepository, StationMapper stationMapper,
                                AclSidRepository aclSidRepository) {
        this.stationRepository = stationRepository;
        this.stationMapper = stationMapper;
        this.aclSidRepository = aclSidRepository;
    }

    @Transactional(readOnly = true)
    public List<Station> findByCriteria(StationCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        return stationRepository.findAll(createSpecification(criteria));
    }

    @Transactional(readOnly = true)
    public Page<StationDTO> findByCriteria(StationCriteria criteria, Pageable page, Integer size) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        page = normalizeSort(page);
        Specification<Station> specification = applySortJoins(createSpecification(criteria), page);
        if (size != null && size == 0) {
            page = PageRequest.of(0, Integer.MAX_VALUE, page.getSort());
        }
        return stationRepository.findAll(specification, page).map(stationMapper::toDto);
    }

    /**
     * Meme requete, mais restreinte aux objets sur lesquels l'utilisateur courant detient
     * l'un des masques demandes. Le filtrage se fait par sous-requete sur AclEntry.
     */
    @Transactional(readOnly = true)
    public Page<StationDTO> findByCriteriaAcl(StationCriteria criteria, Pageable page, Integer size,
                                               List<String> sidOfCurrentUser, List<Integer> masks) {
        log.debug("find by criteria (acl) : {}, page: {}", criteria, page);
        page = normalizeSort(page);
        Specification<Station> specification =
                applySortJoins(createSpecificationACL(criteria, masks, sidOfCurrentUser), page);
        if (size != null && size == 0) {
            page = PageRequest.of(0, Integer.MAX_VALUE, page.getSort());
        }
        return stationRepository.findAll(specification, page).map(stationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Long countByCriteriaAcl(StationCriteria criteria, List<String> sidOfCurrentUser, List<Integer> masks) {
        return stationRepository.count(createSpecificationACL(criteria, masks, sidOfCurrentUser));
    }

    @Transactional(readOnly = true)
    public long countByCriteria(StationCriteria criteria) {
        return stationRepository.count(createSpecification(criteria));
    }

    protected Specification<Station> createSpecification(StationCriteria criteria) {
        return createSpecification(criteria, Specification.where(null));
    }

    protected Specification<Station> createSpecificationACL(StationCriteria criteria,
                                                             List<Integer> masks, List<String> sids) {
        List<AclSid> aclSids = aclSidRepository.findAllBySidIn(sids);

        Specification<Station> acl = (root, query, cb) -> {
            Predicate oidNotNull = cb.isNotNull(root.get(Station_.aclObjectIdentity));

            javax.persistence.criteria.Subquery<Long> sub = query.subquery(Long.class);
            javax.persistence.criteria.Root<AclEntry> ae = sub.from(AclEntry.class);
            javax.persistence.criteria.Join<AclEntry, AclObjectIdentity> aoij =
                    ae.join(AclEntry_.aclObjectIdentity, JoinType.LEFT);

            Predicate sidPredicate = ae.get(AclEntry_.sid).in(aclSids);
            Predicate maskPredicate = ae.get(AclEntry_.mask).in(masks);
            Predicate oidMatch = cb.equal(aoij.get(AclObjectIdentity_.id),
                    root.get(Station_.aclObjectIdentity).get(AclObjectIdentity_.id));

            sub.select(cb.literal(1L)).where(cb.and(oidMatch, sidPredicate, maskPredicate)).distinct(true);

            return cb.and(oidNotNull, cb.exists(sub));
        };
        // les memes filtres que la version non-ACL, appliques par-dessus la restriction
        return createSpecification(criteria, acl);
    }

    protected Specification<Station> createSpecification(StationCriteria criteria,
                                                          Specification<Station> specification) {
        if (criteria == null) {
            return specification;
        }
        if (criteria.getId() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getId(), Station_.id));
        }
        if (criteria.getReference() != null) {
            specification = specification.and(buildStringSpecification(criteria.getReference(), Station_.reference));
        }
        if (criteria.getNatureImplantation() != null) {
            specification = specification.and(buildSpecification(criteria.getNatureImplantation(), Station_.natureImplantation));
        }
        if (criteria.getTypeStation() != null) {
            specification = specification.and(buildSpecification(criteria.getTypeStation(), Station_.typeStation));
        }
        if (criteria.getTypeTrafic() != null) {
            specification = specification.and(buildSpecification(criteria.getTypeTrafic(), Station_.typeTrafic));
        }
        if (criteria.getNatureTrafic() != null) {
            specification = specification.and(buildSpecification(criteria.getNatureTrafic(), Station_.natureTrafic));
        }
        if (criteria.getNatureSupport() != null) {
            specification = specification.and(buildSpecification(criteria.getNatureSupport(), Station_.natureSupport));
        }
        if (criteria.getTypeAntenne() != null) {
            specification = specification.and(buildSpecification(criteria.getTypeAntenne(), Station_.typeAntenne));
        }
        if (criteria.getStatutStation() != null) {
            specification = specification.and(buildSpecification(criteria.getStatutStation(), Station_.statutStation));
        }
        if (criteria.getHauteurTotale() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getHauteurTotale(), Station_.hauteurTotale));
        }
        if (criteria.getInstallateurRaisonSociale() != null) {
            specification = specification.and(buildStringSpecification(criteria.getInstallateurRaisonSociale(), Station_.installateurRaisonSociale));
        }
        if (criteria.getWfProcessID() != null) {
            specification = specification.and(buildStringSpecification(criteria.getWfProcessID(), Station_.wfProcessID));
        }
        if (criteria.getClassId() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getClassId(), Station_.classId));
        }
        if (criteria.getActivityName() != null) {
            specification = specification.and(buildStringSpecification(criteria.getActivityName(), Station_.activityName));
        }
        if (criteria.getAssignee() != null) {
            specification = specification.and(buildStringSpecification(criteria.getAssignee(), Station_.assignee));
        }
        if (criteria.getEndProcess() != null) {
            specification = specification.and(buildSpecification(criteria.getEndProcess(), Station_.endProcess));
        }
        if (criteria.getState() != null) {
            specification = specification.and(buildStringSpecification(criteria.getState(), Station_.state));
        }
        if (criteria.getNumberOfattachments() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getNumberOfattachments(), Station_.numberOfattachments));
        }
        if (criteria.getStep() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getStep(), Station_.step));
        }
        if (criteria.getSysdateCreated() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSysdateCreated(), Station_.sysdateCreated));
        }
        if (criteria.getSysdateUpdated() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSysdateUpdated(), Station_.sysdateUpdated));
        }
        if (criteria.getSyscreatedBy() != null) {
            specification = specification.and(buildStringSpecification(criteria.getSyscreatedBy(), Station_.syscreatedBy));
        }
        if (criteria.getSysupdatedBy() != null) {
            specification = specification.and(buildStringSpecification(criteria.getSysupdatedBy(), Station_.sysupdatedBy));
        }
        if (criteria.getDemandeImplantationId() != null) {
            specification = specification.and((root, query, cb) ->
                    cb.equal(root.join(Station_.demandeImplantation, JoinType.LEFT).get("id"),
                             criteria.getDemandeImplantationId().getEquals()));
        }
        // ---- filtres traversant le site d'implantation ----
        if (criteria.getSiteNom() != null) {
            specification = specification.and(buildSpecification(criteria.getSiteNom(),
                    root -> root.join(Station_.siteImplantation, JoinType.LEFT).get(SiteImplantation_.nomSite)));
        }
        if (criteria.getSiteProvince() != null) {
            specification = specification.and(buildSpecification(criteria.getSiteProvince(),
                    root -> root.join(Station_.siteImplantation, JoinType.LEFT).get(SiteImplantation_.province)));
        }

        // ---- recherche libre ----
        if (criteria.getSearch() != null && criteria.getSearch().getContains() != null) {
            String terme = "%" + criteria.getSearch().getContains().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get(Station_.reference)), terme),
                    cb.like(cb.lower(root.get(Station_.installateurRaisonSociale)), terme),
                    cb.like(cb.lower(root.join(Station_.siteImplantation, JoinType.LEFT).get(SiteImplantation_.nomSite)), terme),
                    cb.like(cb.lower(root.join(Station_.siteImplantation, JoinType.LEFT).get(SiteImplantation_.province)), terme)));
        }

        return specification;
    }

    private Pageable normalizeSort(Pageable page) {
        if (page == null || page.getSort() == null || page.getSort().isUnsorted()) {
            return page;
        }
        // Un tri sur une propriete de collection est refuse : il produit du SQL
        // invalide, et reste mal defini (quelle station ordonne le dossier ?).
        for (Sort.Order o : page.getSort()) {
            String prop = o.getProperty();
            if (prop != null && (prop.startsWith("stations.") || prop.startsWith("frequences.")
                    || prop.startsWith("attestations."))) {
                throw new BadRequestAlertException(StationErrors.SORT_NOT_SUPPORTED,
                        StationErrors.CLASS, StationErrors.SORT_NOT_SUPPORTED);
            }
        }
        Sort mapped = Sort.by(page.getSort().stream()
                .map(o -> new Sort.Order(o.getDirection(), mapSortProperty(o.getProperty()), o.getNullHandling()))
                .toList());
        return PageRequest.of(page.getPageNumber(), page.getPageSize(), mapped);
    }

    private String mapSortProperty(String property) {
        if (property.equals("siteNom")) return "siteImplantation.nomSite";
        if (property.equals("siteProvince")) return "siteImplantation.province";
        return property;
    }

    private Specification<Station> applySortJoins(Specification<Station> specification, Pageable page) {
        if (page == null || page.getSort() == null) {
            return specification;
        }
        for (Sort.Order order : page.getSort()) {
            String property = order.getProperty();
            if (property == null) {
                continue;
            }
            if (property.startsWith("siteImplantation.")) {
                specification = specification.and((root, query, cb) -> { root.join(Station_.siteImplantation, JoinType.LEFT); return null; });
            } else if (property.startsWith("frequences.")) {
                specification = specification.and((root, query, cb) -> { root.join(Station_.frequences, JoinType.LEFT); query.distinct(true); return null; });
            } else if (property.startsWith("demandeImplantation.")) {
                specification = specification.and((root, query, cb) -> { root.join(Station_.demandeImplantation, JoinType.LEFT); return null; });
            }
        }
        return specification;
    }
}
