package picosoft.biz.arcep.service;

import io.github.jhipster.service.QueryService;
import picosoft.biz.arcep.controller.errors.BadRequestAlertException;
import picosoft.biz.arcep.controller.errors.NumeroCourtUrgenceErrors;
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
import picosoft.biz.arcep.domain.numerocourturgence.DemandeNumeroCourtUrgence;
import picosoft.biz.arcep.domain.numerocourturgence.DemandeNumeroCourtUrgence_;
import picosoft.biz.arcep.domain.numerocourturgence.PersonneNumeroCourtUrgence_;
import picosoft.biz.arcep.domain.shared.Client_;
import picosoft.biz.arcep.repository.DemandeNumeroCourtUrgenceRepository;
import picosoft.biz.arcep.service.criteria.DemandeNumeroCourtUrgenceCriteria;
import picosoft.biz.arcep.service.dto.DemandeNumeroCourtUrgenceDTO;
import picosoft.biz.arcep.service.mapper.DemandeNumeroCourtUrgenceMapper;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.Set;
import java.util.List;

/**
 * Service for executing complex queries for {@link DemandeNumeroCourtUrgence} entities in the database.
 * The main input is a {@link DemandeNumeroCourtUrgenceCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 */
@Service
@Transactional(readOnly = true)
public class DemandeNumeroCourtUrgenceQueryService extends QueryService<DemandeNumeroCourtUrgence> {

    private final Logger log = LoggerFactory.getLogger(DemandeNumeroCourtUrgenceQueryService.class);

    private final DemandeNumeroCourtUrgenceRepository demandeNumeroCourtUrgenceRepository;
    private final DemandeNumeroCourtUrgenceMapper demandeNumeroCourtUrgenceMapper;
    private final AclSidRepository aclSidRepository;

    public DemandeNumeroCourtUrgenceQueryService(DemandeNumeroCourtUrgenceRepository demandeNumeroCourtUrgenceRepository, DemandeNumeroCourtUrgenceMapper demandeNumeroCourtUrgenceMapper,
                                AclSidRepository aclSidRepository) {
        this.demandeNumeroCourtUrgenceRepository = demandeNumeroCourtUrgenceRepository;
        this.demandeNumeroCourtUrgenceMapper = demandeNumeroCourtUrgenceMapper;
        this.aclSidRepository = aclSidRepository;
    }

    @Transactional(readOnly = true)
    public List<DemandeNumeroCourtUrgence> findByCriteria(DemandeNumeroCourtUrgenceCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        return demandeNumeroCourtUrgenceRepository.findAll(createSpecification(criteria));
    }

    @Transactional(readOnly = true)
    public Page<DemandeNumeroCourtUrgenceDTO> findByCriteria(DemandeNumeroCourtUrgenceCriteria criteria, Pageable page, Integer size) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        page = normalizeSort(page);
        Specification<DemandeNumeroCourtUrgence> specification = applySortJoins(createSpecification(criteria), page);
        if (size != null && size == 0) {
            page = PageRequest.of(0, Integer.MAX_VALUE, page.getSort());
        }
        return demandeNumeroCourtUrgenceRepository.findAll(specification, page).map(demandeNumeroCourtUrgenceMapper::toDto);
    }

    /**
     * Meme requete, mais restreinte aux objets sur lesquels l'utilisateur courant detient
     * l'un des masques demandes. Le filtrage se fait par sous-requete sur AclEntry.
     */
    @Transactional(readOnly = true)
    public Page<DemandeNumeroCourtUrgenceDTO> findByCriteriaAcl(DemandeNumeroCourtUrgenceCriteria criteria, Pageable page, Integer size,
                                               List<String> sidOfCurrentUser, List<Integer> masks) {
        log.debug("find by criteria (acl) : {}, page: {}", criteria, page);
        page = normalizeSort(page);
        Specification<DemandeNumeroCourtUrgence> specification =
                applySortJoins(createSpecificationACL(criteria, masks, sidOfCurrentUser), page);
        if (size != null && size == 0) {
            page = PageRequest.of(0, Integer.MAX_VALUE, page.getSort());
        }
        return demandeNumeroCourtUrgenceRepository.findAll(specification, page).map(demandeNumeroCourtUrgenceMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Long countByCriteriaAcl(DemandeNumeroCourtUrgenceCriteria criteria, List<String> sidOfCurrentUser, List<Integer> masks) {
        return demandeNumeroCourtUrgenceRepository.count(createSpecificationACL(criteria, masks, sidOfCurrentUser));
    }

    @Transactional(readOnly = true)
    public long countByCriteria(DemandeNumeroCourtUrgenceCriteria criteria) {
        return demandeNumeroCourtUrgenceRepository.count(createSpecification(criteria));
    }

    protected Specification<DemandeNumeroCourtUrgence> createSpecification(DemandeNumeroCourtUrgenceCriteria criteria) {
        return createSpecification(criteria, Specification.where(null));
    }

    protected Specification<DemandeNumeroCourtUrgence> createSpecificationACL(DemandeNumeroCourtUrgenceCriteria criteria,
                                                             List<Integer> masks, List<String> sids) {
        List<AclSid> aclSids = aclSidRepository.findAllBySidIn(sids);

        Specification<DemandeNumeroCourtUrgence> acl = (root, query, cb) -> {
            Predicate oidNotNull = cb.isNotNull(root.get(DemandeNumeroCourtUrgence_.aclObjectIdentity));

            javax.persistence.criteria.Subquery<Long> sub = query.subquery(Long.class);
            javax.persistence.criteria.Root<AclEntry> ae = sub.from(AclEntry.class);
            javax.persistence.criteria.Join<AclEntry, AclObjectIdentity> aoij =
                    ae.join(AclEntry_.aclObjectIdentity, JoinType.LEFT);

            Predicate sidPredicate = ae.get(AclEntry_.sid).in(aclSids);
            Predicate maskPredicate = ae.get(AclEntry_.mask).in(masks);
            Predicate oidMatch = cb.equal(aoij.get(AclObjectIdentity_.id),
                    root.get(DemandeNumeroCourtUrgence_.aclObjectIdentity).get(AclObjectIdentity_.id));

            sub.select(cb.literal(1L)).where(cb.and(oidMatch, sidPredicate, maskPredicate)).distinct(true);

            return cb.and(oidNotNull, cb.exists(sub));
        };
        // les memes filtres que la version non-ACL, appliques par-dessus la restriction
        return createSpecification(criteria, acl);
    }

    protected Specification<DemandeNumeroCourtUrgence> createSpecification(DemandeNumeroCourtUrgenceCriteria criteria,
                                                          Specification<DemandeNumeroCourtUrgence> specification) {
        if (criteria == null) {
            return specification;
        }
        if (criteria.getId() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getId(), DemandeNumeroCourtUrgence_.id));
        }
        if (criteria.getReference() != null) {
            specification = specification.and(buildStringSpecification(criteria.getReference(), DemandeNumeroCourtUrgence_.reference));
        }
        if (criteria.getWeb() != null) {
            specification = specification.and(buildSpecification(criteria.getWeb(), DemandeNumeroCourtUrgence_.web));
        }
        if (criteria.getCreatedDate() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), DemandeNumeroCourtUrgence_.createdDate));
        }
        if (criteria.getSendedDate() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSendedDate(), DemandeNumeroCourtUrgence_.sendedDate));
        }
        if (criteria.getTypeDossier() != null) {
            specification = specification.and(buildStringSpecification(criteria.getTypeDossier(), DemandeNumeroCourtUrgence_.typeDossier));
        }
        if (criteria.getStatutDossier() != null) {
            specification = specification.and(buildStringSpecification(criteria.getStatutDossier(), DemandeNumeroCourtUrgence_.statutDossier));
        }
        if (criteria.getWfProcessID() != null) {
            specification = specification.and(buildStringSpecification(criteria.getWfProcessID(), DemandeNumeroCourtUrgence_.wfProcessID));
        }
        if (criteria.getClassId() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getClassId(), DemandeNumeroCourtUrgence_.classId));
        }
        if (criteria.getActivityName() != null) {
            specification = specification.and(buildStringSpecification(criteria.getActivityName(), DemandeNumeroCourtUrgence_.activityName));
        }
        if (criteria.getAssignee() != null) {
            specification = specification.and(buildStringSpecification(criteria.getAssignee(), DemandeNumeroCourtUrgence_.assignee));
        }
        if (criteria.getEndProcess() != null) {
            specification = specification.and(buildSpecification(criteria.getEndProcess(), DemandeNumeroCourtUrgence_.endProcess));
        }
        if (criteria.getState() != null) {
            specification = specification.and(buildStringSpecification(criteria.getState(), DemandeNumeroCourtUrgence_.state));
        }
        if (criteria.getNumberOfattachments() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getNumberOfattachments(), DemandeNumeroCourtUrgence_.numberOfattachments));
        }
        if (criteria.getStep() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getStep(), DemandeNumeroCourtUrgence_.step));
        }
        if (criteria.getSysdateCreated() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSysdateCreated(), DemandeNumeroCourtUrgence_.sysdateCreated));
        }
        if (criteria.getSysdateUpdated() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getSysdateUpdated(), DemandeNumeroCourtUrgence_.sysdateUpdated));
        }
        if (criteria.getSyscreatedBy() != null) {
            specification = specification.and(buildStringSpecification(criteria.getSyscreatedBy(), DemandeNumeroCourtUrgence_.syscreatedBy));
        }
        if (criteria.getSysupdatedBy() != null) {
            specification = specification.and(buildStringSpecification(criteria.getSysupdatedBy(), DemandeNumeroCourtUrgence_.sysupdatedBy));
        }
        // ---- filtres traversant une relation ----
        if (criteria.getClientCompany() != null) {
            specification = specification.and(buildSpecification(criteria.getClientCompany(),
                    root -> root.join(DemandeNumeroCourtUrgence_.client, JoinType.LEFT).get(Client_.clientName)));
        }
        // Le reseau n'a pas d'« applicant » : il a des PERSONNES, requerant et
        // responsable, distinguees par leur role. On cherche sur leur nom.
        if (criteria.getPersonneNom() != null) {
            specification = specification.and(buildSpecification(criteria.getPersonneNom(),
                    root -> root.join(DemandeNumeroCourtUrgence_.representant, JoinType.LEFT).get(PersonneNumeroCourtUrgence_.nom)));
        }

        // ---- recherche libre : un seul terme, plusieurs colonnes ----
        if (criteria.getSearch() != null && criteria.getSearch().getContains() != null) {
            String terme = "%" + criteria.getSearch().getContains().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get(DemandeNumeroCourtUrgence_.reference)), terme),
                    cb.like(cb.lower(root.get(DemandeNumeroCourtUrgence_.typeDossier)), terme),
                    cb.like(cb.lower(root.join(DemandeNumeroCourtUrgence_.client, JoinType.LEFT).get(Client_.clientName)), terme),
                    cb.like(cb.lower(root.join(DemandeNumeroCourtUrgence_.representant, JoinType.LEFT).get(PersonneNumeroCourtUrgence_.nom)), terme)));
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
                throw new BadRequestAlertException(NumeroCourtUrgenceErrors.SORT_NOT_SUPPORTED,
                        NumeroCourtUrgenceErrors.CLASS, NumeroCourtUrgenceErrors.SORT_NOT_SUPPORTED);
            }
            // Tout nom inconnu est refuse ICI. Le laisser passer le ferait
            // resoudre par Spring Data contre l'entite, qui echouerait en 500
            // sur une simple faute de frappe dans un parametre de requete.
            if (prop != null && !TRIS_ACCEPTES.contains(prop)) {
                throw new BadRequestAlertException(
                        "Tri non supporte sur « " + prop + " ». Proprietes acceptees : "
                                + String.join(", ", new java.util.TreeSet<>(TRIS_ACCEPTES)),
                        NumeroCourtUrgenceErrors.CLASS, NumeroCourtUrgenceErrors.SORT_NOT_SUPPORTED);
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
     * DemandeNumeroCourtUrgence n'a pas de propriete `applicant` -- le tri echouait a la
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

    private Specification<DemandeNumeroCourtUrgence> applySortJoins(Specification<DemandeNumeroCourtUrgence> specification, Pageable page) {
        if (page == null || page.getSort() == null) {
            return specification;
        }
        for (Sort.Order order : page.getSort()) {
            String property = order.getProperty();
            if (property == null) {
                continue;
            }
            if (property.startsWith("client.")) {
                specification = specification.and((root, query, cb) -> { root.join(DemandeNumeroCourtUrgence_.client, JoinType.LEFT); return null; });
            } else if (property.startsWith("personnes.")) {
                specification = specification.and((root, query, cb) -> { root.join(DemandeNumeroCourtUrgence_.representant, JoinType.LEFT); query.distinct(true); return null; });
            } else if (property.startsWith("sites.")) {
            }
        }
        return specification;
    }
}
