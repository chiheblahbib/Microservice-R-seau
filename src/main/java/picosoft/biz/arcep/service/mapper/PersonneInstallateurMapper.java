package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.installateur.DemandeInstallateur;
import picosoft.biz.arcep.domain.installateur.PersonneInstallateur;
import picosoft.biz.arcep.service.dto.PersonneInstallateurDTO;

/** Mapper de {@link PersonneInstallateur} et de son DTO {@link PersonneInstallateurDTO}. */
@Mapper(componentModel = "spring", uses = {},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class PersonneInstallateurMapper implements EntityMapper<PersonneInstallateurDTO, PersonneInstallateur> {

    @Mapping(target = "demandeInstallateurId", source = "demandeInstallateur.id")
    public abstract PersonneInstallateurDTO toDto(PersonneInstallateur entite);

    @Mapping(target = "demandeInstallateur", source = "demandeInstallateurId")
    public abstract PersonneInstallateur toEntity(PersonneInstallateurDTO dto);

    DemandeInstallateur map(Long id) {
        if (id == null) {
            return null;
        }
        DemandeInstallateur demande = new DemandeInstallateur();
        demande.setId(id);
        return demande;
    }

    PersonneInstallateur fromId(Long id) {
        if (id == null) {
            return null;
        }
        PersonneInstallateur entite = new PersonneInstallateur();
        entite.setId(id);
        return entite;
    }
}
