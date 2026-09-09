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
import picosoft.biz.arcep.domain.implantation.enumeration.*;

import java.io.Serializable;

/**
 * Criteria class for the {@link picosoft.biz.arcep.domain.implantation.Station} entity.
 * Bound from the HTTP GET query parameters, converted to a Specification by the query service.
 * Example : {@code /stations?id.greaterThan=5&reference.contains=something}
 */
@Getter
@Setter
public class StationCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    public static class NatureImplantationFilter extends Filter<NatureImplantation> {
        private static final long serialVersionUID = 1L;

        public NatureImplantationFilter() {
        }

        public NatureImplantationFilter(NatureImplantationFilter filter) {
            super(filter);
        }

        @Override
        public NatureImplantationFilter copy() {
            return new NatureImplantationFilter(this);
        }
    }

    public static class TypeStationFilter extends Filter<TypeStation> {
        private static final long serialVersionUID = 1L;

        public TypeStationFilter() {
        }

        public TypeStationFilter(TypeStationFilter filter) {
            super(filter);
        }

        @Override
        public TypeStationFilter copy() {
            return new TypeStationFilter(this);
        }
    }

    public static class TypeTraficFilter extends Filter<TypeTrafic> {
        private static final long serialVersionUID = 1L;

        public TypeTraficFilter() {
        }

        public TypeTraficFilter(TypeTraficFilter filter) {
            super(filter);
        }

        @Override
        public TypeTraficFilter copy() {
            return new TypeTraficFilter(this);
        }
    }

    public static class NatureTraficFilter extends Filter<NatureTrafic> {
        private static final long serialVersionUID = 1L;

        public NatureTraficFilter() {
        }

        public NatureTraficFilter(NatureTraficFilter filter) {
            super(filter);
        }

        @Override
        public NatureTraficFilter copy() {
            return new NatureTraficFilter(this);
        }
    }

    public static class NatureSupportFilter extends Filter<NatureSupport> {
        private static final long serialVersionUID = 1L;

        public NatureSupportFilter() {
        }

        public NatureSupportFilter(NatureSupportFilter filter) {
            super(filter);
        }

        @Override
        public NatureSupportFilter copy() {
            return new NatureSupportFilter(this);
        }
    }

    public static class TypeAntenneFilter extends Filter<TypeAntenne> {
        private static final long serialVersionUID = 1L;

        public TypeAntenneFilter() {
        }

        public TypeAntenneFilter(TypeAntenneFilter filter) {
            super(filter);
        }

        @Override
        public TypeAntenneFilter copy() {
            return new TypeAntenneFilter(this);
        }
    }

    public static class StatutStationFilter extends Filter<StatutStation> {
        private static final long serialVersionUID = 1L;

        public StatutStationFilter() {
        }

        public StatutStationFilter(StatutStationFilter filter) {
            super(filter);
        }

        @Override
        public StatutStationFilter copy() {
            return new StatutStationFilter(this);
        }
    }

    private LongFilter id;
    private StringFilter reference;
    private NatureImplantationFilter natureImplantation;
    private TypeStationFilter typeStation;
    private TypeTraficFilter typeTrafic;
    private NatureTraficFilter natureTrafic;
    private NatureSupportFilter natureSupport;
    private TypeAntenneFilter typeAntenne;
    private StatutStationFilter statutStation;
    private DoubleFilter hauteurTotale;
    private LongFilter demandeImplantationId;
    private StringFilter installateurRaisonSociale;
    private StringFilter siteNom;
    private StringFilter siteProvince;
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

    public StationCriteria() {
    }

    public StationCriteria(StationCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.reference = other.reference == null ? null : other.reference.copy();
        this.natureImplantation = other.natureImplantation == null ? null : other.natureImplantation.copy();
        this.typeStation = other.typeStation == null ? null : other.typeStation.copy();
        this.typeTrafic = other.typeTrafic == null ? null : other.typeTrafic.copy();
        this.natureTrafic = other.natureTrafic == null ? null : other.natureTrafic.copy();
        this.natureSupport = other.natureSupport == null ? null : other.natureSupport.copy();
        this.typeAntenne = other.typeAntenne == null ? null : other.typeAntenne.copy();
        this.statutStation = other.statutStation == null ? null : other.statutStation.copy();
        this.hauteurTotale = other.hauteurTotale == null ? null : other.hauteurTotale.copy();
        this.demandeImplantationId = other.demandeImplantationId == null ? null : other.demandeImplantationId.copy();
        this.installateurRaisonSociale = other.installateurRaisonSociale == null ? null : other.installateurRaisonSociale.copy();
        this.siteNom = other.siteNom == null ? null : other.siteNom.copy();
        this.siteProvince = other.siteProvince == null ? null : other.siteProvince.copy();
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
    public StationCriteria copy() {
        return new StationCriteria(this);
    }
}
