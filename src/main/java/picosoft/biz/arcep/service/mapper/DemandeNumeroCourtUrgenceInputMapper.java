package picosoft.biz.arcep.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import picosoft.biz.arcep.domain.numerocourturgence.DemandeNumeroCourtUrgence;
import picosoft.biz.arcep.service.dto.DemandeNumeroCourtUrgenceInputDTO;

/** Mapper de {@link DemandeNumeroCourtUrgence} et de son DTO {@link DemandeNumeroCourtUrgenceInputDTO}. */
@Mapper(componentModel = "spring",
        uses = {ClientMapper.class, ApplicantMapper.class, NumeroRattachementUrgenceMapper.class,
                RapportTechniqueMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandeNumeroCourtUrgenceInputMapper implements EntityMapper<DemandeNumeroCourtUrgenceInputDTO, DemandeNumeroCourtUrgence> {

    public abstract DemandeNumeroCourtUrgenceInputDTO toDto(DemandeNumeroCourtUrgence demandeNumeroCourtUrgence);

    public abstract DemandeNumeroCourtUrgence toEntity(DemandeNumeroCourtUrgenceInputDTO dto);

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
    public abstract void partialUpdate(@MappingTarget DemandeNumeroCourtUrgence entity, DemandeNumeroCourtUrgenceInputDTO dto);

    @AfterMapping
    protected void fusionnerTitulaire(@MappingTarget DemandeNumeroCourtUrgence entity, DemandeNumeroCourtUrgenceInputDTO dto) {
        if (dto.getClient() == null) {
            return;
        }
        if (entity.getClient() == null) {
            entity.setClient(clientMapper.toEntity(dto.getClient()));
        } else {
            clientMapper.partialUpdate(entity.getClient(), dto.getClient());
        }
    }

    DemandeNumeroCourtUrgence fromId(Long id) {
        if (id == null) {
            return null;
        }
        DemandeNumeroCourtUrgence demande = new DemandeNumeroCourtUrgence();
        demande.setId(id);
        return demande;
    }
}
