package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picosoft.biz.arcep.domain.shared.DemandeComplement;
import picosoft.biz.arcep.domain.shared.Destinataire;
import picosoft.biz.arcep.service.dto.DestinataireDTO;

@Mapper(componentModel = "spring", uses = {}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DestinataireMapper implements EntityMapper<DestinataireDTO, Destinataire> {

    private final Logger log = LoggerFactory.getLogger(DestinataireMapper.class);

    @Mapping(source = "demandeComplement.id", target = "demandeComplementId")
    public abstract DestinataireDTO toDto(Destinataire destinataire);

    @Mapping(source = "demandeComplementId", target = "demandeComplement")
    public abstract Destinataire toEntity(DestinataireDTO destinataireDTO);

    DemandeComplement fromId(Long id) {
        if (id == null) {
            return null;
        }
        DemandeComplement demandeComplement = new DemandeComplement();
        demandeComplement.setId(id);
        return demandeComplement;
    }
}
