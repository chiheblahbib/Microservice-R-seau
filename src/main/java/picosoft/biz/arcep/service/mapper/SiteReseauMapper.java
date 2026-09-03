package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.domain.reseau.SiteReseau;
import picosoft.biz.arcep.service.dto.SiteReseauDTO;

/** Mapper de {@link SiteReseau} et de son DTO {@link SiteReseauDTO}. */
@Mapper(componentModel = "spring", uses = {},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class SiteReseauMapper implements EntityMapper<SiteReseauDTO, SiteReseau> {

    @Mapping(target = "demandeReseauId", source = "demandeReseau.id")
    public abstract SiteReseauDTO toDto(SiteReseau entite);

    @Mapping(target = "demandeReseau", source = "demandeReseauId")
    public abstract SiteReseau toEntity(SiteReseauDTO dto);

    DemandeReseau map(Long id) {
        if (id == null) {
            return null;
        }
        DemandeReseau demande = new DemandeReseau();
        demande.setId(id);
        return demande;
    }

    SiteReseau fromId(Long id) {
        if (id == null) {
            return null;
        }
        SiteReseau entite = new SiteReseau();
        entite.setId(id);
        return entite;
    }
}
