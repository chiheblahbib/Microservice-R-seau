package picosoft.biz.arcep.service.dto;

import javax.validation.constraints.Size;
import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.implantation.enumeration.*;
import picosoft.biz.arcep.domain.shared.enumeration.ApplicantType;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private UUID uuid;

    @Size(max = 25)

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

    // L'installateur reprend, champ pour champ, l'identite du demandeur :
    // une societe ou un particulier. La telecopie a ete retiree.

    private ApplicantType installateurType;

    @Size(max = 100)
    private String installateurRaisonSociale;

    @Size(max = 200)
    private String installateurAdresse;

    @Size(max = 50)
    private String installateurRegistreCommerce;

    @Size(max = 500)
    private String installateurNatureActivite;

    @Size(max = 100)
    private String installateurIdentite;

    @Size(max = 50)
    private String installateurNationalite;

    @Size(max = 100)
    private String installateurNationaliteComplet;

    @Size(max = 100)
    private String installateurQualification;

    @Size(max = 20)
    private String installateurTelephone;

    @Size(max = 100)
    private String installateurEmail;

    private StatutStation statutStation;

    private Long demandeImplantationId;

    @Valid
    private SiteImplantationDTO siteImplantation;

    @Valid
    private java.util.List<FrequenceDTO> frequences;

    private java.util.List<AttestationDTO> attestations;

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

    @Size(max = 64)

    private String state;

    private String signataire;

    private String signataireKcId;

    private Long numberOfattachments;

    private Long step;

    @Size(max = 500)

    private String commentaire;
}
