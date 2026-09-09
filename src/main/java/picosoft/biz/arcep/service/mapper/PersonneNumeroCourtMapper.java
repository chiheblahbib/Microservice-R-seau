package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.numerocourt.DemandeNumeroCourt;
import picosoft.biz.arcep.domain.numerocourt.PersonneNumeroCourt;
import picosoft.biz.arcep.service.dto.PersonneNumeroCourtDTO;

/** Mapper de {@link PersonneNumeroCourt} et de son DTO {@link PersonneNumeroCourtDTO}. */
@Mapper(componentModel = "spring", uses = {},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class PersonneNumeroCourtMapper implements EntityMapper<PersonneNumeroCourtDTO, PersonneNumeroCourt> {

    @Mapping(target = "demandeNumeroCourtId", source = "demandeNumeroCourt.id")
    public abstract PersonneNumeroCourtDTO toDto(PersonneNumeroCourt entite);

    @Mapping(target = "demandeNumeroCourt", source = "demandeNumeroCourtId")
    public abstract PersonneNumeroCourt toEntity(PersonneNumeroCourtDTO dto);

    DemandeNumeroCourt map(Long id) {
        if (id == null) {
            return null;
        }
        DemandeNumeroCourt demande = new DemandeNumeroCourt();
        demande.setId(id);
        return demande;
    }

    PersonneNumeroCourt fromId(Long id) {
        if (id == null) {
            return null;
        }
        PersonneNumeroCourt entite = new PersonneNumeroCourt();
        entite.setId(id);
        return entite;
    }
}
