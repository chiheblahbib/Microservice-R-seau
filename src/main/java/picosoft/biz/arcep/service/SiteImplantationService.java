package picosoft.biz.arcep.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import picosoft.biz.arcep.controller.errors.BadRequestAlertException;
import picosoft.biz.arcep.controller.errors.StationErrors;
import picosoft.biz.arcep.domain.implantation.SiteImplantation;
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

    public void delete(Long id) {
        siteImplantationRepository.deleteById(id);
    }
}
