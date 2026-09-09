package picosoft.biz.arcep.service;

import io.github.jhipster.service.QueryService;
import picosoft.biz.arcep.controller.errors.BadRequestAlertException;
import picosoft.biz.arcep.controller.errors.PqErrors;
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
import picosoft.biz.arcep.domain.pq.DemandePq;
import picosoft.biz.arcep.domain.pq.DemandePq_;
import picosoft.biz.arcep.domain.pq.PersonnePq_;
import picosoft.biz.arcep.domain.shared.Client_;
import picosoft.biz.arcep.repository.DemandePqRepository;
import picosoft.biz.arcep.service.criteria.DemandePqCriteria;
import picosoft.biz.arcep.service.dto.DemandePqDTO;
import picosoft.biz.arcep.service.mapper.DemandePqMapper;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.Set;
import java.util.List;

/**
 * Service for executing complex queries for {@link DemandePq} entities in the database.
 * The main input is a {@link DemandePqCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 */
@Service
@Transactional(readOnly = true)
public class DemandePqQueryService extends QueryService<DemandePq> {

    private final Logger log = LoggerFactory.getLogger(DemandePqQueryService.class);

    private final DemandePqRepository demandePqRepository;
    private final DemandePqMapper demandePqMapper;
    private final AclSidRepository aclSidRepository;

    public DemandePqQueryService(DemandePqRepository demandePqRepository, DemandePqMapper demandePqMapper,
                                AclSidRepository aclSidRepository) {
        this.demandePqRepository = demandePqRepository;
        this.demandePqMapper = demandePqMapper;
        this.aclSidRepository = aclSidRepository;
    }

    @Transactional(readOnly = true)
    public List<DemandePq> findByCriteria(DemandePqCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        return demandePqRepository.findAll(createSpecification(criteria));
    }

    @Transactional(readOnly = true)
    public Page<DemandePqDTO> findByCriteria(DemandePqCriteria criteria, Pageable page, Integer size) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        page = normalizeSort(page);
        Specification<DemandePq> specification = applySortJoins(createSpecification(criteria), page);
        if (size != null && size == 0) {
            page = PageRequest.of(0, Integer.MAX_VALUE, page.getSort());
        }
        return demandePqRepository.findAll(specification, page).map(demandePqMapper::toDto);
    }

    /**
     * Meme requete, mais restreinte aux objets sur lesquels l'utilisateur courant detient
     * l'un des masques demandes. Le filtrage se fait par sous-requete sur AclEntry.
     */
    @Transactional(readOnly = true)
    public Page<DemandePqDTO> findByCriteriaAcl(DemandePqCriteria criteria, Pageable page, Integer size,
                                               List<String> sidOfCurrentUser, List<Integer> masks) {
        log.debug("find by criteria (acl) : {}, page: {}", criteria, page);
        page = normalizeSort(page);
        Specification<DemandePq> specification =
                applySortJoins(createSpecificationACL(criteria, masks, sidOfCurrentUser), page);
        if (size != null && size == 0) {
            page = PageRequest.of(0, Integer.MAX_VALUE, page.getSort());
        }
        return demandePqRepository.findAll(specification, page).map(demandePqMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Long countByCriteriaAcl(DemandePqCriteria criteria, List<String> sidOfCurrentUser, List<Integer> masks) {
        return demandePqRepository.count(createSpecificationACL(criteria, masks, sidOfCurrentUser));
    }

    @Transactional(readOnly = true)
    public long countByCriteria(DemandePqCriteria criteria) {
        return demandePqRepository.count(createSpecification(criteria));
    }

    protected Specification<DemandePq> createSpecification(DemandePqCriteria criteria) {
        return createSpecification(criteria, Specification.where(null));
    }

    protected Specification<DemandePq> createSpecificationACL(DemandePqCriteria criteria,
                                                             List<Integer> masks, List<String> sids) {
        List<AclSid> aclSids = aclSidRepository.findAllBySidIn(sids);

        Specification<DemandePq> acl = (root, query, cb) -> {
            Predicate oidNotNull = cb.isNotNull(root.get(DemandePq_.aclObjectIdentity));

            javax.persistence.criteria.Subquery<Long> sub = query.subquery(Long.class);
            javax.persistence.criteria.Root<AclEntry> ae = sub.from(AclEntry.class);
            javax.persistence.criteria.Join<AclEntry, AclObjectIdentity> aoij =
                    ae.join(AclEntry_.aclObjectIdentity, JoinType.LEFT);

            Predicate sidPredicate = ae.get(AclEntry_.sid).in(aclSids);
            Predicate maskPredicate = ae.get(AclEntry_.mask).in(masks);
            Predicate oidMatch = cb.equal(aoij.get(AclObjectIdentity_.id),
                    root.get(DemandePq_.aclObjectIdentity).get(AclObjectIdentity_.id));

            sub.select(cb.literal(1L)).where(cb.and(oidMatch, sidPredicate, maskPredicate)).distinct(true);

            return cb.and(oidNotNull, cb.exists(sub));
        };
        // les memes filtres que la version non-ACL, appliques par-dessus la restriction
        return createSpecification(criteria, acl);
    }

    protected Specification<DemandePq> createSpecification(DemandePqCriteria criteria,
                                                          Specification<DemandePq> specification) {
        if (criteria == null) {
            return specification;
        }
        if (criteria.getId() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getId(), DemandePq_.id));
        }
        if (criteria.getReference() != null) {
            specification = specification.and(buildStringSpecification(criteria.getReference(), DemandePq_.reference));
        }
        if (criteria.getWeb() != null) {
            specification = specification.and(buildSpecification(criteria.getWeb(), DemandePq_.web));
        }
        if (criteria.getCreatedDate() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), DemandePq_.createdDate));
        }
        if (criteria.getSendedDate() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSendedDate(), DemandePq_.sendedDate));
        }
        if (criteria.getTypeDossier() != null) {
            specification = specification.and(buildStringSpecification(criteria.getTypeDossier(), DemandePq_.typeDossier));
        }
        if (criteria.getStatutDossier() != null) {
            specification = specification.and(buildStringSpecification(criteria.getStatutDossier(), DemandePq_.statutDossier));
        }
        if (criteria.getWfProcessID() != null) {
            specification = specification.and(buildStringSpecification(criteria.getWfProcessID(), DemandePq_.wfProcessID));
        }
        if (criteria.getClassId() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getClassId(), DemandePq_.classId));
        }
        if (criteria.getActivityName() != null) {
            specification = specification.and(buildStringSpecification(criteria.getActivityName(), DemandePq_.activityName));
        }
        if (criteria.getAssignee() != null) {
            specification = specification.and(buildStringSpecification(criteria.getAssignee(), DemandePq_.assignee));
        }
        if (criteria.getEndProcess() != null) {
            specification = specification.and(buildSpecification(criteria.getEndProcess(), DemandePq_.endProcess));
        }
        if (criteria.getState() != null) {
            specification = specification.and(buildStringSpecification(criteria.getState(), DemandePq_.state));
        }
        if (criteria.getNumberOfattachments() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getNumberOfattachments(), DemandePq_.numberOfattachments));
        }
        if (criteria.getStep() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getStep(), DemandePq_.step));
        }
        if (criteria.getSysdateCreated() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSysdateCreated(), DemandePq_.sysdateCreated));
        }
        if (criteria.getSysdateUpdated() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSysdateUpdated(), DemandePq_.sysdateUpdated));
        }
        if (criteria.getSyscreatedBy() != null) {
            specification = specification.and(buildStringSpecification(criteria.getSyscreatedBy(), DemandePq_.syscreatedBy));
        }
        if (criteria.getSysupdatedBy() != null) {
            specification = specification.and(buildStringSpecification(criteria.getSysupdatedBy(), DemandePq_.sysupdatedBy));
        }
        // ---- filtres traversant une relation ----
        if (criteria.getClientCompany() != null) {
            specification = specification.and(buildSpecification(criteria.getClientCompany(),
                    root -> root.join(DemandePq_.client, JoinType.LEFT).get(Client_.clientName)));
        }
        // Le reseau n'a pas d'« applicant » : il a des PERSONNES, requerant et
        // responsable, distinguees par leur role. On cherche sur leur nom.
        if (criteria.getPersonneNom() != null) {
            specification = specification.and(buildSpecification(criteria.getPersonneNom(),
                    root -> root.join(DemandePq_.representant, JoinType.LEFT).get(PersonnePq_.nom)));
        }

        // ---- recherche libre : un seul terme, plusieurs colonnes ----
        if (criteria.getSearch() != null && criteria.getSearch().getContains() != null) {
            String terme = "%" + criteria.getSearch().getContains().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get(DemandePq_.reference)), terme),
                    cb.like(cb.lower(root.get(DemandePq_.typeDossier)), terme),
                    cb.like(cb.lower(root.join(DemandePq_.client, JoinType.LEFT).get(Client_.clientName)), terme),
                    cb.like(cb.lower(root.join(DemandePq_.representant, JoinType.LEFT).get(PersonnePq_.nom)), terme)));
        }

        return specification;
    }

    private Pageable normalizeSort(Pageable page) {
        if (page == null || page.getSort() == null || page.getSort().isUnsorted()) {
            return page;
        }
        // Un tri sur une propriete de collection est refuse : il produit du SQL
        // invalide, et reste mal defini (lequel des sites ordonne le dossier ?).
        //
        // La liste doit nommer les collections REELLES du service : une garde
        // qui designe des collections disparues ne garde plus rien, et le tri
        // passe jusqu'au SQL.
        for (Sort.Order o : page.getSort()) {
            String prop = o.getProperty();
            if (prop != null && (prop.startsWith("equipements.")
                    || prop.startsWith("attestations."))) {
                throw new BadRequestAlertException(PqErrors.SORT_NOT_SUPPORTED,
                        PqErrors.CLASS, PqErrors.SORT_NOT_SUPPORTED);
            }
            // Tout nom inconnu est refuse ICI. Le laisser passer le ferait
            // resoudre par Spring Data contre l'entite, qui echouerait en 500
            // sur une simple faute de frappe dans un parametre de requete.
            if (prop != null && !TRIS_ACCEPTES.contains(prop)) {
                throw new BadRequestAlertException(
                        "Tri non supporte sur « " + prop + " ». Proprietes acceptees : "
                                + String.join(", ", new java.util.TreeSet<>(TRIS_ACCEPTES)),
                        PqErrors.CLASS, PqErrors.SORT_NOT_SUPPORTED);
            }
        }
        Sort mapped = Sort.by(page.getSort().stream()
                .map(o -> new Sort.Order(o.getDirection(), mapSortProperty(o.getProperty()), o.getNullHandling()))
                .toList());
        return PageRequest.of(page.getPageNumber(), page.getPageSize(), mapped);
    }

    /**
     * Les alias de tri exposes au front.
     *
     * La version heritee renvoyait `applicant.applicantName`, alors que
     * DemandePq n'a pas de propriete `applicant` -- le tri echouait a la
     * resolution du chemin. Et `clientCompany` pointait `client.clientName`,
     * donc triait sur le nom d'une personne physique alors que la raison sociale
     * est dans `client.company` : un tri qui rendait un ordre faux, en silence.
     *
     * Le nom du requerant vit desormais dans la collection `personnes`, sur
     * laquelle un tri n'a pas de sens : il est refuse par normalizeSort plus
     * haut, et n'a donc pas d'alias ici.
     */
    private String mapSortProperty(String property) {
        if (property.equals("clientCompany")) return "client.company";
        if (property.equals("titulaire"))     return "client.company";
        return property;
    }

    /**
     * Les proprietes sur lesquelles un tri est accepte.
     *
     * Liste BLANCHE, et non liste noire : Spring Data resout le nom d'une
     * propriete de tri contre le type du domaine, et leve une
     * PropertyReferenceException sur un nom inconnu -- que Spring rend en
     * 500. Verifie sur le service vivant : ?sort=applicantName repondait 500,
     * ce nom etant un vestige du dossier d'implantation.
     *
     * Un parametre de requete errone est une faute de l'appelant, pas une panne
     * du serveur : il doit donner un 400 qui nomme ce qui est accepte.
     */
    private static final Set<String> TRIS_ACCEPTES = Set.of(
            "id", "reference", "statutDossier", "typeDossier",
            "natureReseau", "natureDemande", "referenceAutorisationAnterieure",
            "fraisDossier", "deviseFrais",
            "createdDate", "sendedDate", "sysdateCreated", "sysdateUpdated",
            "engagementNom", "engagementQualite", "engagementLieu", "engagementDate",
            "activityName", "assignee", "state", "endProcess", "step",
            // les alias traduits par mapSortProperty
            "clientCompany", "titulaire");

    private Specification<DemandePq> applySortJoins(Specification<DemandePq> specification, Pageable page) {
        if (page == null || page.getSort() == null) {
            return specification;
        }
        for (Sort.Order order : page.getSort()) {
            String property = order.getProperty();
            if (property == null) {
                continue;
            }
            if (property.startsWith("client.")) {
                specification = specification.and((root, query, cb) -> { root.join(DemandePq_.client, JoinType.LEFT); return null; });
            } else if (property.startsWith("personnes.")) {
                specification = specification.and((root, query, cb) -> { root.join(DemandePq_.representant, JoinType.LEFT); query.distinct(true); return null; });
            } else if (property.startsWith("sites.")) {
            }
        }
        return specification;
    }
}
