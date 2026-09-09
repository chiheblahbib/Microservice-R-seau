package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.numerocourt.NumeroRattachement;
import picosoft.biz.arcep.service.dto.NumeroRattachementCourtDTO;

/** Mapper de {@link NumeroRattachement} et de son DTO. */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class NumeroRattachementCourtMapper
        implements EntityMapper<NumeroRattachementCourtDTO, NumeroRattachement> {

    @Mapping(source = "demandeNumeroCourt.id", target = "demandeNumeroCourtId")
    public abstract NumeroRattachementCourtDTO toDto(NumeroRattachement entite);

    @Mapping(target = "demandeNumeroCourt", ignore = true)
    public abstract NumeroRattachement toEntity(NumeroRattachementCourtDTO dto);

    NumeroRattachement fromId(Long id) {
        if (id == null) {
            return null;
        }
        NumeroRattachement entite = new NumeroRattachement();
        entite.setId(id);
        return entite;
    }
}
