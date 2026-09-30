package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.drrrs.RapportTechnique;
import picosoft.biz.arcep.service.dto.RapportTechniqueDTO;

/**
 * Mapper de {@link RapportTechnique} et de son DTO {@link RapportTechniqueDTO}.
 *
 * Les dix retours vers les dossiers sont ignores a l'aller : c'est l'enfant qui
 * porte la cle etrangere, et c'est le service qui la pose -- voir
 * rattacherEnfants dans chaque Demande*Service.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class RapportTechniqueMapper implements EntityMapper<RapportTechniqueDTO, RapportTechnique> {

    public abstract RapportTechniqueDTO toDto(RapportTechnique rapport);

    @Mapping(target = "demandeAeronef", ignore = true)
    @Mapping(target = "demandeDeclaratif", ignore = true)
    @Mapping(target = "demandeInstallateur", ignore = true)
    @Mapping(target = "demandeIspc", ignore = true)
    @Mapping(target = "demandeMmsi", ignore = true)
    @Mapping(target = "demandeNavire", ignore = true)
    @Mapping(target = "demandeNumeroCourt", ignore = true)
    @Mapping(target = "demandeNumeroCourtUrgence", ignore = true)
    @Mapping(target = "demandePq", ignore = true)
    @Mapping(target = "demandeUssd", ignore = true)
    public abstract RapportTechnique toEntity(RapportTechniqueDTO dto);

    RapportTechnique fromId(Long id) {
        if (id == null) {
            return null;
        }
        RapportTechnique rapport = new RapportTechnique();
        rapport.setId(id);
        return rapport;
    }
}
