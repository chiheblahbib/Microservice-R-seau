package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picosoft.biz.arcep.domain.implantation.DemandeImplantation;
import picosoft.biz.arcep.service.dto.DemandeImplantationOutputDTO;

/**
 * Mapper for the entity {@link DemandeImplantation} and its DTO {@link DemandeImplantationOutputDTO}.
 */
@Mapper(componentModel = "spring",  uses = {ClientMapper.class, ApplicantMapper.class, StationMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandeImplantationOutputMapper implements EntityMapper<DemandeImplantationOutputDTO, DemandeImplantation> {

    private final Logger log = LoggerFactory.getLogger(DemandeImplantationOutputMapper.class);

    public abstract DemandeImplantationOutputDTO toDto(DemandeImplantation demandeImplantation);

    public abstract DemandeImplantation toEntity(DemandeImplantationOutputDTO dto);


    DemandeImplantation fromId(Long id) {
        if (id == null) {
            return null;
        }
        DemandeImplantation demandeImplantation = new DemandeImplantation();
        demandeImplantation.setId(id);
        return demandeImplantation;
    }
}
