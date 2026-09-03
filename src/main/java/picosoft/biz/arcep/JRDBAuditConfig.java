package picosoft.biz.arcep;

import javax.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
    "picosoft.biz.arcep.configuration.audit"},
    entityManagerFactoryRef = "auditDSEmFactory", transactionManagerRef = "auditDSTransactionManager")

public class JRDBAuditConfig {


    @Bean
    @ConfigurationProperties("spring.datasource.audit")
    public DataSourceProperties auditDSProperties() {
        return new DataSourceProperties();
    }


    @Bean
    public LocalContainerEntityManagerFactoryBean auditDSEmFactory(@Qualifier("db") DataSource auditDS, EntityManagerFactoryBuilder builder) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.order_inserts", true);
        properties.put("hibernate.order_updates", true);
        return builder.dataSource(auditDS).packages(
                "picosoft.biz.arcep.configuration.audit"
        ).persistenceUnit("auditDS").properties(properties).build();
    }

    @Bean(name = "auditDSTransactionManager")
    public PlatformTransactionManager auditDSTransactionManager(@Qualifier("auditDSEmFactory") EntityManagerFactory auditDSEmFactory) {
        return new JpaTransactionManager(auditDSEmFactory);
    }

    @Bean(name = "jdbcDatasourceAuditService")
    public JdbcTemplate createJdbcTemplate_AuditService(@Qualifier("db") DataSource auditDS) {
        return new JdbcTemplate(auditDS);
    }

}







