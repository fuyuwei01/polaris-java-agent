# Spring Cloud 2021 Eureka Demo

这是一个基于Spring Cloud 2021和Eureka注册中心的微服务示例项目。

## 项目结构

- `eureka-server`: Eureka注册中心服务（端口：8761）
- `provider`: 服务提供者（端口：8081）
- `consumer`: 服务消费者（端口：8082）
- `gateway`: API网关（端口：8080）

## 启动顺序

1. 首先启动Eureka Server：
   ```bash
   cd eureka-server
   mvn spring-boot:run
   ```

2. 然后启动Provider：
   ```bash
   cd provider
   mvn spring-boot:run
   ```

3. 启动Consumer：
   ```bash
   cd consumer
   mvn spring-boot:run
   ```

4. 最后启动Gateway：
   ```bash
   cd gateway
   mvn spring-boot:run
   ```

## 访问方式

### Eureka Dashboard
- 地址：http://localhost:8761
- 查看注册的服务实例

### 直接访问服务
- Provider: http://localhost:8081/hello/{name}
- Consumer: http://localhost:8082/call/{name}

### 通过Gateway访问
- Provider: http://localhost:8080/provider/hello/{name}
- Consumer: http://localhost:8080/consumer/call/{name}

## 健康检查

所有服务都提供了健康检查端点：
- http://localhost:8080/actuator/health (Gateway)
- http://localhost:8081/actuator/health (Provider)
- http://localhost:8082/actuator/health (Consumer)

## 技术栈

- Spring Boot 2.7.18
- Spring Cloud 2021.0.5
- Netflix Eureka Server/Client
- Spring Cloud Gateway
- RestTemplate (负载均衡)

## 服务发现流程

1. 所有服务启动后自动注册到Eureka Server
2. Consumer通过服务名(eureka-provider)调用Provider，Eureka自动进行负载均衡
3. Gateway通过服务发现自动路由到对应的服务实例