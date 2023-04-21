package io.micrometer.spring.boot;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.spring.boot.binder.MeterBinderHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.List;

@Slf4j
public class MicrometerMetricBeanPostProcessor implements BeanPostProcessor {

    MeterRegistry registry;
    List<MeterBinderHandler> handlers;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if (bean instanceof MetricSet) {
            MetricSet metricSet = (MetricSet) bean;
            if (!canRegister(beanName)) {
                return bean;
            }
            String metricName;
            if (isJvmCollector(beanName)) {
                metricName = Config.getProjectPrefix() + "." + beanName;
            } else {
                //根据规则生成Metric的名字
                metricName = Util.forMetricBean(bean.getClass(), beanName);
            }
            try {
                metrics.register(metricName, metricSet);
                log.debug("Registered metric named {} in registry. class: {}.", metricName, metricSet);
            } catch (IllegalArgumentException ex) {
                log.warn("Error injecting metric for field. bean named {}.", metricName, ex);
            }

        }
        return bean;
    }

    private boolean isJvmCollector(String beanName) {
        return beanName.indexOf("jvm") != -1;
    }

    private boolean canRegister(String beanName) {
        return !isJvmCollector(beanName) || Config.canJvmCollectorStart();
    }
}