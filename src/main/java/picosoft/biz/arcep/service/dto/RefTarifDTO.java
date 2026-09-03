package picosoft.biz.arcep.service.dto;

import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.reseau.enumeration.MomentTarif;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefTarifDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @Size(max = 64)

    private String code;

    @Size(max = 255)

    private String libelle;

    @Size(max = 128)

    private String service;

    @Size(max = 255)

    private String application;

    private BigDecimal montant;

    @Size(max = 8)

    private String devise;

    private MomentTarif moment;

    private LocalDate dateEffet;

    private LocalDate dateFin;

    private Boolean actif;

    @Size(max = 500)

    private String commentaire;
}
