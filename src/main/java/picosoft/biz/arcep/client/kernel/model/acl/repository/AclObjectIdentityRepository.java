package picosoft.biz.arcep.client.kernel.model.acl.repository;

import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import picosoft.biz.arcep.client.kernel.model.acl.AclObjectIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AclObjectIdentityRepository extends JpaRepository<AclObjectIdentity, Integer>, JpaSpecificationExecutor<AclObjectIdentity> {

    AclObjectIdentity findByObjectIdentityAndObjectIdClass(String objectIdentity, AclClass objectIdClass);

}
