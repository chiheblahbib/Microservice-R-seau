package picosoft.biz.arcep.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import picosoft.biz.arcep.domain.aeronef.DemandeAeronef;
import picosoft.biz.arcep.service.dto.DemandeAeronefInputDTO;

/** Mapper de {@link DemandeAeronef} et de son DTO {@link DemandeAeronefInputDTO}. */
@Mapper(componentModel = "spring",
        uses = {ClientMapper.class, PersonneAeronefMapper.class, EquipementBordAeronefMapper.class,
                VerificationControleMapper.class, RapportTechniqueMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandeAeronefInputMapper implements EntityMapper<DemandeAeronefInputDTO, DemandeAeronef> {

    public abstract DemandeAeronefInputDTO toDto(DemandeAeronef demandeAeronef);

    public abstract DemandeAeronef toEntity(DemandeAeronefInputDTO dto);

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
    public abstract void partialUpdate(@MappingTarget DemandeAeronef entity, DemandeAeronefInputDTO dto);

    @AfterMapping
    protected void fusionnerTitulaire(@MappingTarget DemandeAeronef entity, DemandeAeronefInputDTO dto) {
        if (dto.getClient() == null) {
            return;
        }
        if (entity.getClient() == null) {
            entity.setClient(clientMapper.toEntity(dto.getClient()));
        } else {
            clientMapper.partialUpdate(entity.getClient(), dto.getClient());
        }
    }

    DemandeAeronef fromId(Long id) {
        if (id == null) {
            return null;
        }
        DemandeAeronef demande = new DemandeAeronef();
        demande.setId(id);
        return demande;
    }
}
