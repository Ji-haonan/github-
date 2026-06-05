# 博客系统 (Blog System)

基于 Spring Boot 3 + MyBatis-Plus + Thymeleaf 的全栈博客系统，支持文章管理、分类标签、评论回复等功能。

## 技术栈

| 技术 | 版本 |
|------|------|
| Java | 17+ |
| Spring Boot | 3.2 |
| MyBatis-Plus | 3.5.5 |
| Thymeleaf | Spring Boot 内置 |
| MySQL | 8.0+ |
| Bootstrap | 5.3 |

## 功能特性

### 前台功能
- 文章列表展示（分页）
- 文章详情页（支持 HTML 富文本）
- 文章搜索（标题 + 摘要）
- 分类浏览
- 评论与嵌套回复
- 登录/注册

### 管理后台
- 文章管理（发布/草稿/编辑/删除）
- 分类管理（增删改）
- 评论管理（审核/删除）
- 数据统计（文章数/评论数）

### 技术特性
- BCrypt 密码加密
- Spring Security 表单认证
- 逻辑删除
- MyBatis-Plus 分页
- 响应式设计

## 项目结构

```
blog-system/
├── pom.xml                          # Maven 依赖配置
├── sql/schema.sql                   # 数据库初始化脚本
└── src/main/
    ├── java/com/blog/
    │   ├── BlogApplication.java     # Spring Boot 启动类
    │   ├── config/                  # 配置类 (Security/MyBatis-Plus)
    │   ├── entity/                  # 实体类 (User/Article/Category/Tag/Comment)
    │   ├── mapper/                  # MyBatis-Plus Mapper
    │   ├── service/                 # 业务逻辑层
    │   ├── controller/              # 控制器 (前台/后台)
    │   └── handler/                 # 全局异常处理
    └── resources/
        ├── application.yml          # 应用配置
        ├── mapper/ArticleMapper.xml # 自定义 SQL
        ├── static/css/style.css     # 样式文件
        └── templates/               # Thymeleaf 模板
            ├── index.html           # 首页
            ├── article/detail.html  # 文章详情
            ├── category.html        # 分类页
            ├── admin/               # 管理后台模板
            └── user/                # 登录/注册
```

## 快速开始

### 1. 环境准备

- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 2. 创建数据库

执行 SQL 初始化脚本：

```bash
mysql -u root -p < sql/schema.sql
```

脚本会自动：
- 创建数据库 `blog`
- 创建所有表结构
- 插入默认管理员账号：
  - 用户名：`admin`
  - 密码：`admin123`
- 插入默认分类（技术/生活/随笔）

### 3. 修改配置

编辑 `src/main/resources/application.yml`，根据本地 MySQL 配置修改连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root  # 你的 MySQL 用户名
    password: root  # 你的 MySQL 密码
```

### 4. 启动项目

```bash
mvn spring-boot:run
```

### 5. 访问系统

- **前台首页**：http://localhost:8080
- **管理后台**：http://localhost:8080/admin
- **登录页**：http://localhost:8080/login

## 数据库表结构

| 表名 | 说明 |
|------|------|
| `user` | 用户表 |
| `category` | 分类表 |
| `article` | 文章表 |
| `tag` | 标签表 |
| `article_tag` | 文章-标签关联表 |
| `comment` | 评论表 |

## 开发说明

### 新建文章
1. 登录后台账号（admin/admin123）
2. 访问 http://localhost:8080/admin/article/new
3. 填写文章信息并保存

### 技术亮点
- MyBatis-Plus 自动填充（create_time/update_time）
- 逻辑删除（deleted 字段）
- 评论支持无限嵌套回复
- 响应式设计，支持移动端

## 许可证

MIT License
