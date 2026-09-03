package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.domain.reseau.LiaisonReseau;
import picosoft.biz.arcep.service.dto.LiaisonReseauDTO;
import picosoft.biz.arcep.domain.reseau.SiteReseau;

/** Mapper de {@link LiaisonReseau} et de son DTO {@link LiaisonReseauDTO}. */
@Mapper(componentModel = "spring", uses = {},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class LiaisonReseauMapper implements EntityMapper<LiaisonReseauDTO, LiaisonReseau> {

    @Mapping(target = "demandeReseauId", source = "demandeReseau.id")
    @Mapping(target = "siteOrigineId", source = "siteOrigine.id")
    @Mapping(target = "siteExtremiteId", source = "siteExtremite.id")
    public abstract LiaisonReseauDTO toDto(LiaisonReseau entite);

    @Mapping(target = "demandeReseau", source = "demandeReseauId")
    @Mapping(target = "siteOrigine", source = "siteOrigineId")
    @Mapping(target = "siteExtremite", source = "siteExtremiteId")
    public abstract LiaisonReseau toEntity(LiaisonReseauDTO dto);

    DemandeReseau map(Long id) {
        if (id == null) {
            return null;
        }
        DemandeReseau demande = new DemandeReseau();
        demande.setId(id);
        return demande;
    }

    /**
     * Les deux extremites sont des SiteReseau, pas des DemandeReseau : sans
     * cette fabrique, MapStruct emploierait celle du dessus et rattacherait la
     * liaison au mauvais type.
     */
    SiteReseau mapSite(Long id) {
        if (id == null) {
            return null;
        }
        SiteReseau site = new SiteReseau();
        site.setId(id);
        return site;
    }

    LiaisonReseau fromId(Long id) {
        if (id == null) {
            return null;
        }
        LiaisonReseau entite = new LiaisonReseau();
        entite.setId(id);
        return entite;
    }
}
