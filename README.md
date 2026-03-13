# 🛒 Xiangwei Mall (乡味商城) - 微服务电商系统

![Java](https://img.shields.io/badge/Java-17-blue.svg)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.2.6-brightgreen.svg)
![Spring Cloud Alibaba](https://img.shields.io/badge/SpringCloudAlibaba-2023.0.1.0-orange.svg)
![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.6-blue.svg)
![Vue](https://img.shields.io/badge/Vue-3.x-4FC08D.svg)

**📢 找工作专用通道 / Open to Work**
> 本项目作者（蔡奕成）目前正在寻找 **Java 开发全职岗位**，支持线上面试，接受跨城入职。
> 为方便 Boss 直聘的 HR 与技术负责人免积分联系，欢迎直接发邮件至：**3242828406@qq.com** （全天在线，秒回高清 PDF 简历！）

## 📖 项目简介
**Xiangwei Mall (乡味商城)** 是一个基于 `Spring Cloud Alibaba` + `Vue 3` 构建的前后端分离微服务电商系统。项目实现了从商品展示、购物车管理、订单流转、物流追踪到售后评价的完整电商闭环，并对管理员、商家、普通用户进行了严格的 RBAC 权限隔离。

本项目不仅实现了丰富的业务功能，更着重解决了高并发场景下的微服务治理、分布式数据一致性、优雅异常处理以及大数据量下的内存级流式聚合等后端核心痛点，是一个极具实战价值的企业级架构项目。

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
| `xiangwei-order` | 9094 | **订单服务**：购物车管理、订单生成、状态流转、物流单号录入与商家数据统计 |
| `xiangwei-common` | - | **公共依赖模块**：全局异常拦截器、统一结果返回体 `Result`、基础工具类等 |

---

## ✨ 核心技术亮点

### 🔐 1. 安全架构：Gateway 网关 + 双 Token 刷新机制
* **无状态安全**：摒弃传统 Session，网关层 `AuthGlobalFilter` 统一拦截解析 JWT，将解析出的 `userId` 和 `type` (角色) 写入 Header 透传给下游微服务。
* **体验与安全并存**：采用 `AccessToken` (短效) + `RefreshToken` (长效存 Redis) 机制。用户长时间使用无感知刷新，一旦发生敏感操作，服务端主动清除 Redis 缓存，实现**强制下线**。
* **OSS 直传安全**：后端不直接处理文件流，而是通过阿里云 STS 下发临时凭证，前端直传 OSS，极大减轻了服务器网络 I/O 压力并保障了 AK/SK 安全。

### 🔄 2. 分布式事务：Seata AT 模式
* 解决微服务调用链路（如：订单微服务通过 OpenFeign 调用商品微服务扣减库存）中的数据一致性问题。
* 使用 `@GlobalTransactional` 开启全局事务，确保生成订单、扣减库存、清空购物车在同一事务内，**一举成功或集体回滚**，彻底杜绝“超卖”和“扣了库存没订单”的严重 Bug。

### 🛡️ 3. 优雅防御：JSR-303 校验与全局异常处理
* **告别 if-else**：全面接入 Hibernate Validator (JSR-303) 规范，支持嵌套对象 (`@Valid`) 与 URL 参数 (`@Validated`) 的严格参数校验。
* **统一返回规范**：在 Common 模块抽取 `@RestControllerAdvice` 全局异常拦截器，将底层 RuntimeException 与校验拦截优雅地包装为统一的 JSON 结构返回，提供极致的前后端交互体验。

### 📊 4. 性能调优：规避 OOM 的离线跑批与 Redis 缓存预热
* **化解高并发灾难**：针对传统应用层 Stream 聚合海量订单数据极易导致堆内存打满（OOM）的系统级隐患，对商家统计看板进行了深度架构重构。
* **离线跑批与直读缓存**：引入 @Scheduled 定时任务机制，在凌晨业务低谷期对日结数据进行增量计算，并将轻量级结果预热至 Redis 中。前端大屏报表直接拦截读取缓存，实现毫秒级响应，彻底解除了海量数据下的数据库 I/O 瓶颈与服务器内存危机。

### 📦 5. 完备的电商业务闭环
* **权限隔离**：后端利用 `LambdaQueryWrapper` 结合动态 SQL，实现严格的数据越权防护（商家只能看自己的订单，买家只能看自己的购物车）。
* **订单状态机**：完整实现了 `已支付` -> `已发货` -> `已完成` -> `售后/退款` 的状态流转。

---

## 🛠️ 技术栈清单

### 后端 (Backend)
* **核心框架**: Spring Boot 3.2.6 & Java 17
* **微服务生态**: Spring Cloud (2023.0.1) & Spring Cloud Alibaba (2023.0.1.0)
* **注册与配置中心**: Nacos
* **RPC 远程调用**: OpenFeign
* **分布式事务**: Seata 2.0.0
* **持久层框架**: MyBatis-Plus 3.5.6
* **参数校验**: JSR-303 (Hibernate Validator)
* **数据库**: MySQL 8.0
* **分布式缓存**: Redis (Spring Data Redis)
* **云对象存储**: 阿里云 OSS + STS sdk

### 前端 (Frontend)
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

*注：在 `xiangwei-service` 模块中，需要在本地环境变量中配置 `OSS_ACCESS_KEY_ID`, `OSS_ACCESS_KEY_SECRET` 和 `OSS_STS_ROLE_ARN` 以启用文件上传功能。*

### 4. 启动服务
请按照以下顺序启动 Spring Boot 启动类：
1. `GatewayApplication` (网关)
2. `XiangweiAuthService` (认证服务)
3. 依次启动 `Commodity`, `Order`, `System`, `Service` 等模块。

---

## 📞 联系与交流
* **👨‍💻 作者**：蔡奕成
* **📫 面试邀约**：如果您是 HR 或技术面试官，欢迎直接邮件联系 **3242828406@qq.com** 获取完整简历，我随时可以配合线上面试。
* **💡 技术探讨**：欢迎对微服务后端开发、Java 性能调优、分布式事务感兴趣的朋友提 Issue 或 PR 进行交流！如果在本地运行部署中遇到问题，随时欢迎探讨。
