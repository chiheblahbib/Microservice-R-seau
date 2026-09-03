package picosoft.biz.arcep.service.criteria;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.BigDecimalFilter;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.LocalDateFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.ZonedDateTimeFilter;
import lombok.Getter;
import lombok.Setter;
import picosoft.biz.arcep.domain.reseau.enumeration.MomentTarif;

import java.io.Serializable;

/**
 * Criteria class for the {@link picosoft.biz.arcep.domain.reseau.RefTarif} entity.
 */
@Getter
@Setter
public class RefTarifCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    public static class MomentTarifFilter extends Filter<MomentTarif> {
        private static final long serialVersionUID = 1L;

        public MomentTarifFilter() {
        }

        public MomentTarifFilter(MomentTarifFilter filter) {
            super(filter);
        }

        @Override
        public MomentTarifFilter copy() {
            return new MomentTarifFilter(this);
        }
    }

    private LongFilter id;
    private StringFilter code;
    private StringFilter libelle;
    private StringFilter service;
    private StringFilter application;
    private BigDecimalFilter montant;
    private StringFilter devise;
    private MomentTarifFilter moment;
    private LocalDateFilter dateEffet;
    private LocalDateFilter dateFin;
    private BooleanFilter actif;
    private ZonedDateTimeFilter sysdateCreated;
    private ZonedDateTimeFilter sysdateUpdated;
    private StringFilter syscreatedBy;
    private StringFilter sysupdatedBy;

    public RefTarifCriteria() {
    }

    public RefTarifCriteria(RefTarifCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.code = other.code == null ? null : other.code.copy();
        this.libelle = other.libelle == null ? null : other.libelle.copy();
        this.service = other.service == null ? null : other.service.copy();
        this.application = other.application == null ? null : other.application.copy();
        this.montant = other.montant == null ? null : other.montant.copy();
        this.devise = other.devise == null ? null : other.devise.copy();
        this.moment = other.moment == null ? null : other.moment.copy();
        this.dateEffet = other.dateEffet == null ? null : other.dateEffet.copy();
        this.dateFin = other.dateFin == null ? null : other.dateFin.copy();
        this.actif = other.actif == null ? null : other.actif.copy();
        this.sysdateCreated = other.sysdateCreated == null ? null : other.sysdateCreated.copy();
        this.sysdateUpdated = other.sysdateUpdated == null ? null : other.sysdateUpdated.copy();
        this.syscreatedBy = other.syscreatedBy == null ? null : other.syscreatedBy.copy();
        this.sysupdatedBy = other.sysupdatedBy == null ? null : other.sysupdatedBy.copy();
    }

    @Override
    public RefTarifCriteria copy() {
        return new RefTarifCriteria(this);
    }
}
