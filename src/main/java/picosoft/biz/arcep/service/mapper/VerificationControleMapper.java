package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.aeronef.VerificationControle;
import picosoft.biz.arcep.service.dto.VerificationControleDTO;

/** Mapper de {@link VerificationControle} et de son DTO {@link VerificationControleDTO}. */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class VerificationControleMapper implements EntityMapper<VerificationControleDTO, VerificationControle> {

    @Mapping(source = "demandeAeronef.id", target = "demandeAeronefId")
    public abstract VerificationControleDTO toDto(VerificationControle entite);

    @Mapping(target = "demandeAeronef", ignore = true)
    public abstract VerificationControle toEntity(VerificationControleDTO dto);

    VerificationControle fromId(Long id) {
        if (id == null) {
            return null;
        }
        VerificationControle entite = new VerificationControle();
        entite.setId(id);
        return entite;
    }
}
