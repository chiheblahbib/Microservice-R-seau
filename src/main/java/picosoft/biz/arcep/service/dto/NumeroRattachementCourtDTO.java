package picosoft.biz.arcep.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.io.Serializable;

/** Un numero long ou fixe de rattachement, rubrique 4. */
@Getter
@Setter
@NoArgsConstructor
public class NumeroRattachementCourtDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** Chaine : un numero garde ses zeros de tete et ne se calcule pas. */
    @Size(max = 32)
    private String numero;

    private Long demandeNumeroCourtId;
}
