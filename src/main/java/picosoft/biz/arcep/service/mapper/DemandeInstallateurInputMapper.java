package picosoft.biz.arcep.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import picosoft.biz.arcep.domain.installateur.DemandeInstallateur;
import picosoft.biz.arcep.service.dto.DemandeInstallateurInputDTO;

/** Mapper de {@link DemandeInstallateur} et de son DTO {@link DemandeInstallateurInputDTO}. */
@Mapper(componentModel = "spring",
        uses = {ClientMapper.class, ApplicantMapper.class, QualiteDemandeeMapper.class, TechnicienSpecialisteMapper.class,
                OutillageDeclareMapper.class,
                RapportTechniqueMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class DemandeInstallateurInputMapper implements EntityMapper<DemandeInstallateurInputDTO, DemandeInstallateur> {

    public abstract DemandeInstallateurInputDTO toDto(DemandeInstallateur demandeInstallateur);

    public abstract DemandeInstallateur toEntity(DemandeInstallateurInputDTO dto);

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
    public abstract void partialUpdate(@MappingTarget DemandeInstallateur entity, DemandeInstallateurInputDTO dto);

    @AfterMapping
    protected void fusionnerTitulaire(@MappingTarget DemandeInstallateur entity, DemandeInstallateurInputDTO dto) {
        if (dto.getClient() == null) {
            return;
        }
        if (entity.getClient() == null) {
            entity.setClient(clientMapper.toEntity(dto.getClient()));
        } else {
            clientMapper.partialUpdate(entity.getClient(), dto.getClient());
        }
    }

    DemandeInstallateur fromId(Long id) {
        if (id == null) {
            return null;
        }
        DemandeInstallateur demande = new DemandeInstallateur();
        demande.setId(id);
        return demande;
    }
}
