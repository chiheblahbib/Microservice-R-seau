package picosoft.biz.arcep.service;

import org.json.JSONObject;
import org.mapstruct.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import picosoft.biz.arcep.client.kernel.intercomm.KernelInterface;
import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import picosoft.biz.arcep.controller.errors.*;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.domain.shared.Attestation;
import picosoft.biz.arcep.repository.*;
import picosoft.biz.arcep.service.dto.*;
import picosoft.biz.arcep.service.mapper.AttestationMapper;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * La delivrance du titre : autorisation, agrement, attribution.
 *
 * Ce bean est un POINT D'ENTREE DU DIAGRAMME. Les circuits l'appellent par
 * expression JUEL sur la passerelle du signataire, gardee contre les boucles
 * de retour :
 *   ${empty data.attestations
 *       ? attestationService.createAttestationForXxx(data)
 *       : execution.setVariable('isCreatedAtt', true)}
 *
 * Aucune de ces methodes n'a d'appelant Java : en renommer une casse le
 * process a l'execution, pas a la compilation.
 *
 *
 * UNE METHODE PAR FORMULAIRE, ET NON DES SURCHARGES
 *
 * Corrige le 10 septembre 2026. Il n'y avait qu'une methode,
 * createAttestationForAutorisation(DemandeReseauOutputDTO), et les DIX
 * circuits transposes du reseau l'appelaient avec LEUR propre DTO. La
 * conversion echouait a la passerelle « Quel signataire ? » -- et si elle
 * etait passee, la methode aurait cherche un DemandeReseau portant
 * l'identifiant d'un dossier MMSI.
 *
 * Des surcharges auraient ete pires : JUEL choisit une methode par son NOM et
 * son NOMBRE D'ARGUMENTS, jamais par le type de l'argument. Douze surcharges a
 * un argument sont douze candidates indiscernables. D'ou douze noms distincts
 * -- la forme qu'homologation emploie deja (createAttestationForAsi,
 * createAttestationForHomologation).
 *
 *
 * nomModele N'EST RENSEIGNE QUE POUR LE RESEAU
 *
 * Il y porte la nature du reseau autorise. Les onze autres formulaires n'ont
 * pas d'equivalent : aucun ne decrit un « modele » de ce qui est autorise. Le
 * champ reste nul plutot que de recevoir une valeur inventee.
 */
@Service
@Transactional
@Named("attestationService")
public class AttestationService {

    private final Logger log = LoggerFactory.getLogger(AttestationService.class);

    private final AttestationRepository attestationRepository;
    private final AttestationMapper attestationMapper;
    private final KernelInterface kernelInterface;

    private final DemandeReseauRepository demandeReseauRepository;
    private final StationRepository stationRepository;
    private final DemandeInstallateurRepository demandeInstallateurRepository;
    private final DemandeAeronefRepository demandeAeronefRepository;
    private final DemandeNavireRepository demandeNavireRepository;
    private final DemandeDeclaratifRepository demandeDeclaratifRepository;
    private final DemandeIspcRepository demandeIspcRepository;
    private final DemandePqRepository demandePqRepository;
    private final DemandeNumeroCourtRepository demandeNumeroCourtRepository;
    private final DemandeNumeroCourtUrgenceRepository demandeNumeroCourtUrgenceRepository;
    private final DemandeMmsiRepository demandeMmsiRepository;
    private final DemandeUssdRepository demandeUssdRepository;

    public AttestationService(AttestationRepository attestationRepository,
                              AttestationMapper attestationMapper,
                              KernelInterface kernelInterface,
                              DemandeReseauRepository demandeReseauRepository,
                              StationRepository stationRepository,
                              DemandeInstallateurRepository demandeInstallateurRepository,
                              DemandeAeronefRepository demandeAeronefRepository,
                              DemandeNavireRepository demandeNavireRepository,
                              DemandeDeclaratifRepository demandeDeclaratifRepository,
                              DemandeIspcRepository demandeIspcRepository,
                              DemandePqRepository demandePqRepository,
                              DemandeNumeroCourtRepository demandeNumeroCourtRepository,
                              DemandeNumeroCourtUrgenceRepository demandeNumeroCourtUrgenceRepository,
                              DemandeMmsiRepository demandeMmsiRepository,
                              DemandeUssdRepository demandeUssdRepository) {
        this.attestationRepository = attestationRepository;
        this.attestationMapper = attestationMapper;
        this.kernelInterface = kernelInterface;
        this.demandeReseauRepository = demandeReseauRepository;
        this.stationRepository = stationRepository;
        this.demandeInstallateurRepository = demandeInstallateurRepository;
        this.demandeAeronefRepository = demandeAeronefRepository;
        this.demandeNavireRepository = demandeNavireRepository;
        this.demandeDeclaratifRepository = demandeDeclaratifRepository;
        this.demandeIspcRepository = demandeIspcRepository;
        this.demandePqRepository = demandePqRepository;
        this.demandeNumeroCourtRepository = demandeNumeroCourtRepository;
        this.demandeNumeroCourtUrgenceRepository = demandeNumeroCourtUrgenceRepository;
        this.demandeMmsiRepository = demandeMmsiRepository;
        this.demandeUssdRepository = demandeUssdRepository;
    }

    // ------------------------------------------ infrastructures et reseau

    /**
     * L'autorisation d'exploitation du reseau.
     *
     * UNE DEMANDE, UNE AUTORISATION. Contrairement a la demande
     * d'implantation, qui eclate en une autorisation par station, le reseau
     * est autorise d'un bloc : le titre porte sur l'ensemble declare, sites et
     * liaisons compris.
     */
    public AttestationDTO createAttestationForAutorisation(DemandeReseauOutputDTO dto) {
        AclClass aclClass = classeAttestation(ReseauErrors.CLASS, ReseauErrors.ACL_CLASS_NOT_FOUND);
        DemandeReseau demande = demandeReseauRepository.findById(dto.getId())
                .orElseThrow(() -> introuvable("DemandeReseau", dto.getId()));
        Attestation attestation = new Attestation();
        attestation.setDemandeReseau(demande);
        attestation.setNomModele(demande.getNatureReseau() != null
                ? demande.getNatureReseau().name() : null);
        return delivrer(attestation, aclClass, "le reseau", dto.getId());
    }

    /**
     * Le titre d'implantation d'une station.
     *
     * Delivre par le circuit ENFANT, jamais par celui du dossier : le titre
     * porte sur une station, et se reemet -- renouvellement, controle annuel
     * -- sans rouvrir le dossier d'origine.
     */
    public AttestationDTO createAttestationForStation(StationOutputDTO dto) {
        AclClass aclClass = classeAttestation(StationErrors.CLASS, StationErrors.ACL_CLASS_NOT_FOUND);
        Attestation attestation = new Attestation();
        attestation.setStation(stationRepository.findById(dto.getId())
                .orElseThrow(() -> introuvable("Station", dto.getId())));
        return delivrer(attestation, aclClass, "la station", dto.getId());
    }

    public AttestationDTO createAttestationForInstallateur(DemandeInstallateurOutputDTO dto) {
        AclClass aclClass = classeAttestation(InstallateurErrors.CLASS, InstallateurErrors.ACL_CLASS_NOT_FOUND);
        Attestation attestation = new Attestation();
        attestation.setDemandeInstallateur(demandeInstallateurRepository.findById(dto.getId())
                .orElseThrow(() -> introuvable("DemandeInstallateur", dto.getId())));
        return delivrer(attestation, aclClass, "l'installateur", dto.getId());
    }

    // ----------------------------------------------------------- frequences

    public AttestationDTO createAttestationForAeronef(DemandeAeronefOutputDTO dto) {
        AclClass aclClass = classeAttestation(AeronefErrors.CLASS, AeronefErrors.ACL_CLASS_NOT_FOUND);
        Attestation attestation = new Attestation();
        attestation.setDemandeAeronef(demandeAeronefRepository.findById(dto.getId())
                .orElseThrow(() -> introuvable("DemandeAeronef", dto.getId())));
        return delivrer(attestation, aclClass, "l'aeronef", dto.getId());
    }

    public AttestationDTO createAttestationForNavire(DemandeNavireOutputDTO dto) {
        AclClass aclClass = classeAttestation(NavireErrors.CLASS, NavireErrors.ACL_CLASS_NOT_FOUND);
        Attestation attestation = new Attestation();
        attestation.setDemandeNavire(demandeNavireRepository.findById(dto.getId())
                .orElseThrow(() -> introuvable("DemandeNavire", dto.getId())));
        return delivrer(attestation, aclClass, "le navire", dto.getId());
    }

    public AttestationDTO createAttestationForDeclaratif(DemandeDeclaratifOutputDTO dto) {
        AclClass aclClass = classeAttestation(DeclaratifErrors.CLASS, DeclaratifErrors.ACL_CLASS_NOT_FOUND);
        Attestation attestation = new Attestation();
        attestation.setDemandeDeclaratif(demandeDeclaratifRepository.findById(dto.getId())
                .orElseThrow(() -> introuvable("DemandeDeclaratif", dto.getId())));
        return delivrer(attestation, aclClass, "le declaratif", dto.getId());
    }

    // --------------------------------------------------------- numerotation

    public AttestationDTO createAttestationForIspc(DemandeIspcOutputDTO dto) {
        AclClass aclClass = classeAttestation(IspcErrors.CLASS, IspcErrors.ACL_CLASS_NOT_FOUND);
        Attestation attestation = new Attestation();
        attestation.setDemandeIspc(demandeIspcRepository.findById(dto.getId())
                .orElseThrow(() -> introuvable("DemandeIspc", dto.getId())));
        return delivrer(attestation, aclClass, "le code ISPC", dto.getId());
    }

    public AttestationDTO createAttestationForPq(DemandePqOutputDTO dto) {
        AclClass aclClass = classeAttestation(PqErrors.CLASS, PqErrors.ACL_CLASS_NOT_FOUND);
        Attestation attestation = new Attestation();
        attestation.setDemandePq(demandePqRepository.findById(dto.getId())
                .orElseThrow(() -> introuvable("DemandePq", dto.getId())));
        return delivrer(attestation, aclClass, "le bloc de numeros", dto.getId());
    }

    public AttestationDTO createAttestationForNumeroCourt(DemandeNumeroCourtOutputDTO dto) {
        AclClass aclClass = classeAttestation(NumeroCourtErrors.CLASS, NumeroCourtErrors.ACL_CLASS_NOT_FOUND);
        Attestation attestation = new Attestation();
        attestation.setDemandeNumeroCourt(demandeNumeroCourtRepository.findById(dto.getId())
                .orElseThrow(() -> introuvable("DemandeNumeroCourt", dto.getId())));
        return delivrer(attestation, aclClass, "le numero court", dto.getId());
    }

    public AttestationDTO createAttestationForNumeroCourtUrgence(DemandeNumeroCourtUrgenceOutputDTO dto) {
        AclClass aclClass = classeAttestation(NumeroCourtUrgenceErrors.CLASS,
                NumeroCourtUrgenceErrors.ACL_CLASS_NOT_FOUND);
        Attestation attestation = new Attestation();
        attestation.setDemandeNumeroCourtUrgence(demandeNumeroCourtUrgenceRepository.findById(dto.getId())
                .orElseThrow(() -> introuvable("DemandeNumeroCourtUrgence", dto.getId())));
        return delivrer(attestation, aclClass, "le numero court d'urgence", dto.getId());
    }

    public AttestationDTO createAttestationForMmsi(DemandeMmsiOutputDTO dto) {
        AclClass aclClass = classeAttestation(MmsiErrors.CLASS, MmsiErrors.ACL_CLASS_NOT_FOUND);
        Attestation attestation = new Attestation();
        attestation.setDemandeMmsi(demandeMmsiRepository.findById(dto.getId())
                .orElseThrow(() -> introuvable("DemandeMmsi", dto.getId())));
        return delivrer(attestation, aclClass, "le MMSI", dto.getId());
    }

    public AttestationDTO createAttestationForUssd(DemandeUssdOutputDTO dto) {
        AclClass aclClass = classeAttestation(UssdErrors.CLASS, UssdErrors.ACL_CLASS_NOT_FOUND);
        Attestation attestation = new Attestation();
        attestation.setDemandeUssd(demandeUssdRepository.findById(dto.getId())
                .orElseThrow(() -> introuvable("DemandeUssd", dto.getId())));
        return delivrer(attestation, aclClass, "le code USSD", dto.getId());
    }

    // ------------------------------------------------------------- internes

    /**
     * La classe ACL de l'attestation, resolue au kernel.
     *
     * Elle est PARTAGEE AVEC HOMOLOGATION -- meme nom qualifie, meme table
     * homologation.attestation -- donc une seule ligne acl_class pour les deux
     * services, et une seule sequence : les references des deux services
     * s'entrelacent.
     */
    private AclClass classeAttestation(String classeErreur, String cleErreur) {
        AclClass aclClass = kernelInterface.getaclClassByClassName(Attestation.class.getName());
        if (aclClass == null) {
            throw new BadRequestAlertException(cleErreur, classeErreur, cleErreur);
        }
        return aclClass;
    }

    /**
     * Enregistre le titre, puis lui donne sa reference.
     *
     * En deux ecritures, et non une : la reference est calculee par le kernel
     * A PARTIR du titre serialise, qui doit donc deja porter son identifiant.
     */
    private AttestationDTO delivrer(Attestation attestation, AclClass aclClass,
                                    String quoi, Long idDossier) {
        attestation.setClassId(aclClass.getId());
        attestation = attestationRepository.save(attestation);

        AttestationDTO pourSequence = attestationMapper.toDto(attestation);
        attestation.setReference(kernelInterface.getSequenceNumberByClass(
                new JSONObject(pourSequence).toString(), aclClass.getClasse()));
        attestation = attestationRepository.save(attestation);

        log.debug("autorisation {} delivree pour {} {}", attestation.getReference(), quoi, idDossier);
        return attestationMapper.toDto(attestation);
    }

    private EntityNotFoundException introuvable(String entite, Long id) {
        return new EntityNotFoundException(entite + " " + id + " introuvable");
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
