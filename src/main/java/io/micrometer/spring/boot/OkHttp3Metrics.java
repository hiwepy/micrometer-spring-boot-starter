/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.micrometer.spring.boot;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.binder.MeterBinder;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServer;
import org.springframework.boot.web.embedded.undertow.UndertowWebServer;
import org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

/**
 * OkHttp3 Metrics
 */
public class OkHttp3Metrics implements MeterBinder, ApplicationListener<ApplicationStartedEvent> {

	/**
	 * Prefix used for all OkHttp3 metric names.
	 */
	public static final String OKHTTP3_METRIC_NAME_PREFIX = "okhttp3";

	/**
	 * timeout
	 */
	private static final String METRIC_NAME_CALL_TIMEOUT_COUNT 			= OKHTTP3_METRIC_NAME_PREFIX + ".call.timeout.count";
	private static final String METRIC_NAME_CONNECT_TIMEOUT_COUNT 		= OKHTTP3_METRIC_NAME_PREFIX + ".connect.timeout.count";
	private static final String METRIC_NAME_READ_TIMEOUT_COUNT 			= OKHTTP3_METRIC_NAME_PREFIX + ".read.timeout.count";
	private static final String METRIC_NAME_WRITE_TIMEOUT_COUNT 		= OKHTTP3_METRIC_NAME_PREFIX + ".write.timeout.count";
	private static final String METRIC_NAME_PING_FAIL_COUNT 			= OKHTTP3_METRIC_NAME_PREFIX + ".ping.fail.count";

	/**
	 * dispatcher
	 */
	private static final String METRIC_NAME_DISPATCHER_MAX_REQUESTS 			= OKHTTP3_METRIC_NAME_PREFIX + ".dispatcher.max.requests";
	private static final String METRIC_NAME_DISPATCHER_MAX_REQUESTS_PERHOST 	= OKHTTP3_METRIC_NAME_PREFIX + ".dispatcher.max.requests.perhost";
	private static final String METRIC_NAME_DISPATCHER_QUEUED_CALLS_COUNT		= OKHTTP3_METRIC_NAME_PREFIX + ".dispatcher.queued.calls.count";
	private static final String METRIC_NAME_DISPATCHER_RUNNING_CALLS_COUNT		= OKHTTP3_METRIC_NAME_PREFIX + ".dispatcher.running.calls.count";
	private static final String METRIC_NAME_CONNECTION_POOL_CONNECTION_COUNT		= OKHTTP3_METRIC_NAME_PREFIX + ".connection.pool.connection.count";
	private static final String METRIC_NAME_CONNECTION_POOL_IDLE_CONNECTION_COUNT		= OKHTTP3_METRIC_NAME_PREFIX + ".connection.pool.idle.connection.count";

	private OkHttpClient okhttp3Client;
	private Iterable<Tag> tags;

	public OkHttp3Metrics(OkHttpClient okhttp3Client) {
		this(okhttp3Client, Collections.emptyList());
	}

	public OkHttp3Metrics(OkHttpClient okhttp3Client, Iterable<Tag> tags) {
		this.okhttp3Client = okhttp3Client;
		this.tags = tags;
	}

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		this.bindTo(event.getApplicationContext().getBean(MeterRegistry.class));
	}

	@Override
	public void bindTo(MeterRegistry registry) {
		registerDispatcher(registry);
		registerConnectionPool(registry);
	}

	private void registerDispatcher(MeterRegistry registry) {
		Dispatcher dispatcher = okhttp3Client.dispatcher();
		bindCounter(registry, METRIC_NAME_DISPATCHER_MAX_REQUESTS, "max requests of dispatcher ", dispatcher, Dispatcher::getMaxRequests);
		bindCounter(registry, METRIC_NAME_DISPATCHER_MAX_REQUESTS_PERHOST, "max requests of dispatcher by per host ", dispatcher, Dispatcher::getMaxRequestsPerHost);
		bindTimeGauge(registry, METRIC_NAME_DISPATCHER_QUEUED_CALLS_COUNT, "Total number of queued calls ", dispatcher, Dispatcher::queuedCallsCount);
		bindTimeGauge(registry, METRIC_NAME_DISPATCHER_RUNNING_CALLS_COUNT, "Total number of running calls ", dispatcher, Dispatcher::runningCallsCount);
	}

	private void registerConnectionPool(MeterRegistry registry) {
		ConnectionPool connectionPool = okhttp3Client.connectionPool();
		TimeGauge.builder(METRIC_NAME_CONNECTION_POOL_CONNECTION_COUNT, connectionPool, TimeUnit.SECONDS, ConnectionPool::connectionCount)
				.description("Connection Pool Connection Count")
				.tags(tags)
				.tag("name", connectionPool.getClass().getName())
				.register(registry);
		TimeGauge.builder(METRIC_NAME_CONNECTION_POOL_IDLE_CONNECTION_COUNT, connectionPool, TimeUnit.SECONDS, ConnectionPool::idleConnectionCount)
				.description("Connection Pool idle Connection Count")
				.tags(tags)
				.tag("name", connectionPool.getClass().getName())
				.register(registry);
	}

	private void registerOkHttpClient(MeterRegistry registry) {
		Gauge.builder(METRIC_NAME_CALL_TIMEOUT_COUNT, okhttp3Client, OkHttpClient::callTimeoutMillis)
				.description("OkHttp3 Client call timeout count")
				.tags(tags)
				.tag("name", okhttp3Client.getClass().getName())
				.register(registry);
		Gauge.builder(METRIC_NAME_CONNECTION_POOL_CONNECTION_COUNT, okhttp3Client, OkHttpClient::connectTimeoutMillis)
				.description("OkHttp3 Client connectTimeoutMillis")
				.tags(tags)
				.tag("name", okhttp3Client.getClass().getName())
				.register(registry);
		Gauge.builder(METRIC_NAME_CONNECTION_POOL_CONNECTION_COUNT, okhttp3Client, OkHttpClient::readTimeoutMillis)
				.description("OkHttp3 Client readTimeoutMillis")
				.tags(tags)
				.tag("name", okhttp3Client.getClass().getName())
				.register(registry);
		Gauge.builder(METRIC_NAME_CONNECTION_POOL_CONNECTION_COUNT, okhttp3Client, OkHttpClient::writeTimeoutMillis)
				.description("OkHttp3 Client writeTimeoutMillis")
				.tags(tags)
				.tag("name", okhttp3Client.getClass().getName())
				.register(registry);
		Gauge.builder(METRIC_NAME_CONNECTION_POOL_CONNECTION_COUNT, okhttp3Client, OkHttpClient::pingIntervalMillis)
				.description("OkHttp3 Client pingIntervalMillis")
				.tags(tags)
				.tag("name", okhttp3Client.getClass().getName())
				.register(registry);
	}

	private <T> void bindTimer(MeterRegistry registry, String name, String desc, T metricsHandler,
						   ToLongFunction<T> countFunc, ToDoubleFunction<T> consumer) {
		FunctionTimer.builder(name, metricsHandler, countFunc, consumer, TimeUnit.SECONDS)
				.description(desc)
				.tags(tags)
				.register(registry);
	}

	private <T> void bindTimeGauge(MeterRegistry registry, String name, String desc, T metricResult,
							   ToDoubleFunction<T> consumer) {
		TimeGauge.builder(name, metricResult, TimeUnit.SECONDS, consumer)
				.description(desc)
				.tags(tags)
				.register(registry);
	}

	private <T> void bindCounter(MeterRegistry registry, String name, String desc, T metricsHandler,
							 ToDoubleFunction<T> consumer) {
		FunctionCounter.builder(name, metricsHandler, consumer)
				.description(desc)
				.tags(tags)
				.register(registry);
	}


}
