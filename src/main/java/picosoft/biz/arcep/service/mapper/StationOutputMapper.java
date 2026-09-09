package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picosoft.biz.arcep.domain.implantation.Station;
import picosoft.biz.arcep.domain.implantation.DemandeImplantation;
import picosoft.biz.arcep.service.dto.StationOutputDTO;

/**
 * Mapper for the entity {@link Station} and its DTO {@link StationOutputDTO}.
 */
@Mapper(componentModel = "spring",  uses = {SiteImplantationMapper.class, FrequenceMapper.class, AttestationMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class StationOutputMapper implements EntityMapper<StationOutputDTO, Station> {

    private final Logger log = LoggerFactory.getLogger(StationOutputMapper.class);

    @Mapping(target = "demandeImplantationId", source = "demandeImplantation.id")
    public abstract StationOutputDTO toDto(Station station);

    @Mapping(target = "demandeImplantation", source = "demandeImplantationId")
    public abstract Station toEntity(StationOutputDTO dto);

    DemandeImplantation map(Long id) {
        if (id == null) {
            return null;
        }
        DemandeImplantation demandeImplantation = new DemandeImplantation();
        demandeImplantation.setId(id);
        return demandeImplantation;
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
