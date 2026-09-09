package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.installateur.QualiteDemandee;
import picosoft.biz.arcep.service.dto.QualiteDemandeeDTO;

/** Mapper de {@link QualiteDemandee} et de son DTO. */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class QualiteDemandeeMapper implements EntityMapper<QualiteDemandeeDTO, QualiteDemandee> {

    @Mapping(source = "demandeInstallateur.id", target = "demandeInstallateurId")
    public abstract QualiteDemandeeDTO toDto(QualiteDemandee entite);

    @Mapping(target = "demandeInstallateur", ignore = true)
    public abstract QualiteDemandee toEntity(QualiteDemandeeDTO dto);

    QualiteDemandee fromId(Long id) {
        if (id == null) {
            return null;
        }
        QualiteDemandee entite = new QualiteDemandee();
        entite.setId(id);
        return entite;
    }
}
