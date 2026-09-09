package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picosoft.biz.arcep.domain.shared.RefTarif;
import picosoft.biz.arcep.service.dto.RefTarifDTO;

/**
 * Mapper for the entity {@link RefTarif} and its DTO {@link RefTarifDTO}.
 */
@Mapper(componentModel = "spring",  uses = {}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class RefTarifMapper implements EntityMapper<RefTarifDTO, RefTarif> {

    private final Logger log = LoggerFactory.getLogger(RefTarifMapper.class);

    public abstract RefTarifDTO toDto(RefTarif refTarif);

    public abstract RefTarif toEntity(RefTarifDTO refTarifDTO);


    RefTarif fromId(Long id) {
        if (id == null) {
            return null;
        }
        RefTarif refTarif = new RefTarif();
        refTarif.setId(id);
        return refTarif;
    }
}
