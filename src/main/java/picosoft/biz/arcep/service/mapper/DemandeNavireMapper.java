package picosoft.biz.arcep.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import picosoft.biz.arcep.domain.navire.DemandeNavire;
import picosoft.biz.arcep.service.dto.DemandeNavireDTO;

/** Mapper de {@link DemandeNavire} et de son DTO {@link DemandeNavireDTO}. */
@Mapper(componentModel = "spring",
        uses = {ClientMapper.class, PersonneNavireMapper.class, EquipementBordNavireMapper.class,
                AttestationMapper.class, RapportTechniqueMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandeNavireMapper implements EntityMapper<DemandeNavireDTO, DemandeNavire> {

    public abstract DemandeNavireDTO toDto(DemandeNavire demandeNavire);

    public abstract DemandeNavire toEntity(DemandeNavireDTO dto);

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
    public abstract void partialUpdate(@MappingTarget DemandeNavire entity, DemandeNavireDTO dto);

    @AfterMapping
    protected void fusionnerTitulaire(@MappingTarget DemandeNavire entity, DemandeNavireDTO dto) {
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
