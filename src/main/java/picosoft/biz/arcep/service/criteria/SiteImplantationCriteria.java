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
 * Criteria class for the {@link picosoft.biz.arcep.domain.implantation.SiteImplantation} entity.
 * Bound from the HTTP GET query parameters, converted to a Specification by the query service.
 * Example : {@code /site-implantations?id.greaterThan=5&reference.contains=something}
 */
@Getter
@Setter
public class SiteImplantationCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;


    private LongFilter id;
    private StringFilter nomSite;
    private StringFilter province;
    private StringFilter villeQuartier;
    private StringFilter departementCantonVillage;
    private DoubleFilter altitude;
    private BooleanFilter pylonePresent;
    private BooleanFilter zoneClassee;
    private LongFilter stationId;
    private ZonedDateTimeFilter sysdateCreated;
    private ZonedDateTimeFilter sysdateUpdated;
    private StringFilter syscreatedBy;
    private StringFilter sysupdatedBy;

    public SiteImplantationCriteria() {
    }

    public SiteImplantationCriteria(SiteImplantationCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.nomSite = other.nomSite == null ? null : other.nomSite.copy();
        this.province = other.province == null ? null : other.province.copy();
        this.villeQuartier = other.villeQuartier == null ? null : other.villeQuartier.copy();
        this.departementCantonVillage = other.departementCantonVillage == null ? null : other.departementCantonVillage.copy();
        this.altitude = other.altitude == null ? null : other.altitude.copy();
        this.pylonePresent = other.pylonePresent == null ? null : other.pylonePresent.copy();
        this.zoneClassee = other.zoneClassee == null ? null : other.zoneClassee.copy();
        this.stationId = other.stationId == null ? null : other.stationId.copy();
        this.sysdateCreated = other.sysdateCreated == null ? null : other.sysdateCreated.copy();
        this.sysdateUpdated = other.sysdateUpdated == null ? null : other.sysdateUpdated.copy();
        this.syscreatedBy = other.syscreatedBy == null ? null : other.syscreatedBy.copy();
        this.sysupdatedBy = other.sysupdatedBy == null ? null : other.sysupdatedBy.copy();
    }

    @Override
    public SiteImplantationCriteria copy() {
        return new SiteImplantationCriteria(this);
    }
}
