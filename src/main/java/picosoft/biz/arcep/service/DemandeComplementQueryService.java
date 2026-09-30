package picosoft.biz.arcep.service;

import io.github.jhipster.service.QueryService;
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
import picosoft.biz.arcep.domain.shared.DemandeComplement;
import picosoft.biz.arcep.domain.shared.DemandeComplement_;
import picosoft.biz.arcep.repository.DemandeComplementRepository;
import picosoft.biz.arcep.service.criteria.DemandeComplementCriteria;
import picosoft.biz.arcep.service.dto.DemandeComplementDTO;
import picosoft.biz.arcep.service.mapper.DemandeComplementMapper;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DemandeComplementQueryService extends QueryService<DemandeComplement> {

    private final Logger log = LoggerFactory.getLogger(DemandeComplementQueryService.class);
    private final DemandeComplementRepository demandeComplementRepository;
    private final DemandeComplementMapper demandeComplementMapper;
    private final AclSidRepository aclSidRepository;

    public DemandeComplementQueryService(DemandeComplementRepository demandeComplementRepository,
                                         DemandeComplementMapper demandeComplementMapper,
                                         AclSidRepository aclSidRepository) {
        this.demandeComplementRepository = demandeComplementRepository;
        this.demandeComplementMapper = demandeComplementMapper;
        this.aclSidRepository = aclSidRepository;
    }

    @Transactional(readOnly = true)
    public List<DemandeComplement> findByCriteria(DemandeComplementCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<DemandeComplement> specification = createSpecification(criteria);
        return demandeComplementRepository.findAll(specification);
    }

    @Transactional(readOnly = true)
    public Page<DemandeComplementDTO> findByCriteria(DemandeComplementCriteria criteria, Pageable page, Integer size) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        page = normalizeSort(page);
        Specification<DemandeComplement> specification = createSpecification(criteria);
        if (size != null && size == 0) {
            page = PageRequest.of(0, Integer.MAX_VALUE, page.getSort());
        }
        return demandeComplementRepository.findAll(specification, page).map(demandeComplementMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<DemandeComplementDTO> findByCriteriaAcl(DemandeComplementCriteria criteria, Pageable page, Integer size, List<String> sidOfCurrentUser, List<Integer> masks) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        page = normalizeSort(page);
        Specification<DemandeComplement> specification = createSpecificationACL(criteria, masks, sidOfCurrentUser);
        if (size != null && size == 0) {
            page = PageRequest.of(0, Integer.MAX_VALUE, page.getSort());
        }
        return demandeComplementRepository.findAll(specification, page).map(demandeComplementMapper::toDto);
    }

    @Transactional(readOnly = true)
    public long countByCriteria(DemandeComplementCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<DemandeComplement> specification = createSpecification(criteria);
        return demandeComplementRepository.count(specification);
    }

    protected Specification<DemandeComplement> createSpecification(DemandeComplementCriteria criteria) {
        // La table demande_complement est partagée avec homologation (ASI, HOMOLOGATION) :
        // drrrs-back ne sert que les demandes de ses propres dossiers.
        Specification<DemandeComplement> specification = Specification.where(
                (root, query, cb) -> root.get(DemandeComplement_.typeDossier).in(DossierDrrrsResolver.typesDrrrs()));
        if (criteria != null) {
            if (criteria.getId() != null) specification = specification.and(buildRangeSpecification(criteria.getId(), DemandeComplement_.id));
            if (criteria.getReference() != null) specification = specification.and(buildStringSpecification(criteria.getReference(), DemandeComplement_.reference));
            if (criteria.getReferenceDossier() != null) specification = specification.and(buildStringSpecification(criteria.getReferenceDossier(), DemandeComplement_.referenceDossier));
            if (criteria.getSubject() != null) specification = specification.and(buildStringSpecification(criteria.getSubject(), DemandeComplement_.subject));
            if (criteria.getDescription() != null) specification = specification.and(buildStringSpecification(criteria.getDescription(), DemandeComplement_.description));
            if (criteria.getCreatedDate() != null) specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), DemandeComplement_.createdDate));
            if (criteria.getSendedDate() != null) specification = specification.and(buildRangeSpecification(criteria.getSendedDate(), DemandeComplement_.sendedDate));
            if (criteria.getSysdateCreated() != null) specification = specification.and(buildRangeSpecification(criteria.getSysdateCreated(), DemandeComplement_.sysdateCreated));
            if (criteria.getSysdateUpdated() != null) specification = specification.and(buildRangeSpecification(criteria.getSysdateUpdated(), DemandeComplement_.sysdateUpdated));
            if (criteria.getSyscreatedBy() != null) specification = specification.and(buildStringSpecification(criteria.getSyscreatedBy(), DemandeComplement_.syscreatedBy));
            if (criteria.getSysupdatedBy() != null) specification = specification.and(buildStringSpecification(criteria.getSysupdatedBy(), DemandeComplement_.sysupdatedBy));
            if (criteria.getIdsPostAttachments() != null) specification = specification.and(buildStringSpecification(criteria.getIdsPostAttachments(), DemandeComplement_.idsPostAttachments));
            if (criteria.getLabelsPostAttachments() != null) specification = specification.and(buildStringSpecification(criteria.getLabelsPostAttachments(), DemandeComplement_.labelsPostAttachments));
            if (criteria.getLabelsMissingPostAttachments() != null) specification = specification.and(buildStringSpecification(criteria.getLabelsMissingPostAttachments(), DemandeComplement_.labelsMissingPostAttachments));
            if (criteria.getWfProcessID() != null) specification = specification.and(buildStringSpecification(criteria.getWfProcessID(), DemandeComplement_.wfProcessID));
            if (criteria.getClassId() != null) specification = specification.and(buildRangeSpecification(criteria.getClassId(), DemandeComplement_.classId));
            if (criteria.getClassIdDossier() != null) specification = specification.and(buildRangeSpecification(criteria.getClassIdDossier(), DemandeComplement_.classIdDossier));
            if (criteria.getObjectIdDossier() != null) specification = specification.and(buildRangeSpecification(criteria.getObjectIdDossier(), DemandeComplement_.objectIdDossier));
            if (criteria.getActivityName() != null) specification = specification.and(buildStringSpecification(criteria.getActivityName(), DemandeComplement_.activityName));
            if (criteria.getAssignee() != null) specification = specification.and(buildStringSpecification(criteria.getAssignee(), DemandeComplement_.assignee));
            if (criteria.getEndProcess() != null) specification = specification.and(buildSpecification(criteria.getEndProcess(), DemandeComplement_.endProcess));
            if (criteria.getState() != null) specification = specification.and(buildStringSpecification(criteria.getState(), DemandeComplement_.state));
            if (criteria.getStateDemande() != null) specification = specification.and(buildStringSpecification(criteria.getStateDemande(), DemandeComplement_.stateDemande));
            if (criteria.getNumberOfattachments() != null) specification = specification.and(buildRangeSpecification(criteria.getNumberOfattachments(), DemandeComplement_.numberOfattachments));
            if (criteria.getStep() != null) specification = specification.and(buildRangeSpecification(criteria.getStep(), DemandeComplement_.step));
            if (criteria.getApprovedBy() != null) specification = specification.and(buildStringSpecification(criteria.getApprovedBy(), DemandeComplement_.approvedBy));
            if (criteria.getCategorie() != null) specification = specification.and(buildStringSpecification(criteria.getCategorie(), DemandeComplement_.categorie));
            if (criteria.getAffectedSid() != null) specification = specification.and(buildStringSpecification(criteria.getAffectedSid(), DemandeComplement_.affectedSid));
            if (criteria.getAffectedName() != null) specification = specification.and(buildStringSpecification(criteria.getAffectedName(), DemandeComplement_.affectedName));
            if (criteria.getAffectedKeycloakId() != null) specification = specification.and(buildStringSpecification(criteria.getAffectedKeycloakId(), DemandeComplement_.affectedKeycloakId));
            if (criteria.getSidExterne() != null) specification = specification.and(buildStringSpecification(criteria.getSidExterne(), DemandeComplement_.sidExterne));
            if (criteria.getDelaiReponse() != null) specification = specification.and(buildSpecification(criteria.getDelaiReponse(), DemandeComplement_.delaiReponse));
            if (criteria.getWeb() != null) specification = specification.and(buildSpecification(criteria.getWeb(), DemandeComplement_.web));
            if (criteria.getValidation() != null) specification = specification.and(buildSpecification(criteria.getValidation(), DemandeComplement_.validation));
            if (criteria.getCommentaire() != null) specification = specification.and(buildStringSpecification(criteria.getCommentaire(), DemandeComplement_.commentaire));
            if (criteria.getAsiId() != null) specification = specification.and(buildSpecification(criteria.getAsiId(), DemandeComplement_.asiId));

            if (criteria.getSearch() != null) {
                Specification<DemandeComplement> orSpec = Specification.where(null);
                orSpec = orSpec.or(buildStringSpecification(criteria.getSearch(), DemandeComplement_.reference));
                orSpec = orSpec.or(buildStringSpecification(criteria.getSearch(), DemandeComplement_.subject));
                orSpec = orSpec.or(buildStringSpecification(criteria.getSearch(), DemandeComplement_.description));
                orSpec = orSpec.or(buildStringSpecification(criteria.getSearch(), DemandeComplement_.activityName));
                orSpec = orSpec.or(buildStringSpecification(criteria.getSearch(), DemandeComplement_.state));
                orSpec = orSpec.or(buildStringSpecification(criteria.getSearch(), DemandeComplement_.assignee));
                specification = specification.and((root, query, cb) -> {
                    query.distinct(true);
                    return null;
                }).and(orSpec);
            }
        }
        return specification;
    }

    private Pageable normalizeSort(Pageable page) {
        if (page == null || page.getSort() == null || page.getSort().isUnsorted()) {
            return page;
        }
        Sort original = page.getSort();
        Sort mapped = Sort.by(original.stream().map(order -> new Sort.Order(order.getDirection(), order.getProperty(), order.getNullHandling())).toList());
        return PageRequest.of(page.getPageNumber(), page.getPageSize(), mapped);
    }

    protected Specification<DemandeComplement> createSpecificationACL(DemandeComplementCriteria criteria, List<Integer> masks, List<String> sids) {
        List<AclSid> aclSids = aclSidRepository.findAllBySidIn(sids);

        Specification<DemandeComplement> specification = (root, query, cb) -> {
            Predicate aclObjectIdentityNotNull = cb.isNotNull(root.get(DemandeComplement_.aclObjectIdentity));

            javax.persistence.criteria.Subquery<Long> sub = query.subquery(Long.class);
            javax.persistence.criteria.Root<AclEntry> ae = sub.from(AclEntry.class);
            javax.persistence.criteria.Join<AclEntry, AclObjectIdentity> aoij = ae.join("aclObjectIdentity", JoinType.LEFT);

            Predicate sidPredicate = ae.get("sid").in(aclSids);
            Predicate maskPredicate = ae.get("mask").in(masks);
            Predicate oidMatch = cb.equal(aoij.get("id"), root.get(DemandeComplement_.aclObjectIdentity).get("id"));

            sub.select(cb.literal(1L)).where(cb.and(oidMatch, sidPredicate, maskPredicate)).distinct(true);

            return cb.and(aclObjectIdentityNotNull, cb.exists(sub));
        };

        return createSpecification(criteria).and(specification);
    }
}
