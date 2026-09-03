package picosoft.biz.arcep.client.kernel.model.acl.repository;


import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface AclClassRepository extends JpaRepository<AclClass, Long>, JpaSpecificationExecutor<AclClass> {
        Optional<AclClass> findByClasse(String classe);

        Optional<AclClass> findBySimpleName(String simpleName);

        @Query("select a.label from AclClass a where a.simpleName=:simpleName")
        String findLabelBySimpleName(@Param("simpleName") String simpleName);

        @Query(value = "SELECT c.* FROM public.acl_class c  join kernel.k_pa_module m on  c.module_id =m.id\n" +
                "where m.package_name= ?1 and c.include_new_jobs = true", nativeQuery = true)
        List<AclClass> findAllByModule(String packageName);

}
