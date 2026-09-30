package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

/** Une ligne du tableau des controles, rubrique 7 -- remplie par l'ARCEP. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationControleDTO implements Serializable {

    private Long id;

    private LocalDate dateControle;

    private String lieu;

    private String visa;

    private String observations;

    private Long demandeAeronefId;
}
