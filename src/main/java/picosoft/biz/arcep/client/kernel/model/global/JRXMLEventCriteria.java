package picosoft.biz.arcep.client.kernel.model.global;


import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /jrxml-events?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class JRXMLEventCriteria implements Serializable, Criteria {
    private static final long serialVersionUID = 1L;
    private LongFilter id;
    private StringFilter classname;
    private LongFilter objectID;
    private StringFilter inputData;
    private StringFilter outputData;
    private StringFilter comment;
    private StringFilter outputFileName;
    private StringFilter outputformat;
    private StringFilter requesterror;
    private StringFilter outputDone;
    private ZonedDateTimeFilter dateProcessed;
    private ZonedDateTimeFilter sysdateCreated;
    private ZonedDateTimeFilter sysdateUpdated;
    private StringFilter syscreatedBy;
    private BooleanFilter sysupdatedBy;
    private BooleanFilter sysdeleted;
    private StringFilter sysreader;
    private StringFilter sysauthor;
    private StringFilter placeholder;
    private LongFilter jrxmlTemplateDTOId;
    private StringFilter jrxmlTemplateDTOName;
    private ZonedDateTimeFilter evtDate;
    private LongFilter evtId;

    public LongFilter getEvtId() {
        return evtId;
    }

    public void setEvtId(LongFilter evtId) {
        this.evtId = evtId;
    }

    public ZonedDateTimeFilter getEvtDate() {
        return evtDate;
    }

    public void setEvtDate(ZonedDateTimeFilter evtDate) {
        this.evtDate = evtDate;
    }

    public JRXMLEventCriteria() {
    }

    public JRXMLEventCriteria(JRXMLEventCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.classname = other.classname == null ? null : other.classname.copy();
        this.objectID = other.objectID == null ? null : other.objectID.copy();
        this.inputData = other.inputData == null ? null : other.inputData.copy();
        this.comment = other.comment == null ? null : other.comment.copy();
        this.outputFileName = other.outputFileName == null ? null : other.outputFileName.copy();
        this.outputformat = other.outputformat == null ? null : other.outputformat.copy();
        this.requesterror = other.requesterror == null ? null : other.requesterror.copy();
        this.outputDone = other.outputDone == null ? null : other.outputDone.copy();
        this.dateProcessed = other.dateProcessed == null ? null : other.dateProcessed.copy();
        this.sysdateCreated = other.sysdateCreated == null ? null : other.sysdateCreated.copy();
        this.sysdateUpdated = other.sysdateUpdated == null ? null : other.sysdateUpdated.copy();
        this.syscreatedBy = other.syscreatedBy == null ? null : other.syscreatedBy.copy();
        this.sysupdatedBy = other.sysupdatedBy == null ? null : other.sysupdatedBy.copy();
        this.sysdeleted = other.sysdeleted == null ? null : other.sysdeleted.copy();
        this.sysreader = other.sysreader == null ? null : other.sysreader.copy();
        this.sysauthor = other.sysauthor == null ? null : other.sysauthor.copy();
        this.placeholder = other.placeholder == null ? null : other.placeholder.copy();
    }

    @Override
    public JRXMLEventCriteria copy() {
        return new JRXMLEventCriteria(this);
    }


    public StringFilter getOutputData() {
        return outputData;
    }

    public void setOutputData(StringFilter outputData) {
        this.outputData = outputData;
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getClassname() {
        return classname;
    }

    public void setClassname(StringFilter classname) {
        this.classname = classname;
    }

    public LongFilter getObjectID() {
        return objectID;
    }

    public void setObjectID(LongFilter objectID) {
        this.objectID = objectID;
    }

    public StringFilter getInputData() {
        return inputData;
    }

    public void setInputData(StringFilter inputData) {
        this.inputData = inputData;
    }

    public StringFilter getComment() {
        return comment;
    }

    public void setComment(StringFilter comment) {
        this.comment = comment;
    }

    public StringFilter getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(StringFilter outputFileName) {
        this.outputFileName = outputFileName;
    }

    public StringFilter getOutputformat() {
        return outputformat;
    }

    public void setOutputformat(StringFilter outputformat) {
        this.outputformat = outputformat;
    }

    public StringFilter getRequesterror() {
        return requesterror;
    }

    public void setRequesterror(StringFilter requesterror) {
        this.requesterror = requesterror;
    }

    public StringFilter getOutputDone() {
        return outputDone;
    }

    public void setOutputDone(StringFilter outputDone) {
        this.outputDone = outputDone;
    }

    public ZonedDateTimeFilter getDateProcessed() {
        return dateProcessed;
    }

    public void setDateProcessed(ZonedDateTimeFilter dateProcessed) {
        this.dateProcessed = dateProcessed;
    }

    public ZonedDateTimeFilter getSysdateCreated() {
        return sysdateCreated;
    }

    public void setSysdateCreated(ZonedDateTimeFilter sysdateCreated) {
        this.sysdateCreated = sysdateCreated;
    }

    public ZonedDateTimeFilter getSysdateUpdated() {
        return sysdateUpdated;
    }

    public void setSysdateUpdated(ZonedDateTimeFilter sysdateUpdated) {
        this.sysdateUpdated = sysdateUpdated;
    }

    public StringFilter getSyscreatedBy() {
        return syscreatedBy;
    }

    public void setSyscreatedBy(StringFilter syscreatedBy) {
        this.syscreatedBy = syscreatedBy;
    }

    public BooleanFilter getSysupdatedBy() {
        return sysupdatedBy;
    }

    public void setSysupdatedBy(BooleanFilter sysupdatedBy) {
        this.sysupdatedBy = sysupdatedBy;
    }

    public BooleanFilter getSysdeleted() {
        return sysdeleted;
    }

    public void setSysdeleted(BooleanFilter sysdeleted) {
        this.sysdeleted = sysdeleted;
    }

    public StringFilter getSysreader() {
        return sysreader;
    }

    public void setSysreader(StringFilter sysreader) {
        this.sysreader = sysreader;
    }

    public StringFilter getSysauthor() {
        return sysauthor;
    }

    public void setSysauthor(StringFilter sysauthor) {
        this.sysauthor = sysauthor;
    }

    public StringFilter getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(StringFilter placeholder) {
        this.placeholder = placeholder;
    }

    public LongFilter getJrxmlTemplateDTOId() {
        return jrxmlTemplateDTOId;
    }

    public void setJrxmlTemplateDTOId(LongFilter jrxmlTemplateDTOId) {
        this.jrxmlTemplateDTOId = jrxmlTemplateDTOId;
    }

    public StringFilter getJrxmlTemplateDTOName() {
        return jrxmlTemplateDTOName;
    }

    public void setJrxmlTemplateDTOName(StringFilter jrxmlTemplateDTOName) {
        this.jrxmlTemplateDTOName = jrxmlTemplateDTOName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final JRXMLEventCriteria that = (JRXMLEventCriteria) o;
        return
                Objects.equals(id, that.id) &&
                        Objects.equals(classname, that.classname) &&
                        Objects.equals(objectID, that.objectID) &&
                        Objects.equals(inputData, that.inputData) &&
                        Objects.equals(comment, that.comment) &&
                        Objects.equals(outputFileName, that.outputFileName) &&
                        Objects.equals(outputformat, that.outputformat) &&
                        Objects.equals(requesterror, that.requesterror) &&
                        Objects.equals(outputDone, that.outputDone) &&
                        Objects.equals(dateProcessed, that.dateProcessed) &&
                        Objects.equals(sysdateCreated, that.sysdateCreated) &&
                        Objects.equals(sysdateUpdated, that.sysdateUpdated) &&
                        Objects.equals(syscreatedBy, that.syscreatedBy) &&
                        Objects.equals(sysupdatedBy, that.sysupdatedBy) &&
                        Objects.equals(sysdeleted, that.sysdeleted) &&
                        Objects.equals(sysreader, that.sysreader) &&
                        Objects.equals(sysauthor, that.sysauthor) &&
                        Objects.equals(placeholder, that.placeholder) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                classname,
                objectID,
                inputData,
                comment,
                outputFileName,
                outputformat,
                requesterror,
                outputDone,
                dateProcessed,
                sysdateCreated,
                sysdateUpdated,
                syscreatedBy,
                sysupdatedBy,
                sysdeleted,
                sysreader,
                sysauthor,
                placeholder
        );
    }

    @Override
    public String toString() {
        return "JRXMLEventCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (classname != null ? "classname=" + classname + ", " : "") +
                (objectID != null ? "objectID=" + objectID + ", " : "") +
                (inputData != null ? "inputData=" + inputData + ", " : "") +
                (comment != null ? "comment=" + comment + ", " : "") +
                (outputFileName != null ? "outputFileName=" + outputFileName + ", " : "") +
                (outputformat != null ? "outputformat=" + outputformat + ", " : "") +
                (requesterror != null ? "requesterror=" + requesterror + ", " : "") +
                (outputDone != null ? "outputDone=" + outputDone + ", " : "") +
                (dateProcessed != null ? "dateProcessed=" + dateProcessed + ", " : "") +
                (sysdateCreated != null ? "sysdateCreated=" + sysdateCreated + ", " : "") +
                (sysdateUpdated != null ? "sysdateUpdated=" + sysdateUpdated + ", " : "") +
                (syscreatedBy != null ? "syscreatedBy=" + syscreatedBy + ", " : "") +
                (sysupdatedBy != null ? "sysupdatedBy=" + sysupdatedBy + ", " : "") +
                (sysdeleted != null ? "sysdeleted=" + sysdeleted + ", " : "") +
                (sysreader != null ? "sysreader=" + sysreader + ", " : "") +
                (sysauthor != null ? "sysauthor=" + sysauthor + ", " : "") +
                (placeholder != null ? "placeholder=" + placeholder + ", " : "") +
                "}";
    }



}
