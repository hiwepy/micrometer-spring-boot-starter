package io.micrometer.spring.boot;

import okhttp3.ConnectionPool;

public interface MicrometerConfigurer {

    void configure(ConnectionPool connectionPool);

    void configure(okhttp3.OkHttpClient.Builder builder);

}
