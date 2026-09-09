package picosoft.biz.arcep.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import picosoft.biz.arcep.domain.pq.DemandePq;
import picosoft.biz.arcep.service.dto.DemandePqInputDTO;

/** Mapper de {@link DemandePq} et de son DTO {@link DemandePqInputDTO}. */
@Mapper(componentModel = "spring",
        uses = {ClientMapper.class, PersonnePqMapper.class, BlocNumerosMapper.class,
                RapportTechniqueMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandePqInputMapper implements EntityMapper<DemandePqInputDTO, DemandePq> {

    public abstract DemandePqInputDTO toDto(DemandePq demandePq);

    public abstract DemandePq toEntity(DemandePqInputDTO dto);

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
    public abstract void partialUpdate(@MappingTarget DemandePq entity, DemandePqInputDTO dto);

    @AfterMapping
    protected void fusionnerTitulaire(@MappingTarget DemandePq entity, DemandePqInputDTO dto) {
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
