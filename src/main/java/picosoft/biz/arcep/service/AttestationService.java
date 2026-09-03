package picosoft.biz.arcep.service;

import org.json.JSONObject;
import org.mapstruct.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import picosoft.biz.arcep.client.kernel.intercomm.KernelInterface;
import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import picosoft.biz.arcep.controller.errors.BadRequestAlertException;
import picosoft.biz.arcep.controller.errors.ReseauErrors;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.domain.shared.Attestation;
import picosoft.biz.arcep.repository.AttestationRepository;
import picosoft.biz.arcep.repository.DemandeReseauRepository;
import picosoft.biz.arcep.service.dto.AttestationDTO;
import picosoft.biz.arcep.service.dto.DemandeReseauOutputDTO;
import picosoft.biz.arcep.service.mapper.AttestationMapper;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Autorisation d'implantation.
 *
 * Ce bean est un point d'entree du diagramme BPMN : le circuit enfant l'appelle
 * par expression JUEL sur l'etape de signature. Il n'a donc pas d'appelant Java
 * -- renommer ou supprimer une de ses methodes publiques casse le process a
 * l'execution, pas a la compilation.
 *
 * L'expression attendue cote diagramme, gardee contre les boucles de retour :
 *   ${empty data.attestations
 *       ? attestationService.createAttestationForAutorisation(data)
 *       : execution.setVariable('isCreatedAtt', true)}
 */
@Service
@Transactional
@Named("attestationService")
public class AttestationService {

    private final Logger log = LoggerFactory.getLogger(AttestationService.class);

    private final AttestationRepository attestationRepository;
    private final AttestationMapper attestationMapper;
    private final DemandeReseauRepository demandeReseauRepository;
    private final KernelInterface kernelInterface;

    public AttestationService(AttestationRepository attestationRepository,
                              AttestationMapper attestationMapper,
                              DemandeReseauRepository demandeReseauRepository,
                              KernelInterface kernelInterface) {
        this.attestationRepository = attestationRepository;
        this.attestationMapper = attestationMapper;
        this.demandeReseauRepository = demandeReseauRepository;
        this.kernelInterface = kernelInterface;
    }

    /**
     * Cree l'autorisation d'exploitation du reseau.
     *
     * UNE DEMANDE, UNE AUTORISATION. Contrairement a la demande d'implantation,
     * qui eclate en une autorisation par station, le reseau est autorise d'un
     * bloc : le titre porte sur l'ensemble declare, sites et liaisons compris.
     *
     * Appele par le diagramme, jamais depuis le code Java.
     */
    @Transactional
    public AttestationDTO createAttestationForAutorisation(DemandeReseauOutputDTO demandeOutputDTO) {
        AclClass aclClass = kernelInterface.getaclClassByClassName(Attestation.class.getName());
        if (aclClass == null) {
            throw new BadRequestAlertException(ReseauErrors.ACL_CLASS_NOT_FOUND,
                    ReseauErrors.CLASS, ReseauErrors.ACL_CLASS_NOT_FOUND);
        }

        DemandeReseau demande = demandeReseauRepository.findById(demandeOutputDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("DemandeReseau not found"));

        Attestation attestation = new Attestation();
        attestation.setClassId(aclClass.getId());
        attestation.setDemandeReseau(demande);
        attestation.setNomModele(demande.getNatureReseau() != null
                ? demande.getNatureReseau().name() : null);
        attestation = attestationRepository.save(attestation);

        AttestationDTO pourSequence = attestationMapper.toDto(attestation);
        attestation.setReference(kernelInterface.getSequenceNumberByClass(
                new JSONObject(pourSequence).toString(), aclClass.getClasse()));
        attestation = attestationRepository.save(attestation);

        log.debug("autorisation {} creee pour le reseau {}", attestation.getReference(), demande.getId());

        return attestationMapper.toDto(attestation);
    }

    // ------------------------------------------------------------------ CRUD

    public AttestationDTO save(AttestationDTO dto) {
        return attestationMapper.toDto(attestationRepository.save(attestationMapper.toEntity(dto)));
    }

    public Optional<AttestationDTO> findOne(Long id) {
        return attestationRepository.findById(id).map(attestationMapper::toDto);
    }

    /** Les autorisations d'un dossier -- le reseau est autorise d'un bloc. */
    public List<AttestationDTO> findByDemande(Long demandeReseauId) {
        return attestationMapper.toDto(attestationRepository.findByDemandeReseauId(demandeReseauId));
    }

    public void delete(Long id) {
        attestationRepository.deleteById(id);
    }
}
