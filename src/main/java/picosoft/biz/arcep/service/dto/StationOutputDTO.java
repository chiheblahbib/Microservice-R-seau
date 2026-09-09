package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.client.kernel.model.objects.AttachementInputDTO;
import picosoft.biz.arcep.domain.implantation.enumeration.*;
import picosoft.biz.arcep.domain.shared.enumeration.ApplicantType;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Etat renvoye au front, et objet transporte dans la variable BPMN 'data'.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StationOutputDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private UUID uuid;

    private String reference;

    private NatureImplantation natureImplantation;

    private TypeStation typeStation;

    private TypeTrafic typeTrafic;

    private NatureTrafic natureTrafic;

    private NatureSupport natureSupport;

    private Double hauteurSupport;

    private Double hauteurTotale;

    private TypeAntenne typeAntenne;

    private PositionnementAntenne positionnement;

    private ApplicantType installateurType;

    private String installateurRaisonSociale;

    private String installateurAdresse;

    private String installateurRegistreCommerce;

    private String installateurNatureActivite;

    private String installateurIdentite;

    private String installateurNationalite;

    private String installateurNationaliteComplet;

    private String installateurQualification;

    private String installateurTelephone;

    private String installateurEmail;

    private StatutStation statutStation;

    private Long demandeImplantationId;

    private SiteImplantationDTO siteImplantation;

    private List<FrequenceDTO> frequences;

    private List<AttestationDTO> attestations;

    private String idsPostAttachments;

    private String labelsPostAttachments;

    private String labelsMissingPostAttachments;

    private String wfProcessID;

    private Long classId;

    private String className;

    private String activityName;

    private String assignee;

    private String traitedBy;

    private String sidTraitedBy;

    private Boolean endProcess;

    private String state;

    private String signataire;

    private String signataireKcId;

    private Long numberOfattachments;

    private Long step;

}
