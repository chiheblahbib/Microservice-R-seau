package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picosoft.biz.arcep.domain.implantation.Station;
import picosoft.biz.arcep.domain.implantation.SiteImplantation;
import picosoft.biz.arcep.service.dto.SiteImplantationDTO;

/**
 * Mapper for the entity {@link SiteImplantation} and its DTO {@link SiteImplantationDTO}.
 */
@Mapper(componentModel = "spring",  uses = {}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class SiteImplantationMapper implements EntityMapper<SiteImplantationDTO, SiteImplantation> {

    private final Logger log = LoggerFactory.getLogger(SiteImplantationMapper.class);

    @Mapping(target = "stationId", source = "station.id")
    public abstract SiteImplantationDTO toDto(SiteImplantation siteImplantation);

    @Mapping(target = "station", source = "stationId")
    public abstract SiteImplantation toEntity(SiteImplantationDTO siteImplantationDTO);

    Station map(Long id) {
        if (id == null) {
            return null;
        }
        Station station = new Station();
        station.setId(id);
        return station;
    }

    SiteImplantation fromId(Long id) {
        if (id == null) {
            return null;
        }
        SiteImplantation siteImplantation = new SiteImplantation();
        siteImplantation.setId(id);
        return siteImplantation;
    }
}
