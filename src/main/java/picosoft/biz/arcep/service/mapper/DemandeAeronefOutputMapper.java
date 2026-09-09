package picosoft.biz.arcep.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import picosoft.biz.arcep.domain.aeronef.DemandeAeronef;
import picosoft.biz.arcep.service.dto.DemandeAeronefOutputDTO;

/** Mapper de {@link DemandeAeronef} et de son DTO {@link DemandeAeronefOutputDTO}. */
@Mapper(componentModel = "spring",
        uses = {ClientMapper.class, ApplicantMapper.class, EquipementBordAeronefMapper.class,
                VerificationControleMapper.class,
                AttestationMapper.class, RapportTechniqueMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandeAeronefOutputMapper implements EntityMapper<DemandeAeronefOutputDTO, DemandeAeronef> {

    public abstract DemandeAeronefOutputDTO toDto(DemandeAeronef demandeAeronef);

    public abstract DemandeAeronef toEntity(DemandeAeronefOutputDTO dto);

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
    public abstract void partialUpdate(@MappingTarget DemandeAeronef entity, DemandeAeronefOutputDTO dto);

    @AfterMapping
    protected void fusionnerTitulaire(@MappingTarget DemandeAeronef entity, DemandeAeronefOutputDTO dto) {
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
