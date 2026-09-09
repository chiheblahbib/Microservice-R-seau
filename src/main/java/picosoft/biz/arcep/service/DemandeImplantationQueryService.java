package picosoft.biz.arcep.service;

import io.github.jhipster.service.QueryService;
import picosoft.biz.arcep.controller.errors.BadRequestAlertException;
import picosoft.biz.arcep.controller.errors.ImplantationErrors;
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
import picosoft.biz.arcep.domain.implantation.DemandeImplantation;
import picosoft.biz.arcep.domain.implantation.DemandeImplantation_;
import picosoft.biz.arcep.domain.shared.Applicant_;
import picosoft.biz.arcep.domain.shared.Client_;
import picosoft.biz.arcep.repository.DemandeImplantationRepository;
import picosoft.biz.arcep.service.criteria.DemandeImplantationCriteria;
import picosoft.biz.arcep.service.dto.DemandeImplantationDTO;
import picosoft.biz.arcep.service.mapper.DemandeImplantationMapper;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.List;

/**
 * Service for executing complex queries for {@link DemandeImplantation} entities in the database.
 * The main input is a {@link DemandeImplantationCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 */
@Service
@Transactional(readOnly = true)
public class DemandeImplantationQueryService extends QueryService<DemandeImplantation> {

    private final Logger log = LoggerFactory.getLogger(DemandeImplantationQueryService.class);

    private final DemandeImplantationRepository demandeImplantationRepository;
    private final DemandeImplantationMapper demandeImplantationMapper;
    private final AclSidRepository aclSidRepository;

    public DemandeImplantationQueryService(DemandeImplantationRepository demandeImplantationRepository, DemandeImplantationMapper demandeImplantationMapper,
                                AclSidRepository aclSidRepository) {
        this.demandeImplantationRepository = demandeImplantationRepository;
        this.demandeImplantationMapper = demandeImplantationMapper;
        this.aclSidRepository = aclSidRepository;
    }

    @Transactional(readOnly = true)
    public List<DemandeImplantation> findByCriteria(DemandeImplantationCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        return demandeImplantationRepository.findAll(createSpecification(criteria));
    }

    @Transactional(readOnly = true)
    public Page<DemandeImplantationDTO> findByCriteria(DemandeImplantationCriteria criteria, Pageable page, Integer size) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        page = normalizeSort(page);
        Specification<DemandeImplantation> specification = applySortJoins(createSpecification(criteria), page);
        if (size != null && size == 0) {
            page = PageRequest.of(0, Integer.MAX_VALUE, page.getSort());
        }
        return demandeImplantationRepository.findAll(specification, page).map(demandeImplantationMapper::toDto);
    }

    /**
     * Meme requete, mais restreinte aux objets sur lesquels l'utilisateur courant detient
     * l'un des masques demandes. Le filtrage se fait par sous-requete sur AclEntry.
     */
    @Transactional(readOnly = true)
    public Page<DemandeImplantationDTO> findByCriteriaAcl(DemandeImplantationCriteria criteria, Pageable page, Integer size,
                                               List<String> sidOfCurrentUser, List<Integer> masks) {
        log.debug("find by criteria (acl) : {}, page: {}", criteria, page);
        page = normalizeSort(page);
        Specification<DemandeImplantation> specification =
                applySortJoins(createSpecificationACL(criteria, masks, sidOfCurrentUser), page);
        if (size != null && size == 0) {
            page = PageRequest.of(0, Integer.MAX_VALUE, page.getSort());
        }
        return demandeImplantationRepository.findAll(specification, page).map(demandeImplantationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Long countByCriteriaAcl(DemandeImplantationCriteria criteria, List<String> sidOfCurrentUser, List<Integer> masks) {
        return demandeImplantationRepository.count(createSpecificationACL(criteria, masks, sidOfCurrentUser));
    }

    @Transactional(readOnly = true)
    public long countByCriteria(DemandeImplantationCriteria criteria) {
        return demandeImplantationRepository.count(createSpecification(criteria));
    }

    protected Specification<DemandeImplantation> createSpecification(DemandeImplantationCriteria criteria) {
        return createSpecification(criteria, Specification.where(null));
    }

    protected Specification<DemandeImplantation> createSpecificationACL(DemandeImplantationCriteria criteria,
                                                             List<Integer> masks, List<String> sids) {
        List<AclSid> aclSids = aclSidRepository.findAllBySidIn(sids);

        Specification<DemandeImplantation> acl = (root, query, cb) -> {
            Predicate oidNotNull = cb.isNotNull(root.get(DemandeImplantation_.aclObjectIdentity));

            javax.persistence.criteria.Subquery<Long> sub = query.subquery(Long.class);
            javax.persistence.criteria.Root<AclEntry> ae = sub.from(AclEntry.class);
            javax.persistence.criteria.Join<AclEntry, AclObjectIdentity> aoij =
                    ae.join(AclEntry_.aclObjectIdentity, JoinType.LEFT);

            Predicate sidPredicate = ae.get(AclEntry_.sid).in(aclSids);
            Predicate maskPredicate = ae.get(AclEntry_.mask).in(masks);
            Predicate oidMatch = cb.equal(aoij.get(AclObjectIdentity_.id),
                    root.get(DemandeImplantation_.aclObjectIdentity).get(AclObjectIdentity_.id));

            sub.select(cb.literal(1L)).where(cb.and(oidMatch, sidPredicate, maskPredicate)).distinct(true);

            return cb.and(oidNotNull, cb.exists(sub));
        };
        // les memes filtres que la version non-ACL, appliques par-dessus la restriction
        return createSpecification(criteria, acl);
    }

    protected Specification<DemandeImplantation> createSpecification(DemandeImplantationCriteria criteria,
                                                          Specification<DemandeImplantation> specification) {
        if (criteria == null) {
            return specification;
        }
        if (criteria.getId() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getId(), DemandeImplantation_.id));
        }
        if (criteria.getReference() != null) {
            specification = specification.and(buildStringSpecification(criteria.getReference(), DemandeImplantation_.reference));
        }
        if (criteria.getWeb() != null) {
            specification = specification.and(buildSpecification(criteria.getWeb(), DemandeImplantation_.web));
        }
        if (criteria.getCreatedDate() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), DemandeImplantation_.createdDate));
        }
        if (criteria.getSendedDate() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSendedDate(), DemandeImplantation_.sendedDate));
        }
        if (criteria.getTypeDossier() != null) {
            specification = specification.and(buildStringSpecification(criteria.getTypeDossier(), DemandeImplantation_.typeDossier));
        }
        if (criteria.getStatutDossier() != null) {
            specification = specification.and(buildStringSpecification(criteria.getStatutDossier(), DemandeImplantation_.statutDossier));
        }
        if (criteria.getWfProcessID() != null) {
            specification = specification.and(buildStringSpecification(criteria.getWfProcessID(), DemandeImplantation_.wfProcessID));
        }
        if (criteria.getClassId() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getClassId(), DemandeImplantation_.classId));
        }
        if (criteria.getActivityName() != null) {
            specification = specification.and(buildStringSpecification(criteria.getActivityName(), DemandeImplantation_.activityName));
        }
        if (criteria.getAssignee() != null) {
            specification = specification.and(buildStringSpecification(criteria.getAssignee(), DemandeImplantation_.assignee));
        }
        if (criteria.getEndProcess() != null) {
            specification = specification.and(buildSpecification(criteria.getEndProcess(), DemandeImplantation_.endProcess));
        }
        if (criteria.getState() != null) {
            specification = specification.and(buildStringSpecification(criteria.getState(), DemandeImplantation_.state));
        }
        if (criteria.getNumberOfattachments() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getNumberOfattachments(), DemandeImplantation_.numberOfattachments));
        }
        if (criteria.getStep() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getStep(), DemandeImplantation_.step));
        }
        if (criteria.getSysdateCreated() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSysdateCreated(), DemandeImplantation_.sysdateCreated));
        }
        if (criteria.getSysdateUpdated() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSysdateUpdated(), DemandeImplantation_.sysdateUpdated));
        }
        if (criteria.getSyscreatedBy() != null) {
            specification = specification.and(buildStringSpecification(criteria.getSyscreatedBy(), DemandeImplantation_.syscreatedBy));
        }
        if (criteria.getSysupdatedBy() != null) {
            specification = specification.and(buildStringSpecification(criteria.getSysupdatedBy(), DemandeImplantation_.sysupdatedBy));
        }
        // ---- filtres traversant une relation ----
        if (criteria.getClientCompany() != null) {
            specification = specification.and(buildSpecification(criteria.getClientCompany(),
                    root -> root.join(DemandeImplantation_.client, JoinType.LEFT).get(Client_.clientName)));
        }
        if (criteria.getApplicantName() != null) {
            specification = specification.and(buildSpecification(criteria.getApplicantName(),
                    root -> root.join(DemandeImplantation_.applicant, JoinType.LEFT).get(Applicant_.applicantName)));
        }

        // ---- recherche libre : un seul terme, plusieurs colonnes ----
        if (criteria.getSearch() != null && criteria.getSearch().getContains() != null) {
            String terme = "%" + criteria.getSearch().getContains().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get(DemandeImplantation_.reference)), terme),
                    cb.like(cb.lower(root.get(DemandeImplantation_.typeDossier)), terme),
                    cb.like(cb.lower(root.join(DemandeImplantation_.client, JoinType.LEFT).get(Client_.clientName)), terme),
                    cb.like(cb.lower(root.join(DemandeImplantation_.applicant, JoinType.LEFT).get(Applicant_.applicantName)), terme)));
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
            if (prop != null && (prop.startsWith("frequences.")
                    || prop.startsWith("attestations."))) {
                throw new BadRequestAlertException(ImplantationErrors.SORT_NOT_SUPPORTED,
                        ImplantationErrors.CLASS, ImplantationErrors.SORT_NOT_SUPPORTED);
            }
        }
        Sort mapped = Sort.by(page.getSort().stream()
                .map(o -> new Sort.Order(o.getDirection(), mapSortProperty(o.getProperty()), o.getNullHandling()))
                .toList());
        return PageRequest.of(page.getPageNumber(), page.getPageSize(), mapped);
    }

    private String mapSortProperty(String property) {
        if (property.equals("clientCompany")) return "client.clientName";
        if (property.equals("applicantName")) return "applicant.applicantName";
        return property;
    }

    private Specification<DemandeImplantation> applySortJoins(Specification<DemandeImplantation> specification, Pageable page) {
        if (page == null || page.getSort() == null) {
            return specification;
        }
        for (Sort.Order order : page.getSort()) {
            String property = order.getProperty();
            if (property == null) {
                continue;
            }
            if (property.startsWith("client.")) {
                specification = specification.and((root, query, cb) -> { root.join(DemandeImplantation_.client, JoinType.LEFT); return null; });
            } else if (property.startsWith("applicant.")) {
                specification = specification.and((root, query, cb) -> { root.join(DemandeImplantation_.applicant, JoinType.LEFT); return null; });
            } else if (property.startsWith("station.")) {
                // Jointure a valeur unique : pas de distinct, il n'y a pas de
                // doublon possible a eliminer.
                specification = specification.and((root, query, cb) -> { root.join(DemandeImplantation_.station, JoinType.LEFT); return null; });
            }
        }
        return specification;
    }
}
