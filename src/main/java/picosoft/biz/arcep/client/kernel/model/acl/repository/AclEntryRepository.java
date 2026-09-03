package picosoft.biz.arcep.client.kernel.model.acl.repository;



import picosoft.biz.arcep.client.kernel.model.acl.AclEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AclEntryRepository extends JpaRepository<AclEntry, Integer>, JpaSpecificationExecutor<AclEntry> {

    @Query("select a from AclEntry a where a.aclObjectIdentity.id = :aclObjectId and a.sid.sid = :sid")
    List<AclEntry> findByAclObjectIdentityAndSid(@Param("aclObjectId") Long aclObjectId, @Param("sid") String sid);

}
