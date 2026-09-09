package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.installateur.OutillageDeclare;
import picosoft.biz.arcep.service.dto.OutillageDeclareDTO;

/** Mapper de {@link OutillageDeclare} et de son DTO. */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class OutillageDeclareMapper implements EntityMapper<OutillageDeclareDTO, OutillageDeclare> {

    @Mapping(source = "demandeInstallateur.id", target = "demandeInstallateurId")
    public abstract OutillageDeclareDTO toDto(OutillageDeclare entite);

    @Mapping(target = "demandeInstallateur", ignore = true)
    public abstract OutillageDeclare toEntity(OutillageDeclareDTO dto);

    OutillageDeclare fromId(Long id) {
        if (id == null) {
            return null;
        }
        OutillageDeclare entite = new OutillageDeclare();
        entite.setId(id);
        return entite;
    }
}
