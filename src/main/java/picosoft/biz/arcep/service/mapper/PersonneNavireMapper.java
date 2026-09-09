package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.navire.DemandeNavire;
import picosoft.biz.arcep.domain.navire.PersonneNavire;
import picosoft.biz.arcep.service.dto.PersonneNavireDTO;

/** Mapper de {@link PersonneNavire} et de son DTO {@link PersonneNavireDTO}. */
@Mapper(componentModel = "spring", uses = {},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class PersonneNavireMapper implements EntityMapper<PersonneNavireDTO, PersonneNavire> {

    @Mapping(target = "demandeNavireId", source = "demandeNavire.id")
    public abstract PersonneNavireDTO toDto(PersonneNavire entite);

    @Mapping(target = "demandeNavire", source = "demandeNavireId")
    public abstract PersonneNavire toEntity(PersonneNavireDTO dto);

    DemandeNavire map(Long id) {
        if (id == null) {
            return null;
        }
        DemandeNavire demande = new DemandeNavire();
        demande.setId(id);
        return demande;
    }

    PersonneNavire fromId(Long id) {
        if (id == null) {
            return null;
        }
        PersonneNavire entite = new PersonneNavire();
        entite.setId(id);
        return entite;
    }
}
