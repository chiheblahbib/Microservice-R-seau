package picosoft.biz.arcep.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;


@Configuration
public class BeanConfiguration {

    @Autowired
    private Environment environment;

    @Primary
    @Bean(name = "db")
    public DataSource dataSource() {
            DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.driverClassName(environment.getProperty("spring.datasource.arcep.driver-class-name"));
            dataSourceBuilder.url(environment.getProperty("spring.datasource.arcep.url"));
            dataSourceBuilder.username(environment.getProperty("spring.datasource.arcep.username"));
            dataSourceBuilder.password(environment.getProperty("spring.datasource.arcep.password"));
            return dataSourceBuilder.build();
    }

    @Bean(name = "flowabledb")
    public DataSource flowabledataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(environment.getProperty("spring.datasource.flowable.driver-class-name"));
        dataSourceBuilder.url(environment.getProperty("spring.datasource.flowable.url"));
        dataSourceBuilder.username(environment.getProperty("spring.datasource.flowable.username"));
        dataSourceBuilder.password(environment.getProperty("spring.datasource.flowable.password"));
        return dataSourceBuilder.build();
    }

    @Bean(name = "auditdb")
    public DataSource auditdataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(environment.getProperty("spring.datasource.audit.driver-class-name"));
        dataSourceBuilder.url(environment.getProperty("spring.datasource.audit.url"));
        dataSourceBuilder.username(environment.getProperty("spring.datasource.audit.username"));
        dataSourceBuilder.password(environment.getProperty("spring.datasource.audit.password"));
        return dataSourceBuilder.build();
    }

    @Bean(name = "jdbcTemplate")
    public JdbcTemplate jdbcTemplate(@Qualifier("db") DataSource ds) {
        return new JdbcTemplate(ds);
    }

}
