package picosoft.biz.arcep.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import picosoft.biz.arcep.domain.navire.DemandeNavire;
import picosoft.biz.arcep.service.dto.DemandeNavireInputDTO;

/** Mapper de {@link DemandeNavire} et de son DTO {@link DemandeNavireInputDTO}. */
@Mapper(componentModel = "spring",
        uses = {ClientMapper.class, ApplicantMapper.class, EquipementBordNavireMapper.class,
                RapportTechniqueMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandeNavireInputMapper implements EntityMapper<DemandeNavireInputDTO, DemandeNavire> {

    public abstract DemandeNavireInputDTO toDto(DemandeNavire demandeNavire);

    public abstract DemandeNavire toEntity(DemandeNavireInputDTO dto);

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
    public abstract void partialUpdate(@MappingTarget DemandeNavire entity, DemandeNavireInputDTO dto);

    @AfterMapping
    protected void fusionnerTitulaire(@MappingTarget DemandeNavire entity, DemandeNavireInputDTO dto) {
        if (dto.getClient() == null) {
            return;
        }
        if (entity.getClient() == null) {
            entity.setClient(clientMapper.toEntity(dto.getClient()));
        } else {
            clientMapper.partialUpdate(entity.getClient(), dto.getClient());
        }
    }

    DemandeNavire fromId(Long id) {
        if (id == null) {
            return null;
        }
        DemandeNavire demande = new DemandeNavire();
        demande.setId(id);
        return demande;
    }
}
