package picosoft.biz.arcep.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picosoft.biz.arcep.domain.implantation.Station;
import picosoft.biz.arcep.domain.implantation.DemandeImplantation;
import picosoft.biz.arcep.service.dto.StationInputDTO;

/**
 * Mapper for the entity {@link Station} and its DTO {@link StationInputDTO}.
 */
@Mapper(componentModel = "spring",  uses = {SiteImplantationMapper.class, FrequenceMapper.class, AttestationMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class StationInputMapper implements EntityMapper<StationInputDTO, Station> {

    private final Logger log = LoggerFactory.getLogger(StationInputMapper.class);

    @Mapping(target = "demandeImplantationId", source = "demandeImplantation.id")
    public abstract StationInputDTO toDto(Station station);

    @Mapping(target = "demandeImplantation", source = "demandeImplantationId")
    public abstract Station toEntity(StationInputDTO dto);

    DemandeImplantation map(Long id) {
        if (id == null) {
            return null;
        }
        DemandeImplantation demandeImplantation = new DemandeImplantation();
        demandeImplantation.setId(id);
        return demandeImplantation;
    }

    @Autowired
    protected SiteImplantationMapper siteImplantationMapper;

    /**
     * Les relations un-a-un sont retirees du partialUpdate genere : MapStruct les
     * remplacerait par une instance neuve, ce qui viole la contrainte unique portee
     * par la cle etrangere de l'enfant. On les fusionne ici, en place.
     */
    @Mapping(target = "siteImplantation", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void partialUpdate(@MappingTarget Station entity, StationInputDTO dto);

    @AfterMapping
    protected void fusionnerEnfantsUnAUn(@MappingTarget Station entity, StationInputDTO dto) {
        if (dto.getSiteImplantation() != null) {
            if (entity.getSiteImplantation() == null) {
                entity.setSiteImplantation(siteImplantationMapper.toEntity(dto.getSiteImplantation()));
            } else {
                siteImplantationMapper.partialUpdate(entity.getSiteImplantation(), dto.getSiteImplantation());
            }
        }
    }

    Station fromId(Long id) {
        if (id == null) {
            return null;
        }
        Station station = new Station();
        station.setId(id);
        return station;
    }
}
