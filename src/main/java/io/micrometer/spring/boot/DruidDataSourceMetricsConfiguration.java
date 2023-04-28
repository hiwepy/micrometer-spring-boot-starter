package io.micrometer.spring.boot;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceUnwrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.log.LogMessage;

import javax.sql.DataSource;
import java.util.Collection;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter({ MetricsAutoConfiguration.class, DataSourceAutoConfiguration.class,
        SimpleMetricsExportAutoConfiguration.class })
@ConditionalOnClass({ DataSource.class, MeterRegistry.class })
@ConditionalOnBean({ DataSource.class, MeterRegistry.class })
public class DruidDataSourceMetricsConfiguration {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    private final MeterRegistry registry;

    HikariDataSourceMetricsConfiguration(MeterRegistry registry) {
        this.registry = registry;
    }

    @Autowired
    void bindMetricsRegistryToHikariDataSources(Collection<DataSource> dataSources) {
        for (DataSource dataSource : dataSources) {
            HikariDataSource hikariDataSource = DataSourceUnwrapper.unwrap(dataSource, HikariDataSource.class);
            if (hikariDataSource != null) {
                bindMetricsRegistryToHikariDataSource(hikariDataSource);
            }
        }
    }

    private void bindMetricsRegistryToHikariDataSource(DruidDataSource hikari) {
        if (hikari.getMetricRegistry() == null && hikari.getMetricsTrackerFactory() == null) {
            try {
                hikari.setMetricsTrackerFactory(new MicrometerMetricsTrackerFactory(this.registry));
            }
            catch (Exception ex) {
                logger.warn(LogMessage.format("Failed to bind Hikari metrics: %s", ex.getMessage()));
            }
        }
    }

    @Bean
    public DruidDataSourceMetrics druidDataSourceMetrics(DataSource dataSource, MeterRegistry meterRegistry) {
        return new DruidDataSourceMetrics(dataSource, meterRegistry);
    }

}