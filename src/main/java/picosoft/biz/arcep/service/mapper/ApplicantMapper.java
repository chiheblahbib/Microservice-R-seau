package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picosoft.biz.arcep.domain.shared.Applicant;
import picosoft.biz.arcep.service.dto.ApplicantDTO;

/**
 * Mapper for the entity {@link Applicant} and its DTO {@link ApplicantDTO}.
 */
@Mapper(componentModel = "spring",  uses = {}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ApplicantMapper implements EntityMapper<ApplicantDTO, Applicant> {

    private final Logger log = LoggerFactory.getLogger(ApplicantMapper.class);

    public abstract ApplicantDTO toDto(Applicant applicant);

    public abstract Applicant toEntity(ApplicantDTO applicantDTO);


    Applicant fromId(Long id) {
        if (id == null) {
            return null;
        }
        Applicant applicant = new Applicant();
        applicant.setId(id);
        return applicant;
    }
}
