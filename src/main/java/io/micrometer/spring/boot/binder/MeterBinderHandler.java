package io.micrometer.spring.boot.binder;

import io.micrometer.common.lang.NonNull;
import io.micrometer.core.instrument.MeterRegistry;

public interface MeterBinderHandler {

    /**
     * Whether it is supported
     * @author 		： <a href="https://github.com/hiwepy">wandl</a>
     * @param bean  the bean instance
     * @return true or false
     */
    public boolean supports(Object bean, String beanName) ;

    /**
     * Bind the bean to the registry
     * @param registry the registry
     */
    public default void bindTo(@NonNull MeterRegistry registry)  {
    };

}
