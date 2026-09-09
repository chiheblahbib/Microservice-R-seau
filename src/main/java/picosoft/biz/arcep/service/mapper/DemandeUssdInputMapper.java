package picosoft.biz.arcep.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import picosoft.biz.arcep.domain.ussd.DemandeUssd;
import picosoft.biz.arcep.service.dto.DemandeUssdInputDTO;

/** Mapper de {@link DemandeUssd} et de son DTO {@link DemandeUssdInputDTO}. */
@Mapper(componentModel = "spring",
        uses = {ClientMapper.class, PersonneUssdMapper.class, CodeUssdMapper.class,
                RapportTechniqueMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandeUssdInputMapper implements EntityMapper<DemandeUssdInputDTO, DemandeUssd> {

    public abstract DemandeUssdInputDTO toDto(DemandeUssd demandeUssd);

    public abstract DemandeUssd toEntity(DemandeUssdInputDTO dto);

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
    public abstract void partialUpdate(@MappingTarget DemandeUssd entity, DemandeUssdInputDTO dto);

    @AfterMapping
    protected void fusionnerTitulaire(@MappingTarget DemandeUssd entity, DemandeUssdInputDTO dto) {
        if (dto.getClient() == null) {
            return;
        }
        if (entity.getClient() == null) {
            entity.setClient(clientMapper.toEntity(dto.getClient()));
        } else {
            clientMapper.partialUpdate(entity.getClient(), dto.getClient());
        }
    }

    DemandeUssd fromId(Long id) {
        if (id == null) {
            return null;
        }
        DemandeUssd demande = new DemandeUssd();
        demande.setId(id);
        return demande;
    }
}
