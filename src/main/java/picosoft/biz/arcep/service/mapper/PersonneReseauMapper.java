package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.domain.reseau.PersonneReseau;
import picosoft.biz.arcep.service.dto.PersonneReseauDTO;

/** Mapper de {@link PersonneReseau} et de son DTO {@link PersonneReseauDTO}. */
@Mapper(componentModel = "spring", uses = {},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class PersonneReseauMapper implements EntityMapper<PersonneReseauDTO, PersonneReseau> {

    @Mapping(target = "demandeReseauId", source = "demandeReseau.id")
    public abstract PersonneReseauDTO toDto(PersonneReseau entite);

    @Mapping(target = "demandeReseau", source = "demandeReseauId")
    public abstract PersonneReseau toEntity(PersonneReseauDTO dto);

    DemandeReseau map(Long id) {
        if (id == null) {
            return null;
        }
        DemandeReseau demande = new DemandeReseau();
        demande.setId(id);
        return demande;
    }

    PersonneReseau fromId(Long id) {
        if (id == null) {
            return null;
        }
        PersonneReseau entite = new PersonneReseau();
        entite.setId(id);
        return entite;
    }
}
