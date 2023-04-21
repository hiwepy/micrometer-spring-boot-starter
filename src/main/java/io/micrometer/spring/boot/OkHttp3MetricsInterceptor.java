package io.micrometer.spring.boot;


import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;


/**
 * okhttp3 metrics interceptor
 * @author linux_china
 */
public class OkHttp3MetricsInterceptor implements Interceptor {
	
	private MeterRegistry registry;

    public OkHttp3MetricsInterceptor(MeterRegistry registry) {
        this.registry = registry;
    }
    
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String host = request.url().host();
        Response response;
        final Timer timer = registry.timer(name(OkHttpClient.class, host, request.method()));
        final Timer.Context context = timer.time();
        try {
            response = chain.proceed(request);
        } finally {
            context.stop();
        }
        FunctionCounter.builder(name, metricsHandler, consumer)
                .description(desc)
                .tags(tags)
                .register(registry);
        return response;
    }
    
}