# okhttp3-metrics-prometheus

OkHttp3 Metrics For Prometheus

### 组件简介

> OkHttp3 + Prometheus 整合实现，实现对 OkHttp3 的请求监控。

### 使用说明

##### 1、Spring Boot 项目添加 Maven 依赖

``` xml
<dependency>
	<groupId>com.github.hiwepy</groupId>
	<artifactId>okhttp3-metrics-prometheus</artifactId>
	<version>${project.version}</version>
</dependency>
```


##### 2、使用示例

访问本地配置：

http://localhost:8080/actuator/prometheus


```
# HELP redis_requests_seconds Number of requests
# TYPE redis_requests_seconds summary
redis_requests_seconds_count{application="druid-app",} 0.0
redis_requests_seconds_sum{application="druid-app",} 0.0
# HELP redis_request_time_max_seconds The longest request duration in time
# TYPE redis_request_time_max_seconds gauge
redis_request_time_max_seconds{application="druid-app",} 0.0
# HELP redis_request_errors_total Total number of error requests
# TYPE redis_request_errors_total counter
redis_request_errors_total{application="druid-app",} 0.0
# HELP redis_request_time_min_seconds The shortest request duration in time
# TYPE redis_request_time_min_seconds gauge
redis_request_time_min_seconds{application="druid-app",} -0.001
```

## Jeebiz 技术社区

Jeebiz 技术社区 **微信公共号**、**小程序**，欢迎关注反馈意见和一起交流，关注公众号回复「Jeebiz」拉你入群。

|公共号|小程序|
|---|---|
| ![](https://raw.githubusercontent.com/hiwepy/static/main/images/qrcode_for_gh_1d965ea2dfd1_344.jpg)| ![](https://raw.githubusercontent.com/hiwepy/static/main/images/gh_09d7d00da63e_344.jpg)|
