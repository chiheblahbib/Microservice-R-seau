package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.navire.AutorisationAnterieure;
import picosoft.biz.arcep.service.dto.AutorisationAnterieureDTO;

/** Mapper de {@link AutorisationAnterieure} et de son DTO. */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class AutorisationAnterieureMapper
        implements EntityMapper<AutorisationAnterieureDTO, AutorisationAnterieure> {

    @Mapping(source = "demandeNavire.id", target = "demandeNavireId")
    public abstract AutorisationAnterieureDTO toDto(AutorisationAnterieure entite);

    @Mapping(target = "demandeNavire", ignore = true)
    public abstract AutorisationAnterieure toEntity(AutorisationAnterieureDTO dto);

    AutorisationAnterieure fromId(Long id) {
        if (id == null) {
            return null;
        }
        AutorisationAnterieure entite = new AutorisationAnterieure();
        entite.setId(id);
        return entite;
    }
}
