package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.declaratif.DemandeDeclaratif;
import picosoft.biz.arcep.domain.declaratif.PersonneDeclaratif;
import picosoft.biz.arcep.service.dto.PersonneDeclaratifDTO;

/** Mapper de {@link PersonneDeclaratif} et de son DTO {@link PersonneDeclaratifDTO}. */
@Mapper(componentModel = "spring", uses = {},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class PersonneDeclaratifMapper implements EntityMapper<PersonneDeclaratifDTO, PersonneDeclaratif> {

    @Mapping(target = "demandeDeclaratifId", source = "demandeDeclaratif.id")
    public abstract PersonneDeclaratifDTO toDto(PersonneDeclaratif entite);

    @Mapping(target = "demandeDeclaratif", source = "demandeDeclaratifId")
    public abstract PersonneDeclaratif toEntity(PersonneDeclaratifDTO dto);

    DemandeDeclaratif map(Long id) {
        if (id == null) {
            return null;
        }
        DemandeDeclaratif demande = new DemandeDeclaratif();
        demande.setId(id);
        return demande;
    }

    PersonneDeclaratif fromId(Long id) {
        if (id == null) {
            return null;
        }
        PersonneDeclaratif entite = new PersonneDeclaratif();
        entite.setId(id);
        return entite;
    }
}
