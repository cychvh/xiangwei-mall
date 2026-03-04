# 🛒 Xiangwei Mall (乡味商城) - 微服务电商系统

![Java](https://img.shields.io/badge/Java-17-blue.svg)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.2.6-brightgreen.svg)
![Spring Cloud Alibaba](https://img.shields.io/badge/SpringCloudAlibaba-2023.0.1.0-orange.svg)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.6-blue.svg)
![Vue](https://img.shields.io/badge/Vue-3.x-4FC08D.svg)

## 📖 项目简介
**Xiangwei Mall (乡味商城)** 是一个基于 `Spring Cloud Alibaba` + `Vue 3` 构建的前后端分离微服务电商系统。项目实现了从商品展示、购物车管理、订单流转、物流追踪到售后评价的完整电商闭环，并对管理员、商家、普通用户进行了严格的 RBAC 权限隔离。

本项目不仅实现了丰富的业务功能，更着重解决了高并发场景下的微服务治理、分布式数据一致性以及云存储安全等后端核心痛点，是一个极具实战价值的企业级架构项目。

---

## 🏗️ 系统架构与微服务划分

项目采用标准的微服务架构，各职责模块独立部署，通过 Nacos 进行服务发现与配置管理，通过 Gateway 进行统一路由与鉴权。

| 微服务模块 | 端口 | 描述与核心功能 |
| :--- | :--- | :--- |
| `xiangwei-gateway` | 8080 | **API 网关**：统一流量入口，基于全局过滤器实现 JWT 鉴权与请求拦截 |
| `xiangwei-auth` | 9090 | **认证与用户服务**：用户注册登录，基于 Redis 的 Access/Refresh 双 Token 管理 |
| `xiangwei-commodity`| 9091 | **商品服务**：商品上下架、库存扣减、商品评价分页查询 |
| `xiangwei-service` | 9092 | **基础服务**：集成阿里云 STS 服务，生成临时安全凭证供前端直传 OSS |
| `xiangwei-system` | 9093 | **系统服务**：系统公告管理，支持根据不同角色（用户/商家）动态下发公告 |
| `xiangwei-order` | 9094 | **订单服务**：购物车管理、订单生成、状态流转、物流单号录入与售后申请 |
| `xiangwei-common` | - | **公共依赖模块**：统一结果返回体 `Result`、基础工具类等 |

---

## ✨ 核心技术亮点

### 🔐 1. 安全架构：Gateway 网关 + 双 Token 刷新机制
* **无状态安全**：摒弃传统 Session，网关层 `AuthGlobalFilter` 统一拦截解析 JWT，将解析出的 `userId` 和 `type` (角色) 写入 Header 透传给下游微服务。
* **体验与安全并存**：采用 `AccessToken` (短效) + `RefreshToken` (长效存 Redis) 机制。用户长时间使用无感知刷新，一旦发生密码/权限修改等敏感操作，服务端主动清除 Redis 缓存，实现**强制下线**。
* **OSS 直传安全**：后端不直接处理文件流，而是通过阿里云 STS (Security Token Service) 下发临时凭证，前端直传 OSS，极大减轻了服务器网络 I/O 压力并保障了 AK/SK 安全。

### 🔄 2. 分布式事务：Seata AT 模式
* 解决微服务调用链路（如：订单微服务通过 OpenFeign 调用商品微服务扣减库存）中的数据一致性问题。
* 使用 `@GlobalTransactional` 开启全局事务，确保生成订单、扣减库存、清空购物车在同一事务内，**一举成功或集体回滚**，彻底杜绝“超卖”和“扣了库存没订单”的严重 Bug。

### 📦 3. 完备的电商业务闭环
* **权限隔离**：0-管理员，1-商家，2-普通用户。后端利用 `LambdaQueryWrapper` 结合动态 SQL，实现严格的数据越权防护（如商家只能看自己的订单，买家只能看自己的购物车）。
* **订单状态机**：完整实现了 `已支付(1)` -> `已发货(2)` -> `已完成(3)` -> `售后/退款(4)` 的状态流转。
* **防呆设计**：前端 Computed 动态过滤与后端灵活的分页搜索结合，处理了诸如主键冲突、非法传参等诸多边界情况。

---

## 🛠️ 技术栈清单

### 后端 (Backend)
* **核心框架**: Spring Boot 3.2.6
* **微服务全家桶**: Spring Cloud (2023.0.1) & Spring Cloud Alibaba (2023.0.1.0)
* **注册与配置中心**: Nacos
* **RPC 远程调用**: OpenFeign
* **分布式事务**: Seata 2.0.0
* **ORM 框架**: MyBatis-Plus 3.5.6
* **数据库**: MySQL 8.0
* **分布式缓存**: Redis (Spring Data Redis + JSON 序列化)
* **云对象存储**: 阿里云 OSS + STS sdk

### 前端 (Frontend) (另见前端仓库)
* **框架**: Vue 3 (Composition API)
* **UI 组件库**: Element Plus
* **网络请求**: Axios (封装拦截器实现 Token 自动携带)

---

## 🚀 快速本地运行

### 1. 环境准备
* JDK 17
* MySQL 8.0+
* Redis 6.0+
* Nacos 2.x (单机模式启动)
* Seata Server 2.x

### 2. 数据库初始化
本项目对不同微服务进行了数据库隔离（分库），请在 MySQL 中创建以下数据库，并导入对应的数据表：
* `xiangwei_auth`
* `xiangwei_product`
* `xiangwei_order`
* `xiangwei_system`

### 3. 修改配置文件
找到各个微服务下的 `src/main/resources/application.yml` 或 `application.properties`，修改以下配置：
* MySQL 的账号密码
* Redis 的连接地址和密码
* Nacos & Seata 的 Server 地址
* （可选）配置环境变量项如 `REDIS_HOST`, `REDIS_PASSWORD`。

*注：在 `xiangwei-service` 模块中，需要在本地环境变量中配置 `OSS_ACCESS_KEY_ID`, `OSS_ACCESS_KEY_SECRET` 和 `OSS_STS_ROLE_ARN` 以启用文件上传功能。*

### 4. 启动服务
请按照以下顺序启动 Spring Boot 启动类：
1. `GatewayApplication` (网关)
2. `XiangweiAuthService` (认证服务)
3. 依次启动 `Commodity`, `Order`, `System`, `Service` 等模块。

---

## 📞 联系与交流
* 作者：蔡奕成
* 欢迎对微服务后端开发、Java 性能调优感兴趣的朋友提 Issue 或 PR 进行交流！如果在运行部署中遇到问题，随时欢迎探讨。
