package picosoft.biz.arcep.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picosoft.biz.arcep.domain.implantation.DemandeImplantation;
import picosoft.biz.arcep.service.dto.DemandeImplantationInputDTO;

/**
 * Mapper for the entity {@link DemandeImplantation} and its DTO {@link DemandeImplantationInputDTO}.
 */
@Mapper(componentModel = "spring",  uses = {ClientMapper.class, ApplicantMapper.class, StationMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandeImplantationInputMapper implements EntityMapper<DemandeImplantationInputDTO, DemandeImplantation> {

    private final Logger log = LoggerFactory.getLogger(DemandeImplantationInputMapper.class);

    public abstract DemandeImplantationInputDTO toDto(DemandeImplantation demandeImplantation);

    public abstract DemandeImplantation toEntity(DemandeImplantationInputDTO dto);


    @Autowired
    protected ClientMapper clientMapper;

    @Autowired
    protected ApplicantMapper applicantMapper;

    @Autowired
    protected StationMapper stationMapper;

    /**
     * Les relations un-a-un sont retirees du partialUpdate genere : MapStruct les
     * remplacerait par une instance neuve, ce qui viole la contrainte unique portee
     * par la cle etrangere de l'enfant. On les fusionne ici, en place.
     */
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "station", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void partialUpdate(@MappingTarget DemandeImplantation entity, DemandeImplantationInputDTO dto);

    @AfterMapping
    protected void fusionnerEnfantsUnAUn(@MappingTarget DemandeImplantation entity, DemandeImplantationInputDTO dto) {
        if (dto.getClient() != null) {
            if (entity.getClient() == null) {
                entity.setClient(clientMapper.toEntity(dto.getClient()));
            } else {
                clientMapper.partialUpdate(entity.getClient(), dto.getClient());
            }
        }
        if (dto.getStation() != null) {
            if (entity.getStation() == null) {
                entity.setStation(stationMapper.toEntity(dto.getStation()));
            } else {
                stationMapper.partialUpdate(entity.getStation(), dto.getStation());
            }
        }
    }

    DemandeImplantation fromId(Long id) {
        if (id == null) {
            return null;
        }
        DemandeImplantation demandeImplantation = new DemandeImplantation();
        demandeImplantation.setId(id);
        return demandeImplantation;
    }
}
