package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentaireDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String auteur;

    private String description;

    private ZonedDateTime dateSaisie;

    private Long classId;

    private Long objectID;

    private Boolean externe;
}
