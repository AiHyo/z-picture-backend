# Z-Picture 图片管理系统

<div align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.7.6-brightgreen" alt="Spring Boot">
  <img src="https://img.shields.io/badge/JDK-1.8+-blue" alt="JDK">
  <img src="https://img.shields.io/badge/MySQL-5.7+-orange" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-6.0+-red" alt="Redis">
  <img src="https://img.shields.io/badge/License-MIT-lightgrey" alt="License">
</div>

## 项目简介
分为公共图库、私有图库和团队共享图库。

①用户可以在公共图库中上传‌并检索图片，也可以将图片上传至私有空间进行批量管⁡理、多维检索、编辑和分析等；可以开通团队空间‍并邀请成员，共享和实时协同编辑图片。

②管理员额外可以在公共图库中爬虫批量上传、审核、管理图片。

## 项目特性

- **多空间管理**：支持用户创建多个独立图片空间，实现图片资源分层管理
- **权限控制**：基于Sa-Token实现细粒度的RBAC权限控制，保障数据安全
- **高性能架构**：采用Redis+Caffeine多级缓存、异步处理技术，确保系统在大数据量下的稳定运行
- **AI绘图集成**：集成阿里云百炼大模型，支持智能图片生成和扩展
- **多维度图片搜索**：支持通过标签、颜色、相似度等多种方式检索图片
- **数据抓取功能**：基于Jsoup实现网络图片批量抓取，丰富图片库资源
- **协作编辑**：基于WebSocket实现多人实时协作编辑图片功能

## 技术栈

- **后端框架**：Spring Boot 2.7.6
- **数据库**：MySQL + MyBatis-Plus + MyBatis X
- **缓存系统**：Redis(分布式缓存) + Caffeine(本地缓存)
- **数据抓取**：Jsoup
- **对象存储**：腾讯云COS对象存储
- **权限控制**：Sa-Token
- **并发处理**：
  - JUC并发和异步编程
  - Disruptor高性能无锁队列
- **双向通信**：WebSocket

## 核心功能

1. **用户管理**
   - 用户注册（MD5+盐值加密）
   - 用户登录（Redis+Spring Session）
   - 基于注解的权限校验

2. **空间管理**
   - 创建和管理多个图片空间
   - 空间成员协作和权限控制
   - 空间数据分析和统计

3. **图片管理**
   - 支持本地文件和URL上传图片
   - 图片标签和分类管理
   - 图片编辑和批量操作

4. **AI绘图功能**
   - 集成阿里云百炼大模型
   - 图片扩展(Out-Painting)

5. **高级搜索**
   - 颜色搜索（基于欧氏距离算法）
   - 以图搜图（调用百度以图搜图API）
   - 多维度组合搜索

6. **协作功能**
   - 基于WebSocket的实时协作
   - 编辑锁机制避免冲突
   - 消息的异步处理

## 系统架构

- 采用分层架构，实现业务逻辑和技术实现的解耦
- 使用AOP切面技术实现日志、权限等横切关注点
- 实现多级缓存策略，提升系统性能
- 采用异步处理机制，优化用户体验和系统吞吐量

## 安装和部署

### 环境要求

- JDK 1.8+
- MySQL 5.7+
- Redis 6.0+
- Maven 3.6+

### 快速开始

1. 克隆项目

```bash
git clone https://github.com/AiHyo/z-picture-backend.git
cd z-picture-backend
```

2. 配置数据库

```sql
# 创建数据库
CREATE DATABASE z_picture DEFAULT CHARACTER SET utf8mb4;
```

3. 修改配置

需要修改`application.yml`中的配置，将占位符替换为您自己的配置：

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/z_picture
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379

# 对象存储配置（需要从腾讯云获取）
cos:
  client:
    host: your_host
    secretId: your_secretId
    secretKey: your_secretKey
    region: your_region
    bucket: your_bucket

# AI接口配置
aliYunAi:
  apiKey: your_apiKey
```

4. 编译运行

```bash
mvn clean package
java -jar target/z-picture-backend-0.0.1-SNAPSHOT.jar
```

5. 访问API文档

```
http://localhost:8123/api/doc.html
```

## 项目优化亮点

- **接口规范化**：
  - 统一响应封装，标准化接口数据格式
  - 基于@RestControllerAdvice的全局异常处理
  - 使用Knife4j自动生成接口文档，提高可读性

- **性能优化**：
  - Redis+Caffeine多级缓存，降低接口响应时间400%
  - 通过随机过期时间策略降低缓存雪崩风险
  - 使用@Async注解实现异步处理耗时操作

- **成本优化**：
  - 腾讯云COS对象存储+数据万象，实现图片Webp转码和缩略图生成
  - 30天未访问图片自动降频存储，节约存储成本

- **安全优化**：
  - 基于Sa-Token实现多账号体系的RBAC权限控制
  - MD5+盐值加密存储用户密码，防止明文泄露
  - AOP切面实现统一权限校验，确保安全访问

- **高并发处理**：
  - 使用Disruptor无锁队列处理WebSocket消息，提升系统吞吐量
  - 基于分段锁+事务模板实现空间创建服务
  - CompletableFuture实现批量上传并发处理，性能提升300%

- **代码质量**：
  - 模板方法模式统一封装图片上传流程
  - 门面模式组合API调用，简化客户端使用
  - 通过@Lazy注解解决循环依赖问题

## 项目展望

- 支持更多AI绘图模型的集成
- 增强图片识别和智能分类功能
- 开发移动端应用，提供更便捷的访问方式
- ……

