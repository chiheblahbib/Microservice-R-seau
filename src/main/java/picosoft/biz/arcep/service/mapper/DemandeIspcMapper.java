package picosoft.biz.arcep.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import picosoft.biz.arcep.domain.ispc.DemandeIspc;
import picosoft.biz.arcep.service.dto.DemandeIspcDTO;

/** Mapper de {@link DemandeIspc} et de son DTO {@link DemandeIspcDTO}. */
@Mapper(componentModel = "spring",
        uses = {ClientMapper.class, PersonneIspcMapper.class, FonctionPointSemaphoreMapper.class,
                AttestationMapper.class, RapportTechniqueMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandeIspcMapper implements EntityMapper<DemandeIspcDTO, DemandeIspc> {

    public abstract DemandeIspcDTO toDto(DemandeIspc demandeIspc);

    public abstract DemandeIspc toEntity(DemandeIspcDTO dto);

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
    public abstract void partialUpdate(@MappingTarget DemandeIspc entity, DemandeIspcDTO dto);

    @AfterMapping
    protected void fusionnerTitulaire(@MappingTarget DemandeIspc entity, DemandeIspcDTO dto) {
        if (dto.getClient() == null) {
            return;
        }
        if (entity.getClient() == null) {
            entity.setClient(clientMapper.toEntity(dto.getClient()));
        } else {
            clientMapper.partialUpdate(entity.getClient(), dto.getClient());
        }
    }

    DemandeIspc fromId(Long id) {
        if (id == null) {
            return null;
        }
        DemandeIspc demande = new DemandeIspc();
        demande.setId(id);
        return demande;
    }
}
