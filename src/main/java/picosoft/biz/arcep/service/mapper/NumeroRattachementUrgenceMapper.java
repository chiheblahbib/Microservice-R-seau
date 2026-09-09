package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.numerocourturgence.NumeroRattachement;
import picosoft.biz.arcep.service.dto.NumeroRattachementUrgenceDTO;

/** Mapper de {@link NumeroRattachement} et de son DTO. */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class NumeroRattachementUrgenceMapper
        implements EntityMapper<NumeroRattachementUrgenceDTO, NumeroRattachement> {

    @Mapping(source = "demandeNumeroCourtUrgence.id", target = "demandeNumeroCourtUrgenceId")
    public abstract NumeroRattachementUrgenceDTO toDto(NumeroRattachement entite);

    @Mapping(target = "demandeNumeroCourtUrgence", ignore = true)
    public abstract NumeroRattachement toEntity(NumeroRattachementUrgenceDTO dto);

    NumeroRattachement fromId(Long id) {
        if (id == null) {
            return null;
        }
        NumeroRattachement entite = new NumeroRattachement();
        entite.setId(id);
        return entite;
    }
}
