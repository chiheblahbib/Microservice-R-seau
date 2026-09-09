package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.ispc.DemandeIspc;
import picosoft.biz.arcep.domain.ispc.PersonneIspc;
import picosoft.biz.arcep.service.dto.PersonneIspcDTO;

/** Mapper de {@link PersonneIspc} et de son DTO {@link PersonneIspcDTO}. */
@Mapper(componentModel = "spring", uses = {},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class PersonneIspcMapper implements EntityMapper<PersonneIspcDTO, PersonneIspc> {

    @Mapping(target = "demandeIspcId", source = "demandeIspc.id")
    public abstract PersonneIspcDTO toDto(PersonneIspc entite);

    @Mapping(target = "demandeIspc", source = "demandeIspcId")
    public abstract PersonneIspc toEntity(PersonneIspcDTO dto);

    DemandeIspc map(Long id) {
        if (id == null) {
            return null;
        }
        DemandeIspc demande = new DemandeIspc();
        demande.setId(id);
        return demande;
    }

    PersonneIspc fromId(Long id) {
        if (id == null) {
            return null;
        }
        PersonneIspc entite = new PersonneIspc();
        entite.setId(id);
        return entite;
    }
}
