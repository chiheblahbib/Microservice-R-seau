package picosoft.biz.arcep.Workflow.config;

import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "org.flowable.engine.repository", entityManagerFactoryRef = "internDSEmFactory", transactionManagerRef = "internDSTransactionManager")
public class FlowableDBConfig {

    @Autowired
    @Qualifier("processEngine")
    private ProcessEngine processEngine;

    @Bean
    public LocalContainerEntityManagerFactoryBean internDSEmFactory(@Qualifier("flowabledb") DataSource internDS, EntityManagerFactoryBuilder builder) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.order_inserts", true);
        properties.put("hibernate.order_updates", true);
        properties.put("hibernate.default_schema", "public");

        return builder
                .dataSource(internDS)
                .packages("org.flowable.engine")
                .persistenceUnit("internDS")
                .properties(properties)
                .build();
    }

    @Bean(name = "internDSTransactionManager")
    public PlatformTransactionManager internDSTransactionManager(@Qualifier("internDSEmFactory") EntityManagerFactory internDSEmFactory) {
        return new JpaTransactionManager(internDSEmFactory);
    }

    @Bean(name = "jdbcDatasourceInternService")
    @Autowired
    public JdbcTemplate createJdbcTemplate_ProfileService(@Qualifier("flowabledb") DataSource internDS) {
        return new JdbcTemplate(internDS);
    }

    @Bean
    public ProcessEngineConfigurationImpl processEngineConfiguration() {
        ProcessEngineConfigurationImpl processEngineConfiguration =
                (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();

        // Custom configurations that are not available as properties
        processEngineConfiguration.setEnableDatabaseEventLogging(true);
        processEngineConfiguration.setEnableHistoricTaskLogging(true);
        processEngineConfiguration.setEnableLogSqlExecutionTime(true);
        processEngineConfiguration.setEnableVerboseExecutionTreeLogging(true);

        // Build the process engine if not already built
        processEngineConfiguration.buildProcessEngine();

        return processEngineConfiguration;
    }

}
