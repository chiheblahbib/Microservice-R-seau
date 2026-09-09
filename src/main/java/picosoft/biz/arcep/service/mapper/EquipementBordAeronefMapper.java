package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.aeronef.EquipementBord;
import picosoft.biz.arcep.service.dto.EquipementBordAeronefDTO;

/** Mapper de {@link EquipementBord} et de son DTO {@link EquipementBordAeronefDTO}. */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class EquipementBordAeronefMapper implements EntityMapper<EquipementBordAeronefDTO, EquipementBord> {

    @Mapping(source = "demandeAeronef.id", target = "demandeAeronefId")
    public abstract EquipementBordAeronefDTO toDto(EquipementBord entite);

    @Mapping(target = "demandeAeronef", ignore = true)
    public abstract EquipementBord toEntity(EquipementBordAeronefDTO dto);

    EquipementBord fromId(Long id) {
        if (id == null) {
            return null;
        }
        EquipementBord entite = new EquipementBord();
        entite.setId(id);
        return entite;
    }
}
