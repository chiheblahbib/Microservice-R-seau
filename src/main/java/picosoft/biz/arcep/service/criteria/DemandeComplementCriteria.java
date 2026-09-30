package picosoft.biz.arcep.service.criteria;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.ZonedDateTimeFilter;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class DemandeComplementCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;
    private StringFilter reference;
    private StringFilter referenceDossier;
    private StringFilter subject;
    private StringFilter description;
    private ZonedDateTimeFilter createdDate;
    private ZonedDateTimeFilter sendedDate;
    private StringFilter approvedBy;
    private StringFilter categorie;
    private StringFilter idsPostAttachments;
    private StringFilter labelsPostAttachments;
    private StringFilter labelsMissingPostAttachments;
    private StringFilter wfProcessID;
    private LongFilter classId;
    private LongFilter classIdDossier;
    private LongFilter objectIdDossier;
    private StringFilter activityName;
    private StringFilter assignee;
    private BooleanFilter endProcess;
    private StringFilter state;
    private StringFilter stateDemande;
    private LongFilter numberOfattachments;
    private LongFilter step;
    private StringFilter affectedSid;
    private StringFilter affectedName;
    private StringFilter affectedKeycloakId;
    private StringFilter sidExterne;
    private BooleanFilter delaiReponse;
    private BooleanFilter web;
    private BooleanFilter validation;
    private StringFilter commentaire;
    private LongFilter asiId;
    private ZonedDateTimeFilter sysdateCreated;
    private ZonedDateTimeFilter sysdateUpdated;
    private StringFilter syscreatedBy;
    private StringFilter sysupdatedBy;
    private StringFilter search;

    public DemandeComplementCriteria() {
    }

    public DemandeComplementCriteria(DemandeComplementCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.reference = other.reference == null ? null : other.reference.copy();
        this.referenceDossier = other.referenceDossier == null ? null : other.referenceDossier.copy();
        this.subject = other.subject == null ? null : other.subject.copy();
        this.description = other.description == null ? null : other.description.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.sendedDate = other.sendedDate == null ? null : other.sendedDate.copy();
        this.approvedBy = other.approvedBy == null ? null : other.approvedBy.copy();
        this.categorie = other.categorie == null ? null : other.categorie.copy();
        this.idsPostAttachments = other.idsPostAttachments == null ? null : other.idsPostAttachments.copy();
        this.labelsPostAttachments = other.labelsPostAttachments == null ? null : other.labelsPostAttachments.copy();
        this.labelsMissingPostAttachments = other.labelsMissingPostAttachments == null ? null : other.labelsMissingPostAttachments.copy();
        this.wfProcessID = other.wfProcessID == null ? null : other.wfProcessID.copy();
        this.classId = other.classId == null ? null : other.classId.copy();
        this.classIdDossier = other.classIdDossier == null ? null : other.classIdDossier.copy();
        this.objectIdDossier = other.objectIdDossier == null ? null : other.objectIdDossier.copy();
        this.activityName = other.activityName == null ? null : other.activityName.copy();
        this.assignee = other.assignee == null ? null : other.assignee.copy();
        this.endProcess = other.endProcess == null ? null : other.endProcess.copy();
        this.state = other.state == null ? null : other.state.copy();
        this.stateDemande = other.stateDemande == null ? null : other.stateDemande.copy();
        this.numberOfattachments = other.numberOfattachments == null ? null : other.numberOfattachments.copy();
        this.step = other.step == null ? null : other.step.copy();
        this.affectedSid = other.affectedSid == null ? null : other.affectedSid.copy();
        this.affectedName = other.affectedName == null ? null : other.affectedName.copy();
        this.affectedKeycloakId = other.affectedKeycloakId == null ? null : other.affectedKeycloakId.copy();
        this.sidExterne = other.sidExterne == null ? null : other.sidExterne.copy();
        this.delaiReponse = other.delaiReponse == null ? null : other.delaiReponse.copy();
        this.web = other.web == null ? null : other.web.copy();
        this.validation = other.validation == null ? null : other.validation.copy();
        this.commentaire = other.commentaire == null ? null : other.commentaire.copy();
        this.asiId = other.asiId == null ? null : other.asiId.copy();
        this.sysdateCreated = other.sysdateCreated == null ? null : other.sysdateCreated.copy();
        this.sysdateUpdated = other.sysdateUpdated == null ? null : other.sysdateUpdated.copy();
        this.syscreatedBy = other.syscreatedBy == null ? null : other.syscreatedBy.copy();
        this.sysupdatedBy = other.sysupdatedBy == null ? null : other.sysupdatedBy.copy();
        this.description = other.description == null ? null : other.description.copy();
        this.search = other.search == null ? null : other.search.copy();
    }

    @Override
    public DemandeComplementCriteria copy() {
        return new DemandeComplementCriteria(this);
    }
}
