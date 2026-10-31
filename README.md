# dcloud-interview

基于 Spring Boot 与 Spring Cloud Alibaba 的 AI 面试微服务骨架。父工程统一管理版本，业务服务通过 `common` 复用基础设施依赖。当前仓库已完成多模块 POM 搭建，业务代码与配置尚未落地。

## 技术基线

| 组件 | 版本 |
| --- | --- |
| JDK | 21 |
| Spring Boot | 3.5.3 |
| Spring Cloud | 2025.0.0 |
| Spring Cloud Alibaba | 2023.0.3.2 |
| Spring AI | 1.1.0-M3 |
| Spring AI Alibaba | 1.1.0.0-M4 |
| MyBatis-Plus | 3.5.6 |
| MySQL Connector | 8.0.27 |
| Nacos Client | 3.1.0 |
| Hutool | 5.8.27 |
| Fastjson2 | 2.0.42 |
| JJWT | 0.12.3 |
| Knife4j | 4.4.0 |

`common` 已引入 Web、OpenFeign、LoadBalancer、Nacos Discovery、MyBatis-Plus、MySQL、Redis、RabbitMQ、JWT。Spring AI、Knife4j 的版本已在父 POM 锁定，各业务模块尚未引用。

## 模块

| 模块 | 职责 |
| --- | --- |
| `common` | 公共依赖，以及后续的配置、常量、异常、模型、工具类、DTO |
| `gateway` | API 网关 |
| `account-service` | 账户服务，依赖 `common` |
| `ai-business-service` | AI 面试相关能力，依赖 `common` |
| `product-service` | 商品服务，依赖 `common` |

## 工程结构

```text
dcloud-interview/
├── pom.xml                 # 父工程，聚合模块并锁定依赖版本
├── common/                 # 公共模块（jar，不打可执行包）
│   └── pom.xml
├── gateway/                # 网关服务
│   └── pom.xml
├── account-service/        # 账户服务
│   └── pom.xml
├── ai-service/             # AI 服务
│   └── pom.xml
├── product-service/        # 商品服务
│   └── pom.xml
└── README.md
```

`common` 计划中的包路径为 `net.xdclass.common`，按 `config`、`constant`、`exception`、`model`、`util`、`dto` 划分。这些目录和各服务的启动类、配置文件都还未创建。

## 环境

- JDK 21
- Maven 3.9+
- 后续联调需要：Nacos、MySQL 8、Redis、RabbitMQ

## 构建

在仓库根目录执行：

```bash
mvn clean install -DskipTests
```

`common` 配置了 `spring-boot-maven-plugin` 的 `skip=true`，只作为依赖 jar 安装到本地仓库。业务服务继承父工程的 `repackage`，后续补上启动类后可独立运行。

## 后续

1. 补齐各服务的启动类与 `application.yml`。
2. 在 `common` 中落地公共模型、异常与工具类。
3. 为 `gateway` 接入 Spring Cloud Gateway，并完成 Nacos 注册与路由。
