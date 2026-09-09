package picosoft.biz.arcep.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import picosoft.biz.arcep.domain.declaratif.DemandeDeclaratif;
import picosoft.biz.arcep.service.dto.DemandeDeclaratifInputDTO;

/** Mapper de {@link DemandeDeclaratif} et de son DTO {@link DemandeDeclaratifInputDTO}. */
@Mapper(componentModel = "spring",
        uses = {ClientMapper.class, PersonneDeclaratifMapper.class, ServiceDeclareDeclaratifMapper.class, InfrastructureDeclareeMapper.class,
                RapportTechniqueMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandeDeclaratifInputMapper implements EntityMapper<DemandeDeclaratifInputDTO, DemandeDeclaratif> {

    public abstract DemandeDeclaratifInputDTO toDto(DemandeDeclaratif demandeDeclaratif);

    public abstract DemandeDeclaratif toEntity(DemandeDeclaratifInputDTO dto);

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
    public abstract void partialUpdate(@MappingTarget DemandeDeclaratif entity, DemandeDeclaratifInputDTO dto);

    @AfterMapping
    protected void fusionnerTitulaire(@MappingTarget DemandeDeclaratif entity, DemandeDeclaratifInputDTO dto) {
        if (dto.getClient() == null) {
            return;
        }
        if (entity.getClient() == null) {
            entity.setClient(clientMapper.toEntity(dto.getClient()));
        } else {
            clientMapper.partialUpdate(entity.getClient(), dto.getClient());
        }
    }

    DemandeDeclaratif fromId(Long id) {
        if (id == null) {
            return null;
        }
        DemandeDeclaratif demande = new DemandeDeclaratif();
        demande.setId(id);
        return demande;
    }
}
