package picosoft.biz.arcep;

import javax.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
    "picosoft.biz.arcep.repository",
        "picosoft.biz.arcep.client.kernel.model.acl.repository"},
    entityManagerFactoryRef = "arcepDSEmFactory", transactionManagerRef = "arcepDSTransactionManager")

public class JRDBArcepConfig {


    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.arcep")
    public DataSourceProperties arcepDSProperties() {
        return new DataSourceProperties();
    }


    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean arcepDSEmFactory(@Qualifier("db") DataSource arcepDS, EntityManagerFactoryBuilder builder) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.order_inserts", true);
        properties.put("hibernate.order_updates", true);
        return builder.dataSource(arcepDS).packages(
            "picosoft.biz.arcep.domain",
                "picosoft.biz.arcep.client.kernel.model.acl"
        ).persistenceUnit("arcepDS").properties(properties).build();
    }

    @Primary
    @Bean(name = "arcepDSTransactionManager")
    public PlatformTransactionManager arcepDSTransactionManager(@Qualifier("arcepDSEmFactory") EntityManagerFactory arcepDSEmFactory) {
        return new JpaTransactionManager(arcepDSEmFactory);
    }

    @Primary
    @Bean(name = "jdbcDatasourceArcepService")
    public JdbcTemplate createJdbcTemplate_ArcepService(@Qualifier("db") DataSource arcepDS) {
        return new JdbcTemplate(arcepDS);
    }

}







