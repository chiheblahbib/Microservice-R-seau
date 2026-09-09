package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.ispc.FonctionPointSemaphore;
import picosoft.biz.arcep.service.dto.FonctionPointSemaphoreDTO;

/** Mapper de {@link FonctionPointSemaphore} et de son DTO. */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class FonctionPointSemaphoreMapper
        implements EntityMapper<FonctionPointSemaphoreDTO, FonctionPointSemaphore> {

    @Mapping(source = "demandeIspc.id", target = "demandeIspcId")
    public abstract FonctionPointSemaphoreDTO toDto(FonctionPointSemaphore entite);

    @Mapping(target = "demandeIspc", ignore = true)
    public abstract FonctionPointSemaphore toEntity(FonctionPointSemaphoreDTO dto);

    FonctionPointSemaphore fromId(Long id) {
        if (id == null) {
            return null;
        }
        FonctionPointSemaphore entite = new FonctionPointSemaphore();
        entite.setId(id);
        return entite;
    }
}
