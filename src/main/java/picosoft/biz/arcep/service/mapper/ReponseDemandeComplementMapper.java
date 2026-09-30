package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picosoft.biz.arcep.domain.shared.DemandeComplement;
import picosoft.biz.arcep.domain.shared.ReponseDemandeComplement;
import picosoft.biz.arcep.service.dto.ReponseDemandeComplementDTO;

@Mapper(componentModel = "spring", uses = {}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ReponseDemandeComplementMapper implements EntityMapper<ReponseDemandeComplementDTO, ReponseDemandeComplement> {

    private final Logger log = LoggerFactory.getLogger(ReponseDemandeComplementMapper.class);

    @Mapping(source = "demandeComplement.id", target = "demandeComplementId")
    public abstract ReponseDemandeComplementDTO toDto(ReponseDemandeComplement reponseDemandeComplement);

    @Mapping(source = "demandeComplementId", target = "demandeComplement")
    public abstract ReponseDemandeComplement toEntity(ReponseDemandeComplementDTO reponseDemandeComplementDTO);

    DemandeComplement fromId(Long id) {
        if (id == null) {
            return null;
        }
        DemandeComplement demandeComplement = new DemandeComplement();
        demandeComplement.setId(id);
        return demandeComplement;
    }
}
