package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.installateur.TechnicienSpecialiste;
import picosoft.biz.arcep.service.dto.TechnicienSpecialisteDTO;

/** Mapper de {@link TechnicienSpecialiste} et de son DTO. */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class TechnicienSpecialisteMapper implements EntityMapper<TechnicienSpecialisteDTO, TechnicienSpecialiste> {

    @Mapping(source = "demandeInstallateur.id", target = "demandeInstallateurId")
    public abstract TechnicienSpecialisteDTO toDto(TechnicienSpecialiste entite);

    @Mapping(target = "demandeInstallateur", ignore = true)
    public abstract TechnicienSpecialiste toEntity(TechnicienSpecialisteDTO dto);

    TechnicienSpecialiste fromId(Long id) {
        if (id == null) {
            return null;
        }
        TechnicienSpecialiste entite = new TechnicienSpecialiste();
        entite.setId(id);
        return entite;
    }
}
