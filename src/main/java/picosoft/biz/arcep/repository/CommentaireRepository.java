package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.shared.Commentaire;

import java.util.List;

@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, Long>, JpaSpecificationExecutor<Commentaire> {

    List<Commentaire> findByClassIdAndObjectID(Long classId, Long objectID);
}
