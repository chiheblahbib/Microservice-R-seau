package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.domain.reseau.TypeReseauDeclare;
import picosoft.biz.arcep.service.dto.TypeReseauDeclareDTO;

/** Mapper de {@link TypeReseauDeclare} et de son DTO {@link TypeReseauDeclareDTO}. */
@Mapper(componentModel = "spring", uses = {},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class TypeReseauDeclareMapper implements EntityMapper<TypeReseauDeclareDTO, TypeReseauDeclare> {

    @Mapping(target = "demandeReseauId", source = "demandeReseau.id")
    public abstract TypeReseauDeclareDTO toDto(TypeReseauDeclare entite);

    @Mapping(target = "demandeReseau", source = "demandeReseauId")
    public abstract TypeReseauDeclare toEntity(TypeReseauDeclareDTO dto);

    DemandeReseau map(Long id) {
        if (id == null) {
            return null;
        }
        DemandeReseau demande = new DemandeReseau();
        demande.setId(id);
        return demande;
    }

    TypeReseauDeclare fromId(Long id) {
        if (id == null) {
            return null;
        }
        TypeReseauDeclare entite = new TypeReseauDeclare();
        entite.setId(id);
        return entite;
    }
}
