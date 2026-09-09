package picosoft.biz.arcep.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import picosoft.biz.arcep.domain.pq.DemandePq;
import picosoft.biz.arcep.service.dto.DemandePqOutputDTO;

/** Mapper de {@link DemandePq} et de son DTO {@link DemandePqOutputDTO}. */
@Mapper(componentModel = "spring",
        uses = {ClientMapper.class, PersonnePqMapper.class, BlocNumerosMapper.class,
                AttestationMapper.class, RapportTechniqueMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandePqOutputMapper implements EntityMapper<DemandePqOutputDTO, DemandePq> {

    public abstract DemandePqOutputDTO toDto(DemandePq demandePq);

    public abstract DemandePq toEntity(DemandePqOutputDTO dto);

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
    public abstract void partialUpdate(@MappingTarget DemandePq entity, DemandePqOutputDTO dto);

    @AfterMapping
    protected void fusionnerTitulaire(@MappingTarget DemandePq entity, DemandePqOutputDTO dto) {
        if (dto.getClient() == null) {
            return;
        }
        if (entity.getClient() == null) {
            entity.setClient(clientMapper.toEntity(dto.getClient()));
        } else {
            clientMapper.partialUpdate(entity.getClient(), dto.getClient());
        }
    }

    DemandePq fromId(Long id) {
        if (id == null) {
            return null;
        }
        DemandePq demande = new DemandePq();
        demande.setId(id);
        return demande;
    }
}
