package picosoft.biz.arcep.service.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.shared.enumeration.MomentTarif;

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


    private String code;


    private String libelle;


    private String service;


    private String application;

    private BigDecimal montant;


    private String devise;

    private MomentTarif moment;

    private LocalDate dateEffet;

    private LocalDate dateFin;

    private Boolean actif;


    private String commentaire;
}
