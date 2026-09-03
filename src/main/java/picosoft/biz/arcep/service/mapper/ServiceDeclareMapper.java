package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.domain.reseau.ServiceDeclare;
import picosoft.biz.arcep.service.dto.ServiceDeclareDTO;

/** Mapper de {@link ServiceDeclare} et de son DTO {@link ServiceDeclareDTO}. */
@Mapper(componentModel = "spring", uses = {},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ServiceDeclareMapper implements EntityMapper<ServiceDeclareDTO, ServiceDeclare> {

    @Mapping(target = "demandeReseauId", source = "demandeReseau.id")
    public abstract ServiceDeclareDTO toDto(ServiceDeclare entite);

    @Mapping(target = "demandeReseau", source = "demandeReseauId")
    public abstract ServiceDeclare toEntity(ServiceDeclareDTO dto);

    DemandeReseau map(Long id) {
        if (id == null) {
            return null;
        }
        DemandeReseau demande = new DemandeReseau();
        demande.setId(id);
        return demande;
    }

    ServiceDeclare fromId(Long id) {
        if (id == null) {
            return null;
        }
        ServiceDeclare entite = new ServiceDeclare();
        entite.setId(id);
        return entite;
    }
}
