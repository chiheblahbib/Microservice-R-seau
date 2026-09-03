package picosoft.biz.arcep.client.kernel.model.acl.repository;


import picosoft.biz.arcep.client.kernel.model.acl.AclSid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.Optional;

@EnableJpaRepositories
public interface AclSidRepository extends JpaRepository<AclSid, Integer>, JpaSpecificationExecutor<AclSid> {


    Optional<AclSid> findBySid(String sid);
    List<AclSid> findAllBySidIn(List<String> sids);

}


