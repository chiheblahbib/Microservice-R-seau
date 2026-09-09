package picosoft.biz.arcep.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import picosoft.biz.arcep.domain.mmsi.DemandeMmsi;
import picosoft.biz.arcep.service.dto.DemandeMmsiOutputDTO;

/** Mapper de {@link DemandeMmsi} et de son DTO {@link DemandeMmsiOutputDTO}. */
@Mapper(componentModel = "spring",
        uses = {ClientMapper.class, PersonneMmsiMapper.class, BesoinMmsiMapper.class,
                AttestationMapper.class, RapportTechniqueMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandeMmsiOutputMapper implements EntityMapper<DemandeMmsiOutputDTO, DemandeMmsi> {

    public abstract DemandeMmsiOutputDTO toDto(DemandeMmsi demandeMmsi);

    public abstract DemandeMmsi toEntity(DemandeMmsiOutputDTO dto);

    @Autowired
    protected ClientMapper clientMapper;

    /**
     * Le titulaire est retire du partialUpdate genere.
     *
     * MapStruct le remplacerait par une instance neuve, ce qui viole la
     * contrainte unique portee par la cle etrangere de l'enfant. On le fusionne
     * ici, en place.
     */
    @Mapping(target = "client", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void partialUpdate(@MappingTarget DemandeMmsi entity, DemandeMmsiOutputDTO dto);

    @AfterMapping
    protected void fusionnerTitulaire(@MappingTarget DemandeMmsi entity, DemandeMmsiOutputDTO dto) {
        if (dto.getClient() == null) {
            return;
        }
        if (entity.getClient() == null) {
            entity.setClient(clientMapper.toEntity(dto.getClient()));
        } else {
            clientMapper.partialUpdate(entity.getClient(), dto.getClient());
        }
    }

    DemandeMmsi fromId(Long id) {
        if (id == null) {
            return null;
        }
        DemandeMmsi demande = new DemandeMmsi();
        demande.setId(id);
        return demande;
    }
}
