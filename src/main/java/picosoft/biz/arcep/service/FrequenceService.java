package picosoft.biz.arcep.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import picosoft.biz.arcep.controller.errors.BadRequestAlertException;
import picosoft.biz.arcep.controller.errors.StationErrors;
import picosoft.biz.arcep.domain.implantation.Frequence;
import picosoft.biz.arcep.domain.implantation.Station;
import picosoft.biz.arcep.repository.FrequenceRepository;
import picosoft.biz.arcep.service.criteria.FrequenceCriteria;
import picosoft.biz.arcep.service.dto.FrequenceDTO;
import picosoft.biz.arcep.service.mapper.FrequenceMapper;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FrequenceService {

    private final FrequenceRepository frequenceRepository;
    private final FrequenceQueryService frequenceQueryService;
    private final FrequenceMapper frequenceMapper;

    public FrequenceService(FrequenceRepository frequenceRepository, FrequenceQueryService frequenceQueryService,
                           FrequenceMapper frequenceMapper) {
        this.frequenceRepository = frequenceRepository;
        this.frequenceQueryService = frequenceQueryService;
        this.frequenceMapper = frequenceMapper;
    }

    public FrequenceDTO save(FrequenceDTO dto) {
        return frequenceMapper.toDto(frequenceRepository.save(frequenceMapper.toEntity(dto)));
    }

    public FrequenceDTO update(Long id, FrequenceDTO dto) {
        Frequence entity = frequenceRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(StationErrors.OBJECT_NOT_FOUND,
                        StationErrors.CLASS, StationErrors.OBJECT_NOT_FOUND));
        frequenceMapper.partialUpdate(entity, dto);
        return frequenceMapper.toDto(frequenceRepository.save(entity));
    }

    public Page<FrequenceDTO> findAll(FrequenceCriteria criteria, Pageable pageable, Integer size) {
        return frequenceQueryService.findByCriteria(criteria, pageable, size);
    }

    public Optional<FrequenceDTO> findOne(Long id) {
        return frequenceRepository.findById(id).map(frequenceMapper::toDto);
    }

    public List<FrequenceDTO> findByStation(Long stationId) {
        return frequenceMapper.toDto(frequenceRepository.findByStationId(stationId));
    }

    /**
     * La frequence n'a ni circuit ni autorisation en propre : elle est l'enfant
     * de la station, et c'est l'etat de celle-ci qui decide. Meme regle que
     * StationService.delete et que SiteImplantationService.delete.
     *
     * Une frequence sans station est une orpheline : rien ne l'engage, elle
     * s'efface.
     */
    public void delete(Long id) {
        Frequence entity = frequenceRepository.findById(id)
                .orElseThrow(() -> new BadRequestAlertException(StationErrors.OBJECT_NOT_FOUND,
                        StationErrors.CLASS, StationErrors.OBJECT_NOT_FOUND));

        Station station = entity.getStation();
        if (station != null
                && (station.getWfProcessID() != null
                    || (station.getAttestations() != null && !station.getAttestations().isEmpty()))) {
            throw new BadRequestAlertException(StationErrors.OBJECT_ENGAGED,
                    StationErrors.CLASS, StationErrors.OBJECT_ENGAGED);
        }
        frequenceRepository.delete(entity);
    }
}
