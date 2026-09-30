package picosoft.biz.arcep.service.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picosoft.biz.arcep.domain.shared.DemandeComplement;
import picosoft.biz.arcep.service.dto.DemandeComplementInputDTO;

@Mapper(componentModel = "spring", uses = {ReponseDemandeComplementMapper.class, DestinataireMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandeComplementInputMapper implements EntityMapper<DemandeComplementInputDTO, DemandeComplement> {

    private final Logger log = LoggerFactory.getLogger(DemandeComplementInputMapper.class);

    @Mapping(source = "reponseDemandeComplement", target = "reponseDemandeComplement")
    @Mapping(source = "destinataire", target = "destinataire")
    public abstract DemandeComplementInputDTO toDto(DemandeComplement entity);

    @Mapping(source = "reponseDemandeComplement", target = "reponseDemandeComplement")
    @Mapping(target = "destinataire", ignore = true)
    public abstract DemandeComplement toEntity(DemandeComplementInputDTO dto);

    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "reponseDemandeComplement", target = "reponseDemandeComplement")
    @Mapping(target = "destinataire", ignore = true)
    public abstract void partialUpdate(@MappingTarget DemandeComplement entity, DemandeComplementInputDTO dto);

}
