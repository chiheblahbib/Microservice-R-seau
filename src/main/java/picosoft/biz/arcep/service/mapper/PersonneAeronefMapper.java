package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.aeronef.DemandeAeronef;
import picosoft.biz.arcep.domain.aeronef.PersonneAeronef;
import picosoft.biz.arcep.service.dto.PersonneAeronefDTO;

/** Mapper de {@link PersonneAeronef} et de son DTO {@link PersonneAeronefDTO}. */
@Mapper(componentModel = "spring", uses = {},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class PersonneAeronefMapper implements EntityMapper<PersonneAeronefDTO, PersonneAeronef> {

    @Mapping(target = "demandeAeronefId", source = "demandeAeronef.id")
    public abstract PersonneAeronefDTO toDto(PersonneAeronef entite);

    @Mapping(target = "demandeAeronef", source = "demandeAeronefId")
    public abstract PersonneAeronef toEntity(PersonneAeronefDTO dto);

    DemandeAeronef map(Long id) {
        if (id == null) {
            return null;
        }
        DemandeAeronef demande = new DemandeAeronef();
        demande.setId(id);
        return demande;
    }

    PersonneAeronef fromId(Long id) {
        if (id == null) {
            return null;
        }
        PersonneAeronef entite = new PersonneAeronef();
        entite.setId(id);
        return entite;
    }
}
