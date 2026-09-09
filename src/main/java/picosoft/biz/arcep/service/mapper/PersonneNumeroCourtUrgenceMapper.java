package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.numerocourturgence.DemandeNumeroCourtUrgence;
import picosoft.biz.arcep.domain.numerocourturgence.PersonneNumeroCourtUrgence;
import picosoft.biz.arcep.service.dto.PersonneNumeroCourtUrgenceDTO;

/** Mapper de {@link PersonneNumeroCourtUrgence} et de son DTO {@link PersonneNumeroCourtUrgenceDTO}. */
@Mapper(componentModel = "spring", uses = {},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class PersonneNumeroCourtUrgenceMapper implements EntityMapper<PersonneNumeroCourtUrgenceDTO, PersonneNumeroCourtUrgence> {

    @Mapping(target = "demandeNumeroCourtUrgenceId", source = "demandeNumeroCourtUrgence.id")
    public abstract PersonneNumeroCourtUrgenceDTO toDto(PersonneNumeroCourtUrgence entite);

    @Mapping(target = "demandeNumeroCourtUrgence", source = "demandeNumeroCourtUrgenceId")
    public abstract PersonneNumeroCourtUrgence toEntity(PersonneNumeroCourtUrgenceDTO dto);

    DemandeNumeroCourtUrgence map(Long id) {
        if (id == null) {
            return null;
        }
        DemandeNumeroCourtUrgence demande = new DemandeNumeroCourtUrgence();
        demande.setId(id);
        return demande;
    }

    PersonneNumeroCourtUrgence fromId(Long id) {
        if (id == null) {
            return null;
        }
        PersonneNumeroCourtUrgence entite = new PersonneNumeroCourtUrgence();
        entite.setId(id);
        return entite;
    }
}
