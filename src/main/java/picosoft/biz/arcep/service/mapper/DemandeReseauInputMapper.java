package picosoft.biz.arcep.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.service.dto.DemandeReseauInputDTO;

/** Mapper de {@link DemandeReseau} et de son DTO {@link DemandeReseauInputDTO}. */
@Mapper(componentModel = "spring",
        uses = {ClientMapper.class, ApplicantMapper.class, PersonneReseauMapper.class, TypeReseauDeclareMapper.class,
                ServiceDeclareMapper.class, SiteReseauMapper.class, LiaisonReseauMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandeReseauInputMapper implements EntityMapper<DemandeReseauInputDTO, DemandeReseau> {

    public abstract DemandeReseauInputDTO toDto(DemandeReseau demandeReseau);

    public abstract DemandeReseau toEntity(DemandeReseauInputDTO dto);

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
    public abstract void partialUpdate(@MappingTarget DemandeReseau entity, DemandeReseauInputDTO dto);

    @AfterMapping
    protected void fusionnerTitulaire(@MappingTarget DemandeReseau entity, DemandeReseauInputDTO dto) {
        if (dto.getClient() == null) {
            return;
        }
        if (entity.getClient() == null) {
            entity.setClient(clientMapper.toEntity(dto.getClient()));
        } else {
            clientMapper.partialUpdate(entity.getClient(), dto.getClient());
        }
    }

    DemandeReseau fromId(Long id) {
        if (id == null) {
            return null;
        }
        DemandeReseau demande = new DemandeReseau();
        demande.setId(id);
        return demande;
    }
}
