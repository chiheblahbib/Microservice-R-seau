package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.declaratif.ServiceDeclare;
import picosoft.biz.arcep.service.dto.ServiceDeclareDeclaratifDTO;

/** Mapper de {@link ServiceDeclare} et de son DTO. */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ServiceDeclareDeclaratifMapper implements EntityMapper<ServiceDeclareDeclaratifDTO, ServiceDeclare> {

    @Mapping(source = "demandeDeclaratif.id", target = "demandeDeclaratifId")
    public abstract ServiceDeclareDeclaratifDTO toDto(ServiceDeclare entite);

    @Mapping(target = "demandeDeclaratif", ignore = true)
    public abstract ServiceDeclare toEntity(ServiceDeclareDeclaratifDTO dto);

    ServiceDeclare fromId(Long id) {
        if (id == null) {
            return null;
        }
        ServiceDeclare entite = new ServiceDeclare();
        entite.setId(id);
        return entite;
    }
}
