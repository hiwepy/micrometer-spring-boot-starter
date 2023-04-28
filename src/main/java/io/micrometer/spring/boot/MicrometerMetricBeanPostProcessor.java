package io.micrometer.spring.boot;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import io.micrometer.spring.boot.binder.MeterBinderHandler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Objects;

@Slf4j
public class MicrometerMetricBeanPostProcessor implements BeanPostProcessor {

    MeterRegistry registry;
    List<MeterBinderHandler> handlers;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public static <T> T unwrap(Object bean, Class<T> target) {
        if (target.isInstance(bean)) {
            return target.cast(bean);
        }
        if (AopUtils.isAopProxy(bean)) {
            Object proxyTarget = AopProxyUtils.getSingletonTarget(bean);
            return unwrap(proxyTarget, target);
        }
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ThreadPoolTaskExecutor executor = Util.getThreadPoolTaskExecutor(bean);
        ThreadPoolTaskExecutor okHttpClient = unwrap(bean, ThreadPoolTaskExecutor.class);
        if (Objects.nonNull(okHttpClient)) {

        }
        ExecutorServiceMetrics.monitor(registry, executor, pool.getThreadNamePrefix(),  metricPrefix);


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