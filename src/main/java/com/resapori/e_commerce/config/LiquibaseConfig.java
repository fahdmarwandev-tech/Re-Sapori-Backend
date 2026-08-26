package com.resapori.e_commerce.config;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiquibaseConfig {

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("db/changelog/db.changelog-master.yaml");
        return liquibase;
    }

    /**
     * Permanently guarantees Liquibase runs before Hibernate schema validation.
     *
     * In Spring Boot 4.x the old LiquibaseDependsOnPostProcessor no longer reliably
     * enforces ordering, so we inject the dependency directly into the
     * entityManagerFactory bean definition before any beans are instantiated.
     * The @Bean method must be static so Spring can call it without first creating
     * the LiquibaseConfig instance (required for BeanDefinitionRegistryPostProcessor).
     */
    @Bean
    public static BeanDefinitionRegistryPostProcessor liquibaseBeforeJpa() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
                    throws BeansException {
                if (registry.containsBeanDefinition("entityManagerFactory")) {
                    BeanDefinition emf = registry.getBeanDefinition("entityManagerFactory");
                    emf.setDependsOn("liquibase");
                }
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
                    throws BeansException {
                // no-op
            }
        };
    }
}
