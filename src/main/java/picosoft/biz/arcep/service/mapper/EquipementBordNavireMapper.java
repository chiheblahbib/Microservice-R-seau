package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.navire.EquipementBord;
import picosoft.biz.arcep.service.dto.EquipementBordNavireDTO;

/** Mapper de {@link EquipementBord} et de son DTO {@link EquipementBordNavireDTO}. */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class EquipementBordNavireMapper implements EntityMapper<EquipementBordNavireDTO, EquipementBord> {

    @Mapping(source = "demandeNavire.id", target = "demandeNavireId")
    public abstract EquipementBordNavireDTO toDto(EquipementBord entite);

    @Mapping(target = "demandeNavire", ignore = true)
    public abstract EquipementBord toEntity(EquipementBordNavireDTO dto);

    EquipementBord fromId(Long id) {
        if (id == null) {
            return null;
        }
        EquipementBord entite = new EquipementBord();
        entite.setId(id);
        return entite;
    }
}
