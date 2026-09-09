package picosoft.biz.arcep.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import picosoft.biz.arcep.domain.numerocourt.DemandeNumeroCourt;
import picosoft.biz.arcep.service.dto.DemandeNumeroCourtInputDTO;

/** Mapper de {@link DemandeNumeroCourt} et de son DTO {@link DemandeNumeroCourtInputDTO}. */
@Mapper(componentModel = "spring",
        uses = {ClientMapper.class, ApplicantMapper.class, NumeroRattachementCourtMapper.class,
                RapportTechniqueMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandeNumeroCourtInputMapper implements EntityMapper<DemandeNumeroCourtInputDTO, DemandeNumeroCourt> {

    public abstract DemandeNumeroCourtInputDTO toDto(DemandeNumeroCourt demandeNumeroCourt);

    public abstract DemandeNumeroCourt toEntity(DemandeNumeroCourtInputDTO dto);

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
    public abstract void partialUpdate(@MappingTarget DemandeNumeroCourt entity, DemandeNumeroCourtInputDTO dto);

    @AfterMapping
    protected void fusionnerTitulaire(@MappingTarget DemandeNumeroCourt entity, DemandeNumeroCourtInputDTO dto) {
        if (dto.getClient() == null) {
            return;
        }
        if (entity.getClient() == null) {
            entity.setClient(clientMapper.toEntity(dto.getClient()));
        } else {
            clientMapper.partialUpdate(entity.getClient(), dto.getClient());
        }
    }

    DemandeNumeroCourt fromId(Long id) {
        if (id == null) {
            return null;
        }
        DemandeNumeroCourt demande = new DemandeNumeroCourt();
        demande.setId(id);
        return demande;
    }
}
