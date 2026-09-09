package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
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

    @Size(max = 150)
    private String lieu;

    @Size(max = 100)
    private String visa;

    @Size(max = 500)
    private String observations;

    private Long demandeAeronefId;
}
