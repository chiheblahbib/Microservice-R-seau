package picosoft.biz.arcep.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import picosoft.biz.arcep.domain.ispc.PersonneIspc;

/** Depot de PersonneIspc. */
@Repository
public interface PersonneIspcRepository extends JpaRepository<PersonneIspc, Long>,
        JpaSpecificationExecutor<PersonneIspc> {
}
