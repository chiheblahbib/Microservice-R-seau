package picosoft.biz.arcep.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import picosoft.biz.arcep.controller.errors.BadRequestAlertException;
import picosoft.biz.arcep.controller.errors.StationErrors;
import picosoft.biz.arcep.domain.implantation.SiteImplantation;
import picosoft.biz.arcep.domain.implantation.Station;
import picosoft.biz.arcep.repository.SiteImplantationRepository;
import picosoft.biz.arcep.service.criteria.SiteImplantationCriteria;
import picosoft.biz.arcep.service.dto.SiteImplantationDTO;
import picosoft.biz.arcep.service.mapper.SiteImplantationMapper;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SiteImplantationService {

    private final SiteImplantationRepository siteImplantationRepository;
    private final SiteImplantationQueryService siteImplantationQueryService;
    private final SiteImplantationMapper siteImplantationMapper;

    public SiteImplantationService(SiteImplantationRepository siteImplantationRepository, SiteImplantationQueryService siteImplantationQueryService,
                           SiteImplantationMapper siteImplantationMapper) {
        this.siteImplantationRepository = siteImplantationRepository;
        this.siteImplantationQueryService = siteImplantationQueryService;
        this.siteImplantationMapper = siteImplantationMapper;
    }

    public SiteImplantationDTO save(SiteImplantationDTO dto) {
        return siteImplantationMapper.toDto(siteImplantationRepository.save(siteImplantationMapper.toEntity(dto)));
    }

    public SiteImplantationDTO update(Long id, SiteImplantationDTO dto) {
        SiteImplantation entity = siteImplantationRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(StationErrors.OBJECT_NOT_FOUND,
                        StationErrors.CLASS, StationErrors.OBJECT_NOT_FOUND));
        siteImplantationMapper.partialUpdate(entity, dto);
        return siteImplantationMapper.toDto(siteImplantationRepository.save(entity));
    }

    public Page<SiteImplantationDTO> findAll(SiteImplantationCriteria criteria, Pageable pageable, Integer size) {
        return siteImplantationQueryService.findByCriteria(criteria, pageable, size);
    }

    public Optional<SiteImplantationDTO> findOne(Long id) {
        return siteImplantationRepository.findById(id).map(siteImplantationMapper::toDto);
    }

    /**
     * Le site n'a ni circuit ni autorisation en propre : il est l'enfant OneToOne
     * de la station, et c'est l'etat de celle-ci qui decide. Meme regle que
     * StationService.delete, lue un cran plus haut.
     *
     * Un site sans station est un orphelin : rien ne l'engage, il s'efface.
     */
    public void delete(Long id) {
        SiteImplantation entity = siteImplantationRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(StationErrors.OBJECT_NOT_FOUND,
                        StationErrors.CLASS, StationErrors.OBJECT_NOT_FOUND));

        Station station = entity.getStation();
        if (station != null
                && (station.getWfProcessID() != null
                    || (station.getAttestations() != null && !station.getAttestations().isEmpty()))) {
            throw new BadRequestAlertException(StationErrors.OBJECT_ENGAGED,
                    StationErrors.CLASS, StationErrors.OBJECT_ENGAGED);
        }
        siteImplantationRepository.delete(entity);
    }
}
