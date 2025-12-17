# Eureka 微服务 K8s 部署指南

本指南说明如何将 Eureka Server、Gateway 和 Provider 部署到腾讯云 TKE (Kubernetes) 集群。

## 📋 前置条件

1. ✅ 已构建并推送 Docker 镜像到腾讯云镜像仓库
   - `ccr.ccs.tencentyun.com/tsf_100020616957/eureka-server:latest`
   - `ccr.ccs.tencentyun.com/tsf_100020616957/eureka-gateway:latest`
   - `ccr.ccs.tencentyun.com/tsf_100020616957/eureka-provider:latest`

2. ✅ 已部署 Eureka Server，并获取其 IP 地址
   - 当前 Eureka Server IP: `192.168.109.30:8761`

3. ✅ 已配置 kubectl 连接到 TKE 集群

## 🚀 快速部署

### 1. 部署 Gateway

```bash
kubectl apply -f gateway-deployment.yaml
```

### 2. 部署 Provider

```bash
kubectl apply -f provider-deployment.yaml
```

### 3. 查看部署状态

```bash
# 查看所有 Pod
kubectl get pods

# 查看 Gateway Pod
kubectl get pods -l app=eureka-gateway

# 查看 Provider Pod
kubectl get pods -l app=eureka-provider

# 查看 Service
kubectl get svc
```

## 🔍 验证部署

### 1. 检查 Pod 是否正常运行

```bash
# 查看 Gateway Pod 详情
kubectl describe pod -l app=eureka-gateway

# 查看 Provider Pod 详情
kubectl describe pod -l app=eureka-provider
```

### 2. 查看应用日志

```bash
# 查看 Gateway 日志
kubectl logs -f -l app=eureka-gateway

# 查看 Provider 日志
kubectl logs -f -l app=eureka-provider

# 过滤 Eureka 相关日志
kubectl logs -l app=eureka-gateway | grep -i eureka
```

### 3. 验证服务注册

访问 Eureka Server 控制台：
```bash
# 在浏览器中打开
http://192.168.109.30:8761

# 或使用 curl 查看注册的服务
curl http://192.168.109.30:8761/eureka/apps
```

应该能看到 `EUREKA-GATEWAY` 和 `EUREKA-PROVIDER` 已注册。

### 4. 测试服务调用

```bash
# 进入 Gateway Pod
kubectl exec -it $(kubectl get pod -l app=eureka-gateway -o jsonpath='{.items[0].metadata.name}') -- sh

# 在 Pod 内测试调用 Provider
curl http://localhost:8080/provider/test

# 或者通过 Service 调用
curl http://eureka-gateway:8080/provider/test
```

## ⚙️ 配置说明

### 环境变量配置

两个部署文件都使用环境变量来配置 Eureka 地址：

| 环境变量 | 说明 | 默认值 |
|---------|------|--------|
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka Server 地址 | `http://192.168.109.30:8761/eureka/` |
| `EUREKA_INSTANCE_IP_ADDRESS` | 实例 IP（自动获取 Pod IP） | `status.podIP` |
| `EUREKA_INSTANCE_PREFER_IP_ADDRESS` | 优先使用 IP 地址注册 | `true` |

### 修改 Eureka Server 地址

如果 Eureka Server IP 变更，只需修改部署文件中的环境变量：

```yaml
env:
- name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
  value: "http://新的IP:8761/eureka/"
```

然后重新部署：
```bash
kubectl apply -f gateway-deployment.yaml
kubectl apply -f provider-deployment.yaml
```

### 资源配置

默认资源配置：

| 资源 | Request | Limit |
|------|---------|-------|
| Memory | 512Mi | 1Gi |
| CPU | 250m | 500m |

根据实际负载调整 `resources` 配置。

## 🔧 故障排查

### 问题 1: Pod 无法启动

```bash
# 查看 Pod 事件
kubectl describe pod <pod-name>

# 查看容器日志
kubectl logs <pod-name>

# 查看上一次容器日志（如果容器重启了）
kubectl logs <pod-name> --previous
```

**常见原因：**
- 镜像拉取失败：检查镜像仓库权限
- 资源不足：检查节点资源
- 健康检查失败：调整 `initialDelaySeconds`

### 问题 2: 服务未注册到 Eureka

```bash
# 查看应用日志中的 Eureka 相关信息
kubectl logs <pod-name> | grep -i "eureka\|register\|discovery"
```

**常见原因：**
- Eureka Server 地址配置错误
- 网络不通：测试 Pod 到 Eureka Server 的连通性
  ```bash
  kubectl exec -it <pod-name> -- curl http://192.168.109.30:8761/eureka/
  ```
- 应用启动失败：查看完整日志

### 问题 3: Gateway 无法调用 Provider

```bash
# 检查 Eureka 中的服务列表
curl http://192.168.109.30:8761/eureka/apps/EUREKA-PROVIDER

# 查看 Gateway 日志
kubectl logs -l app=eureka-gateway | grep -i "provider\|ribbon\|loadbalancer"
```

**常见原因：**
- Provider 未成功注册
- 服务名称不匹配
- 网络策略限制

### 问题 4: 健康检查失败

```bash
# 手动测试健康检查端点
kubectl exec -it <pod-name> -- curl http://localhost:8080/actuator/health
```

**解决方法：**
- 增加 `initialDelaySeconds`（应用启动时间较长）
- 检查 actuator 端点是否启用
- 调整 `timeoutSeconds` 和 `failureThreshold`

## 📊 监控和日志

### 查看实时日志

```bash
# 实时跟踪 Gateway 日志
kubectl logs -f -l app=eureka-gateway

# 查看最近 100 行日志
kubectl logs --tail=100 -l app=eureka-gateway

# 查看带时间戳的日志
kubectl logs --timestamps -l app=eureka-gateway
```

### 查看资源使用情况

```bash
# 查看 Pod 资源使用
kubectl top pods

# 查看节点资源使用
kubectl top nodes
```

## 🔄 更新部署

### 更新镜像版本

```bash
# 方法 1: 修改 YAML 文件后重新应用
kubectl apply -f gateway-deployment.yaml

# 方法 2: 直接设置新镜像
kubectl set image deployment/eureka-gateway \
  eureka-gateway=ccr.ccs.tencentyun.com/tsf_100020616957/eureka-gateway:v2.0

# 查看滚动更新状态
kubectl rollout status deployment/eureka-gateway
```

### 回滚部署

```bash
# 查看历史版本
kubectl rollout history deployment/eureka-gateway

# 回滚到上一个版本
kubectl rollout undo deployment/eureka-gateway

# 回滚到指定版本
kubectl rollout undo deployment/eureka-gateway --to-revision=2
```

## 🎯 扩缩容

### 手动扩缩容

```bash
# 扩容 Provider 到 5 个副本
kubectl scale deployment/eureka-provider --replicas=5

# 缩容到 1 个副本
kubectl scale deployment/eureka-provider --replicas=1
```

### 自动扩缩容 (HPA)

创建 HPA 配置：

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: eureka-provider-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: eureka-provider
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

## 🧹 清理资源

```bash
# 删除 Gateway
kubectl delete -f gateway-deployment.yaml

# 删除 Provider
kubectl delete -f provider-deployment.yaml

# 或者直接删除资源
kubectl delete deployment eureka-gateway eureka-provider
kubectl delete service eureka-gateway eureka-provider
```

## 📝 注意事项

1. **镜像拉取策略**：使用 `imagePullPolicy: Always` 确保每次都拉取最新镜像
2. **健康检查**：根据应用实际启动时间调整 `initialDelaySeconds`
3. **资源限制**：根据实际负载调整 CPU 和内存配置
4. **副本数量**：Provider 默认 2 个副本，可根据需要调整
5. **网络策略**：确保 Pod 可以访问 Eureka Server 的 IP 和端口

## 🔗 相关链接

- [Kubernetes 官方文档](https://kubernetes.io/docs/)
- [腾讯云 TKE 文档](https://cloud.tencent.com/document/product/457)
- [Spring Cloud Eureka 文档](https://spring.io/projects/spring-cloud-netflix)
