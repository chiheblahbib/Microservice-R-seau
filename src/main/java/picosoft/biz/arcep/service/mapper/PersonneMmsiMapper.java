package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import picosoft.biz.arcep.domain.mmsi.DemandeMmsi;
import picosoft.biz.arcep.domain.mmsi.PersonneMmsi;
import picosoft.biz.arcep.service.dto.PersonneMmsiDTO;

/** Mapper de {@link PersonneMmsi} et de son DTO {@link PersonneMmsiDTO}. */
@Mapper(componentModel = "spring", uses = {},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class PersonneMmsiMapper implements EntityMapper<PersonneMmsiDTO, PersonneMmsi> {

    @Mapping(target = "demandeMmsiId", source = "demandeMmsi.id")
    public abstract PersonneMmsiDTO toDto(PersonneMmsi entite);

    @Mapping(target = "demandeMmsi", source = "demandeMmsiId")
    public abstract PersonneMmsi toEntity(PersonneMmsiDTO dto);

    DemandeMmsi map(Long id) {
        if (id == null) {
            return null;
        }
        DemandeMmsi demande = new DemandeMmsi();
        demande.setId(id);
        return demande;
    }

    PersonneMmsi fromId(Long id) {
        if (id == null) {
            return null;
        }
        PersonneMmsi entite = new PersonneMmsi();
        entite.setId(id);
        return entite;
    }
}
