package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.declaratif.InfrastructureDeclaree;
import picosoft.biz.arcep.service.dto.InfrastructureDeclareeDTO;

/** Mapper de {@link InfrastructureDeclaree} et de son DTO. */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class InfrastructureDeclareeMapper implements EntityMapper<InfrastructureDeclareeDTO, InfrastructureDeclaree> {

    @Mapping(source = "demandeDeclaratif.id", target = "demandeDeclaratifId")
    public abstract InfrastructureDeclareeDTO toDto(InfrastructureDeclaree entite);

    @Mapping(target = "demandeDeclaratif", ignore = true)
    public abstract InfrastructureDeclaree toEntity(InfrastructureDeclareeDTO dto);

    InfrastructureDeclaree fromId(Long id) {
        if (id == null) {
            return null;
        }
        InfrastructureDeclaree entite = new InfrastructureDeclaree();
        entite.setId(id);
        return entite;
    }
}
