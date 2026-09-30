package picosoft.biz.arcep.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Un numero long ou fixe de rattachement, rubrique 4. */
@Getter
@Setter
@NoArgsConstructor
public class NumeroRattachementUrgenceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String numero;

    private Long demandeNumeroCourtUrgenceId;
}
