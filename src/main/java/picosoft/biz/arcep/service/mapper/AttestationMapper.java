package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picosoft.biz.arcep.domain.shared.Attestation;
import picosoft.biz.arcep.service.dto.AttestationDTO;

/**
 * Mapper for the entity {@link Attestation} and its DTO {@link AttestationDTO}.
 */
@Mapper(componentModel = "spring",  uses = {}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class AttestationMapper implements EntityMapper<AttestationDTO, Attestation> {

    private final Logger log = LoggerFactory.getLogger(AttestationMapper.class);

    public abstract AttestationDTO toDto(Attestation attestation);

    public abstract Attestation toEntity(AttestationDTO attestationDTO);


    Attestation fromId(Long id) {
        if (id == null) {
            return null;
        }
        Attestation attestation = new Attestation();
        attestation.setId(id);
        return attestation;
    }
}
