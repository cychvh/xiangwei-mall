# 接口报告 / API Report

## 1) 概述与范围
本报告基于当前仓库源码自动扫描生成，范围覆盖 `src/main/java` 下的 Controller 与安全相关配置。

- 后端框架：Spring Boot
- 扫描范围：Controller、Config（JWT/拦截器）、DTO/VO、实体与返回结构
- 文档来源：代码与配置文件（未发现 Swagger/OpenAPI 配置）

## 2) 全局约定

### 2.1 服务与基础路径
- 服务端口：`9999`（见 `src/main/resources/application.yml`）
- 基础路径：TBD（代码中未看到全局 `/api` 前缀配置）

### 2.2 鉴权（JWT）
- Header：`Authorization: Bearer <accessToken>`
- 放行接口：`/user/login`、`/user/register`、`/user/refresh`
- 其余接口：需要 JWT；拦截器会解析并注入 `userId`/`type`/`username` 至 `request`（见 `src/main/java/com/cyc/xiangwei/Config/JwtInterceptor.java`）
- Refresh Token：服务端要求 Redis 中存在对应 refresh token，否则 401

### 2.3 统一返回结构
返回结构为 `Result{ code, msg, data }`（见 `src/main/java/com/cyc/xiangwei/bean/Result.java`）。

示例：
```json
{
  "code": "200",
  "msg": "success",
  "data": {}
}
```

### 2.4 分页约定
多个接口使用 MyBatis-Plus `Page`/`IPage`。返回结构由框架提供，常见字段为 `records`、`total`、`size`、`current`、`pages`（字段以实际运行时为准）。

分页参数按接口定义，常见为：
- `pageNum`（默认 1）
- `pageSize` 或 `size`（默认 5 或 8）

### 2.5 时间格式
- `Notice.date` 使用 `yyyy-MM-dd'T'HH:mm:ss` 且 `GMT+8`（见 `src/main/java/com/cyc/xiangwei/bean/Notice.java`）
- 其他 `LocalDateTime`（如 `Order.createTime`、`OrderDelivery.shipTime`）：TBD（代码中未明确序列化格式）

### 2.6 通用错误码（来自源码）
仅列出代码中出现的错误码与语义（消息字符串有编码乱码则标记为 TBD）。

- `200`：成功
- `400`：参数不合法（数量/公告 ID 等）
- `401`：未登录
- `403`：无权限
- `404`：资源不存在
- `405`：权限不足/操作失败（多处复用）
- `406`：修改失败
- `500`：服务器错误
- `10086`：用户名已被使用

## 3) 接口清单总表（按模块分组）

### 用户模块 UserController
- `POST /user/login` 登录
- `POST /user/refresh` 刷新 Token
- `POST /user/register` 注册
- `GET /user/logout` 登出
- `GET /user/getuser` 用户列表（管理员）
- `PUT /user/updateUser` 更新用户（管理员/本人）
- `DELETE /user/{id}` 删除用户（管理员）

### 商品模块 productController
- `GET /product/list` 商家商品列表
- `GET /product/AU/list` 用户/管理员商品列表（仅上架）
- `PUT /product/admin/update` 管理员更新商品类别（代码逻辑实际为 `type != 0` 可通过）
- `GET /product/user/productOne/{id}` 用户查看商品详情
- `POST /product/add` 商家新增商品（multipart）
- `PUT /product/update` 商家更新商品（multipart）
- `DELETE /product/{id}` 商家删除商品
- `PUT /product/status` 商家上下架

### 订单模块 OrderController
- `GET /order/userList` 用户订单列表
- `GET /order/adminList` 管理员订单列表
- `GET /order/getOrderItem` 订单明细
- `POST /order/addOrder` 新增订单
- `GET /order/MerchantList` 商家订单列表
- `GET /order/merchantOrderItems` 商家订单明细
- `PUT /order/confirmReceipt` 用户确认收货

### 购物车模块 CartController
- `GET /cart/list` 购物车列表
- `POST /cart/addcart` 加入购物车
- `PUT /cart/updateCart` 更新购物车数量
- `DELETE /cart/delCart` 删除购物车项

### 物流模块 DeliveryController
- `POST /delivery/ship` 商家发货
- `GET /delivery/list` 商家物流列表
- `PUT /delivery/correct` 商家修正物流
- `GET /delivery/getOne` 用户查看物流

### 公告模块 NoticeController
- `GET /notice/getNotice` 用户/商家公告列表
- `GET /notice/list` 管理员公告列表
- `POST /notice/addNotice` 新增公告（管理员）
- `DELETE /notice/deleteNotice` 删除公告（管理员）
- `PUT /notice/updateNotice` 更新公告（管理员）

### 统计模块 StatisticsController
- `GET /statistics/getAllStats` 商家统计数据

## 4) 逐接口详情

### 4.1 用户模块

#### 4.1.1 登录
- 接口名称：用户登录
- URL：`POST /user/login`
- 鉴权：否
- 请求头：`Content-Type: application/json`
- 请求体：
  - `username` (string)
  - `password` (string)
- 响应结构：
  - `accessToken` (string)
  - `refreshToken` (string)
  - `user` (User)
- 响应示例：
```json
{
  "code": "200",
  "msg": "success",
  "data": {
    "accessToken": "ACCESS_TOKEN",
    "refreshToken": "REFRESH_TOKEN",
    "user": {
      "id": 1,
      "username": "demo",
      "password": null,
      "phone": "13800000000",
      "type": 2,
      "address": "TBD"
    }
  }
}
```
- 错误码：`500`（用户名或密码不正确，消息字符串存在编码乱码，TBD）
- 备注：登录成功会生成 access/refresh token 并写入 Redis

#### 4.1.2 刷新 Token
- 接口名称：刷新 Token
- URL：`POST /user/refresh`
- 鉴权：否（使用 refresh token）
- 请求头：`Content-Type: application/json`
- 请求体：
  - `refreshToken` (string)
- 响应结构：
  - `accessToken` (string)
  - `refreshToken` (string)
- 响应示例：
```json
{
  "code": "200",
  "msg": "success",
  "data": {
    "accessToken": "NEW_ACCESS_TOKEN",
    "refreshToken": "NEW_REFRESH_TOKEN"
  }
}
```
- 错误码：`500`（refreshToken 为空/无效/过期/不匹配）

#### 4.1.3 注册
- 接口名称：用户注册
- URL：`POST /user/register`
- 鉴权：否
- 请求头：`Content-Type: application/json`
- 请求体：
  - `username` (string)
  - `password` (string)
  - `phone` (string, optional)
  - `type` (integer, optional)
  - `address` (string, optional)
- 响应示例：
```json
{ "code": "200", "msg": "success", "data": null }
```
- 错误码：`10086`（用户名已被使用）

#### 4.1.4 登出
- 接口名称：用户登出
- URL：`GET /user/logout`
- 鉴权：是
- 请求头：`Authorization: Bearer <accessToken>`
- 响应示例：
```json
{ "code": "200", "msg": "success", "data": null }
```
- 备注：当前 Controller 未调用 `logout` 清理 Redis，仅返回成功

#### 4.1.5 用户列表（管理员）
- 接口名称：用户分页查询
- URL：`GET /user/getuser`
- 鉴权：是（仅管理员 `type=0`）
- 查询参数：
  - `pageNum` (int, default=1)
  - `size` (int, default=5)
  - `search` (string, default="")
- 响应结构：`Page<User>`
- 错误码：`405`（权限不足）

#### 4.1.6 更新用户
- 接口名称：更新用户
- URL：`PUT /user/updateUser`
- 鉴权：是（管理员或本人）
- 请求体：`User`
- 备注：
  - 管理员可更新 `username`、`type`
  - 普通用户可更新 `address`、`password`
- 错误码：
  - `401` 未登录
  - `405` 权限不足

#### 4.1.7 删除用户（管理员）
- 接口名称：删除用户
- URL：`DELETE /user/{id}`
- 鉴权：是（仅管理员 `type=0`）
- 路径参数：`id` (int)
- 错误码：
  - `405` 权限不足
  - `500` 删除失败

### 4.2 商品模块

#### 4.2.1 商家商品列表
- URL：`GET /product/list`
- 鉴权：是（仅商家 `type=1`）
- 查询参数：`pageNum`、`pageSize`、`search`
- 响应结构：`Page<Product>`
- 错误码：
  - `401` 未登录
  - `403` 非商家无权访问

#### 4.2.2 用户/管理员商品列表
- URL：`GET /product/AU/list`
- 鉴权：是（非商家）
- 查询参数：`pageNum`、`pageSize`、`search`
- 响应结构：`Page<Product>`（仅 `status=1`）
- 错误码：`403` 无权访问

#### 4.2.3 管理员更新商品类别
- URL：`PUT /product/admin/update`
- 鉴权：是（代码逻辑为 `type != 0` 才能通过）
- 请求体：`Product`
- 备注：仅更新 `categoryname`
- 错误码：`403` 非管理员无权访问（逻辑与文案存在不一致）

#### 4.2.4 用户查看商品详情
- URL：`GET /product/user/productOne/{id}`
- 鉴权：是（仅用户 `type=2`）
- 路径参数：`id` (int)
- 响应结构：`Product`
- 错误码：
  - `403` 权限不足
  - `405` 产品为空或未上架

#### 4.2.5 商家新增商品
- URL：`POST /product/add`
- 鉴权：是（仅商家 `type=1`）
- 请求头：`Content-Type: multipart/form-data`
- 表单字段：
  - `product`：`Product`（JSON）
  - `file`：图片文件
- 错误码：
  - `401` 未登录
  - `403` 非商家无权操作
  - `500` 服务端错误

#### 4.2.6 商家更新商品
- URL：`PUT /product/update`
- 鉴权：是（仅商家）
- 请求头：`Content-Type: multipart/form-data`
- 表单字段：
  - `product`：`Product`（JSON）
  - `file`：图片文件（可选）
- 错误码：`401`、`500`

#### 4.2.7 商家删除商品
- URL：`DELETE /product/{id}`
- 鉴权：是（仅商家）
- 路径参数：`id` (int)
- 错误码：`401`、`500`

#### 4.2.8 商家上下架
- URL：`PUT /product/status`
- 鉴权：是（仅商家）
- 查询参数：`id`、`status`
- 错误码：
  - `401` 未登录
  - `404` 商品不存在
  - `403` 无权操作

### 4.3 订单模块

#### 4.3.1 用户订单列表
- URL：`GET /order/userList`
- 鉴权：是（仅用户 `type=2`）
- 查询参数：`pageNum`、`pageSize`、`productName`
- 响应结构：`IPage<Order>`
- 错误码：`405` 权限不足

#### 4.3.2 管理员订单列表
- URL：`GET /order/adminList`
- 鉴权：是（仅管理员 `type=0`）
- 查询参数：`pageNum`、`pageSize`、`orderId`
- 响应结构：`Page<Order>`
- 错误码：`405` 权限不足

#### 4.3.3 订单明细（用户）
- URL：`GET /order/getOrderItem`
- 鉴权：是（用户）
- 查询参数：`orderId`
- 响应结构：`List<OrderItem>`
- 错误码：`500`（服务端异常）

#### 4.3.4 新增订单
- URL：`POST /order/addOrder`
- 鉴权：是（仅用户 `type=2`）
- 请求体：`OrderDTO`
  - `order`：`Order`
  - `orderItems`：`List<OrderItem>`
- 错误码：
  - `405` 权限不足
  - `500` 数据为空或服务端异常

#### 4.3.5 商家订单列表
- URL：`GET /order/MerchantList`
- 鉴权：是（仅商家 `type=1`）
- 查询参数：`pageNum`、`pageSize`
- 响应结构：TBD（Service 返回值未在 Controller 中具体化）
- 错误码：`405` 权限不足

#### 4.3.6 商家订单明细
- URL：`GET /order/merchantOrderItems`
- 鉴权：是（仅商家 `type=1`）
- 查询参数：`orderId`
- 响应结构：`List<OrderItem>`
- 错误码：`500` 服务端异常

#### 4.3.7 用户确认收货
- URL：`PUT /order/confirmReceipt`
- 鉴权：是（仅用户 `type=2`）
- 查询参数：`orderId`
- 响应示例：
```json
{
  "code": "200",
  "msg": "success",
  "data": "TBD"
}
```
- 错误码：
  - `405` 权限不足
  - `500` 订单 ID 为空或服务端异常

### 4.4 购物车模块

#### 4.4.1 购物车列表
- URL：`GET /cart/list`
- 鉴权：是
- 查询参数：`pageNum`、`pageSize`
- 响应结构：`Page<CartVo>`

#### 4.4.2 加入购物车
- URL：`POST /cart/addcart`
- 鉴权：是
- 请求体：`Cart`（`userid` 由后端写入）
- 错误码：
  - `400` 数量不合法
  - `500` 服务端异常（未显式定义，基于 Result.error）

#### 4.4.3 更新购物车数量
- URL：`PUT /cart/updateCart`
- 鉴权：是
- 请求体：`Cart`（需包含 `id` 与 `quantity`）
- 错误码：
  - `400` 数量不合法
  - `405` 权限不足
  - `500` 修改失败

#### 4.4.4 删除购物车项
- URL：`DELETE /cart/delCart`
- 鉴权：是
- 查询参数：`cartId`
- 错误码：
  - `405` 权限不足
  - `500` 删除失败

### 4.5 物流模块

#### 4.5.1 商家发货
- URL：`POST /delivery/ship`
- 鉴权：是（仅商家 `type=1`）
- 请求体：`OrderDelivery`（使用 `orderId`、`expressCompany`、`expressNo`）
- 错误码：
  - `405` 权限不足
  - `500` 服务端异常

#### 4.5.2 商家物流列表
- URL：`GET /delivery/list`
- 鉴权：是（仅商家）
- 查询参数：`pageNum`、`pageSize`、`orderId`(可选)
- 响应结构：`Page<OrderDelivery>`
- 错误码：`405` 权限不足

#### 4.5.3 商家修正物流
- URL：`PUT /delivery/correct`
- 鉴权：是（仅商家）
- 请求体：`OrderDelivery`
- 错误码：
  - `400` 物流信息不完整
  - `405` 权限不足
  - `500` 服务端异常

#### 4.5.4 用户查看物流
- URL：`GET /delivery/getOne`
- 鉴权：是（仅用户 `type=2`）
- 查询参数：`orderId`
- 响应结构：`OrderDelivery`
- 错误码：
  - `405` 权限不足
  - `500` 服务端异常

### 4.6 公告模块

#### 4.6.1 用户/商家公告列表
- URL：`GET /notice/getNotice`
- 鉴权：是（用户/商家）
- 查询参数：`pageNum`、`pageSize`
- 响应结构：`IPage<Notice>`
- 错误码：`401` 未登录

#### 4.6.2 管理员公告列表
- URL：`GET /notice/list`
- 鉴权：是（仅管理员 `type=0`）
- 查询参数：`pageNum`、`pageSize`、`search`
- 响应结构：`Page<Notice>`
- 错误码：`405` 权限不足

#### 4.6.3 新增公告
- URL：`POST /notice/addNotice`
- 鉴权：是（仅管理员）
- 请求体：`Notice`（服务端写入 `date`）
- 错误码：
  - `405` 权限不足
  - `500` 添加失败

#### 4.6.4 删除公告
- URL：`DELETE /notice/deleteNotice`
- 鉴权：是（仅管理员）
- 查询参数：`id`
- 错误码：
  - `400` 公告 ID 为空
  - `405` 权限不足或删除失败

#### 4.6.5 更新公告
- URL：`PUT /notice/updateNotice`
- 鉴权：是（仅管理员）
- 请求体：`Notice`（服务端更新 `date`）
- 错误码：
  - `405` 权限不足
  - `406` 修改失败

### 4.7 统计模块

#### 4.7.1 商家统计数据
- URL：`GET /statistics/getAllStats`
- 鉴权：是（需登录；实际未校验类型）
- 响应结构：
  - `daily`：`List<DailySalesVO>`
  - `pie`：`List<ProductPieVO>`
- 错误码：`403` 未登录

## 5) 错误码汇总表

| 错误码 | 含义 | 备注 |
| --- | --- | --- |
| 200 | 成功 | - |
| 400 | 参数不合法 | 数量/公告 ID/物流信息 |
| 401 | 未登录 | JWT 不存在或无效 |
| 403 | 无权限 | 非角色允许 |
| 404 | 资源不存在 | 商品不存在 |
| 405 | 权限不足/操作失败 | 逻辑复用较多 |
| 406 | 修改失败 | 公告更新 |
| 500 | 服务器错误 | 业务异常 |
| 10086 | 用户名已被使用 | 注册 |
