package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.pq.DemandePq;
import picosoft.biz.arcep.domain.pq.PersonnePq;
import picosoft.biz.arcep.service.dto.PersonnePqDTO;

/** Mapper de {@link PersonnePq} et de son DTO {@link PersonnePqDTO}. */
@Mapper(componentModel = "spring", uses = {},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class PersonnePqMapper implements EntityMapper<PersonnePqDTO, PersonnePq> {

    @Mapping(target = "demandePqId", source = "demandePq.id")
    public abstract PersonnePqDTO toDto(PersonnePq entite);

    @Mapping(target = "demandePq", source = "demandePqId")
    public abstract PersonnePq toEntity(PersonnePqDTO dto);

    DemandePq map(Long id) {
        if (id == null) {
            return null;
        }
        DemandePq demande = new DemandePq();
        demande.setId(id);
        return demande;
    }

    PersonnePq fromId(Long id) {
        if (id == null) {
            return null;
        }
        PersonnePq entite = new PersonnePq();
        entite.setId(id);
        return entite;
    }
}
