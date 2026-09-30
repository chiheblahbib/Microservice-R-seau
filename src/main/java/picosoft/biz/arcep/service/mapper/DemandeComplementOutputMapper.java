package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picosoft.biz.arcep.domain.shared.DemandeComplement;
import picosoft.biz.arcep.service.dto.DemandeComplementOutputDTO;

@Mapper(componentModel = "spring", uses = {ReponseDemandeComplementMapper.class, DestinataireMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandeComplementOutputMapper implements EntityMapper<DemandeComplementOutputDTO, DemandeComplement> {

    private final Logger log = LoggerFactory.getLogger(DemandeComplementOutputMapper.class);

    @Mapping(source = "reponseDemandeComplement", target = "reponseDemandeComplement")
    @Mapping(source = "destinataire", target = "destinataire")
    public abstract DemandeComplementOutputDTO toDto(DemandeComplement entity);

    @Mapping(source = "reponseDemandeComplement", target = "reponseDemandeComplement")
    @Mapping(source = "destinataire", target = "destinataire")
    public abstract DemandeComplement toEntity(DemandeComplementOutputDTO dto);

}
