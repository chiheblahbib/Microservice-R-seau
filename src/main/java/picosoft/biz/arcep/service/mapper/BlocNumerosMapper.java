package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.pq.BlocNumeros;
import picosoft.biz.arcep.service.dto.BlocNumerosDTO;

/** Mapper de {@link BlocNumeros} et de son DTO. */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class BlocNumerosMapper implements EntityMapper<BlocNumerosDTO, BlocNumeros> {

    @Mapping(source = "demandePq.id", target = "demandePqId")
    public abstract BlocNumerosDTO toDto(BlocNumeros entite);

    @Mapping(target = "demandePq", ignore = true)
    public abstract BlocNumeros toEntity(BlocNumerosDTO dto);

    BlocNumeros fromId(Long id) {
        if (id == null) {
            return null;
        }
        BlocNumeros entite = new BlocNumeros();
        entite.setId(id);
        return entite;
    }
}
