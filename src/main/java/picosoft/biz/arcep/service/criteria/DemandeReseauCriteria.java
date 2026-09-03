package picosoft.biz.arcep.service.criteria;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.ZonedDateTimeFilter;
import lombok.Getter;
import lombok.Setter;
import picosoft.biz.arcep.domain.reseau.enumeration.*;

import java.io.Serializable;

/**
 * Criteria class for the {@link picosoft.biz.arcep.domain.reseau.DemandeReseau} entity.
 * Bound from the HTTP GET query parameters, converted to a Specification by the query service.
 * Example : {@code /demande-implantations?id.greaterThan=5&reference.contains=something}
 */
@Getter
@Setter
public class DemandeReseauCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;
    private StringFilter reference;
    private BooleanFilter web;
    private ZonedDateTimeFilter createdDate;
    private ZonedDateTimeFilter sendedDate;
    private StringFilter typeDossier;
    private StringFilter statutDossier;
    private StringFilter clientCompany;
    private StringFilter personneNom;
    private StringFilter wfProcessID;
    private LongFilter classId;
    private StringFilter activityName;
    private StringFilter assignee;
    private BooleanFilter endProcess;
    private StringFilter state;
    private LongFilter numberOfattachments;
    private LongFilter step;
    private ZonedDateTimeFilter sysdateCreated;
    private ZonedDateTimeFilter sysdateUpdated;
    private StringFilter syscreatedBy;
    private StringFilter sysupdatedBy;
    private StringFilter search;

    public DemandeReseauCriteria() {
    }

    public DemandeReseauCriteria(DemandeReseauCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.reference = other.reference == null ? null : other.reference.copy();
        this.web = other.web == null ? null : other.web.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.sendedDate = other.sendedDate == null ? null : other.sendedDate.copy();
        this.typeDossier = other.typeDossier == null ? null : other.typeDossier.copy();
        this.statutDossier = other.statutDossier == null ? null : other.statutDossier.copy();
        this.clientCompany = other.clientCompany == null ? null : other.clientCompany.copy();
        this.personneNom = other.personneNom == null ? null : other.personneNom.copy();
        this.wfProcessID = other.wfProcessID == null ? null : other.wfProcessID.copy();
        this.classId = other.classId == null ? null : other.classId.copy();
        this.activityName = other.activityName == null ? null : other.activityName.copy();
        this.assignee = other.assignee == null ? null : other.assignee.copy();
        this.endProcess = other.endProcess == null ? null : other.endProcess.copy();
        this.state = other.state == null ? null : other.state.copy();
        this.numberOfattachments = other.numberOfattachments == null ? null : other.numberOfattachments.copy();
        this.step = other.step == null ? null : other.step.copy();
        this.sysdateCreated = other.sysdateCreated == null ? null : other.sysdateCreated.copy();
        this.sysdateUpdated = other.sysdateUpdated == null ? null : other.sysdateUpdated.copy();
        this.syscreatedBy = other.syscreatedBy == null ? null : other.syscreatedBy.copy();
        this.sysupdatedBy = other.sysupdatedBy == null ? null : other.sysupdatedBy.copy();
        this.search = other.search == null ? null : other.search.copy();
    }

    @Override
    public DemandeReseauCriteria copy() {
        return new DemandeReseauCriteria(this);
    }
}
