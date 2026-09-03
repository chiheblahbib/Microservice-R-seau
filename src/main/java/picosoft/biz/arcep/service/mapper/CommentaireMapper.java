package picosoft.biz.arcep.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picosoft.biz.arcep.domain.shared.Commentaire;
import picosoft.biz.arcep.service.dto.CommentaireDTO;

/**
 * Mapper for the entity {@link Commentaire} and its DTO {@link CommentaireDTO}.
 */
@Mapper(componentModel = "spring",  uses = {}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class CommentaireMapper implements EntityMapper<CommentaireDTO, Commentaire> {

    private final Logger log = LoggerFactory.getLogger(CommentaireMapper.class);

    public abstract CommentaireDTO toDto(Commentaire commentaire);

    public abstract Commentaire toEntity(CommentaireDTO commentaireDTO);


    Commentaire fromId(Long id) {
        if (id == null) {
            return null;
        }
        Commentaire commentaire = new Commentaire();
        commentaire.setId(id);
        return commentaire;
    }
}
