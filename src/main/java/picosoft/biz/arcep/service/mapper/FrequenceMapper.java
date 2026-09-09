package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picosoft.biz.arcep.domain.implantation.Station;
import picosoft.biz.arcep.domain.implantation.Frequence;
import picosoft.biz.arcep.service.dto.FrequenceDTO;

/**
 * Mapper for the entity {@link Frequence} and its DTO {@link FrequenceDTO}.
 */
@Mapper(componentModel = "spring",  uses = {}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class FrequenceMapper implements EntityMapper<FrequenceDTO, Frequence> {

    private final Logger log = LoggerFactory.getLogger(FrequenceMapper.class);

    @Mapping(target = "stationId", source = "station.id")
    public abstract FrequenceDTO toDto(Frequence frequence);

    @Mapping(target = "station", source = "stationId")
    public abstract Frequence toEntity(FrequenceDTO frequenceDTO);

    Station map(Long id) {
        if (id == null) {
            return null;
        }
        Station station = new Station();
        station.setId(id);
        return station;
    }

    Frequence fromId(Long id) {
        if (id == null) {
            return null;
        }
        Frequence frequence = new Frequence();
        frequence.setId(id);
        return frequence;
    }
}
