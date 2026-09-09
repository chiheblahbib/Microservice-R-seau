package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.mmsi.BesoinMmsi;
import picosoft.biz.arcep.service.dto.BesoinMmsiDTO;

/** Mapper de {@link BesoinMmsi} et de son DTO. */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class BesoinMmsiMapper implements EntityMapper<BesoinMmsiDTO, BesoinMmsi> {

    @Mapping(source = "demandeMmsi.id", target = "demandeMmsiId")
    public abstract BesoinMmsiDTO toDto(BesoinMmsi entite);

    @Mapping(target = "demandeMmsi", ignore = true)
    public abstract BesoinMmsi toEntity(BesoinMmsiDTO dto);

    BesoinMmsi fromId(Long id) {
        if (id == null) {
            return null;
        }
        BesoinMmsi entite = new BesoinMmsi();
        entite.setId(id);
        return entite;
    }
}
