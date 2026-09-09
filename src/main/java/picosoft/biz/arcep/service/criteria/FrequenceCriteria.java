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
 * Criteria class for the {@link picosoft.biz.arcep.domain.implantation.Frequence} entity.
 * Bound from the HTTP GET query parameters, converted to a Specification by the query service.
 * Example : {@code /frequences?id.greaterThan=5&reference.contains=something}
 */
@Getter
@Setter
public class FrequenceCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    public static class SensFrequenceFilter extends Filter<SensFrequence> {
        private static final long serialVersionUID = 1L;

        public SensFrequenceFilter() {
        }

        public SensFrequenceFilter(SensFrequenceFilter filter) {
            super(filter);
        }

        @Override
        public SensFrequenceFilter copy() {
            return new SensFrequenceFilter(this);
        }
    }

    private LongFilter id;
    private SensFrequenceFilter sens;
    private DoubleFilter frequenceCentraleMhz;
    private DoubleFilter bandeMhz;
    private DoubleFilter zoneServiceKm;
    private DoubleFilter parWatts;
    private LongFilter stationId;
    private ZonedDateTimeFilter sysdateCreated;
    private ZonedDateTimeFilter sysdateUpdated;
    private StringFilter syscreatedBy;
    private StringFilter sysupdatedBy;

    public FrequenceCriteria() {
    }

    public FrequenceCriteria(FrequenceCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.sens = other.sens == null ? null : other.sens.copy();
        this.frequenceCentraleMhz = other.frequenceCentraleMhz == null ? null : other.frequenceCentraleMhz.copy();
        this.bandeMhz = other.bandeMhz == null ? null : other.bandeMhz.copy();
        this.zoneServiceKm = other.zoneServiceKm == null ? null : other.zoneServiceKm.copy();
        this.parWatts = other.parWatts == null ? null : other.parWatts.copy();
        this.stationId = other.stationId == null ? null : other.stationId.copy();
        this.sysdateCreated = other.sysdateCreated == null ? null : other.sysdateCreated.copy();
        this.sysdateUpdated = other.sysdateUpdated == null ? null : other.sysdateUpdated.copy();
        this.syscreatedBy = other.syscreatedBy == null ? null : other.syscreatedBy.copy();
        this.sysupdatedBy = other.sysupdatedBy == null ? null : other.sysupdatedBy.copy();
    }

    @Override
    public FrequenceCriteria copy() {
        return new FrequenceCriteria(this);
    }
}
