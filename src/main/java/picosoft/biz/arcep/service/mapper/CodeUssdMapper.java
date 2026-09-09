package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.ussd.CodeUssd;
import picosoft.biz.arcep.service.dto.CodeUssdDTO;

/** Mapper de {@link CodeUssd} et de son DTO. */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class CodeUssdMapper implements EntityMapper<CodeUssdDTO, CodeUssd> {

    @Mapping(source = "demandeUssd.id", target = "demandeUssdId")
    public abstract CodeUssdDTO toDto(CodeUssd entite);

    @Mapping(target = "demandeUssd", ignore = true)
    public abstract CodeUssd toEntity(CodeUssdDTO dto);

    CodeUssd fromId(Long id) {
        if (id == null) {
            return null;
        }
        CodeUssd entite = new CodeUssd();
        entite.setId(id);
        return entite;
    }
}
