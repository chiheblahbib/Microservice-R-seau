package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.ussd.DemandeUssd;
import picosoft.biz.arcep.domain.ussd.PersonneUssd;
import picosoft.biz.arcep.service.dto.PersonneUssdDTO;

/** Mapper de {@link PersonneUssd} et de son DTO {@link PersonneUssdDTO}. */
@Mapper(componentModel = "spring", uses = {},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class PersonneUssdMapper implements EntityMapper<PersonneUssdDTO, PersonneUssd> {

    @Mapping(target = "demandeUssdId", source = "demandeUssd.id")
    public abstract PersonneUssdDTO toDto(PersonneUssd entite);

    @Mapping(target = "demandeUssd", source = "demandeUssdId")
    public abstract PersonneUssd toEntity(PersonneUssdDTO dto);

    DemandeUssd map(Long id) {
        if (id == null) {
            return null;
        }
        DemandeUssd demande = new DemandeUssd();
        demande.setId(id);
        return demande;
    }

    PersonneUssd fromId(Long id) {
        if (id == null) {
            return null;
        }
        PersonneUssd entite = new PersonneUssd();
        entite.setId(id);
        return entite;
    }
}
