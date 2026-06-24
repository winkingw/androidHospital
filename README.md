# Serene Health -- 医院线上挂号预约 APP

## 1. 项目简介

Serene Health 是一款基于 Android 平台的医院线上挂号预约应用，支持科室医生浏览、在线预约挂号、缴费支付、健康档案管理、后台管理等功能，所有数据使用本地 SQLite 数据库模拟，无需网络后端。

### 技术栈

| 项 | 选型 |
|------|------|
| 开发语言 | Java |
| UI 框架 | XML + ViewBinding |
| 架构模式 | MVC（Activity 即 Controller） |
| 数据库 | SQLite + SQLiteOpenHelper |
| 图片加载 | Glide |
| 页面结构 | Activity + Fragment（底部导航切换） |
| 网络 | 无（所有数据本地模拟） |
| IDE | Android Studio |
| 最低 SDK | API 24 (Android 7.0) |
| 目标 SDK | API 34 (Android 14) |

---

## 2. 快速开始

### 克隆仓库

```bash
git clone https://github.com/winkingw/androidHospital.git
```

### 用 Android Studio 打开

1. 打开 Android Studio，选择 **File -> Open**。
2. 导航到克隆的 `androidHospital` 文件夹，点击 OK。
3. 等待 Gradle 同步完成（首次打开可能需要下载依赖，耗时数分钟）。
4. 确保已创建模拟器（推荐 Pixel 系列，API 34）。

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更新版本
- JDK 17
- Gradle 8.2+
- AGP (Android Gradle Plugin) 8.2+

### 运行项目

直接点击 **Run** 按钮（绿色三角形）即可。首次启动应用时，`MockDataUtil` 会自动初始化模拟数据到 SQLite 数据库，无需额外操作。

---

## 3. 项目结构

```
androidHospital/
├── docs/                                        # 项目规范文档（7份）
│   ├── 01-技术栈选型与架构规范.md
│   ├── 02-数据库设计文档.md
│   ├── 03-DAO方法规范文档.md
│   ├── 04-Git协作规范.md
│   ├── 05-代码风格规范.md
│   ├── 06-模拟数据规格书.md
│   └── 07-任务分工表.md
│
├── app/src/main/java/com/serenehealth/
│   ├── bean/                                    # 实体 Bean（16个）
│   │   ├── User.java                 # 用户/就诊人
│   │   ├── Department.java           # 科室
│   │   ├── Doctor.java               # 医生
│   │   ├── DoctorSchedule.java       # 医生排班
│   │   ├── RegisterSource.java       # 号源
│   │   ├── Appointment.java          # 预约记录
│   │   ├── PaymentOrder.java         # 缴费订单
│   │   ├── MedicalCard.java          # 医保卡
│   │   ├── MedicalCardRecord.java    # 医保消费明细
│   │   ├── VisitHistory.java         # 就诊历史
│   │   ├── Message.java              # 消息通知
│   │   ├── Banner.java               # 轮播图
│   │   ├── Feedback.java             # 满意度评价
│   │   ├── AdminUser.java            # 后台账号
│   │   ├── SymptomDepartmentRule.java # 症状-科室规则（选做）
│   │   └── HelpContent.java          # 帮助内容（选做）
│   │
│   ├── db/                                      # 数据库层
│   │   ├── DBHelper.java             # 数据库管理（建表+升级）
│   │   ├── UserDao.java              # 用户 DAO
│   │   ├── DepartmentDao.java        # 科室 DAO
│   │   ├── DoctorDao.java            # 医生 DAO
│   │   ├── DoctorScheduleDao.java    # 排班 DAO
│   │   ├── RegisterSourceDao.java    # 号源 DAO
│   │   ├── AppointmentDao.java       # 预约 DAO
│   │   ├── PaymentOrderDao.java      # 缴费 DAO
│   │   ├── MedicalCardDao.java       # 医保卡 DAO
│   │   ├── MedicalCardRecordDao.java # 医保消费 DAO
│   │   ├── VisitHistoryDao.java      # 就诊历史 DAO
│   │   ├── MessageDao.java           # 消息 DAO
│   │   ├── BannerDao.java            # 轮播图 DAO
│   │   ├── FeedbackDao.java          # 评价 DAO
│   │   └── AdminUserDao.java         # 后台账号 DAO
│   │
│   ├── activity/                                # Activity（待开发）
│   ├── fragment/                                # Fragment（待开发）
│   ├── adapter/                                 # RecyclerView 适配器（待开发）
│   │
│   └── util/                                    # 工具类
│       ├── MockDataUtil.java         # 模拟数据初始化
│       └── SPUtil.java               # SharedPreferences 工具
│
├── app/src/main/res/                            # 资源文件
│   ├── layout/                       # XML 布局
│   ├── drawable/                     # 图片/形状资源
│   ├── values/                       # colors/strings/themes
│   └── ...
│
└── .gitignore                                   # Git 忽略规则
```

---

## 4. 开发规范

`docs/` 目录下包含 7 份规范文档。每个组员在开始写代码之前，**必须先阅读以下对应文档**：

| 文档 | 阅读目的 |
|------|---------|
| `docs/01-技术栈选型与架构规范.md` | 了解 MVC 分层约定、包结构、命名规则 |
| `docs/02-数据库设计文档.md` | 了解 16 张表的字段定义、ER 关系、建表 SQL |
| `docs/03-DAO方法规范文档.md` | 查看每个 DAO 有哪些可用方法、参数和返回值 |
| `docs/04-Git协作规范.md` | 掌握分支策略、Commit 格式、PR 流程、冲突解决 |
| `docs/05-代码风格规范.md` | 统一缩进、命名、注释等 Java 代码风格 |
| `docs/06-模拟数据规格书.md` | 了解初始化到数据库中的模拟数据内容 |
| `docs/07-任务分工表.md` | 确认自己负责的功能列表和交付物 |

### 建议阅读顺序

1. 先读 **01**（整体架构）和 **07**（自己负责的功能）
2. 再读 **02**（数据库）和 **03**（DAO 方法）
3. 然后读 **06**（模拟数据）
4. 写代码前回顾 **04**（Git）和 **05**（代码风格）

---

## 5. 使用 AI 辅助开发指南

本项目的规范文档和公共模块代码结构清晰，适合使用 Claude Code 或其他 AI 工具辅助开发。建议按以下步骤操作：

### 5.1 启动 AI 对话

在项目根目录（`androidHospital/`）打开终端，启动 AI 工具（如 Claude Code）。

### 5.2 让 AI 阅读项目上下文

在首次对话中，要求 AI 先阅读项目规范文档和现有代码。示例：

> 请先阅读以下文件，了解项目规范：
> - `docs/02-数据库设计文档.md` -- 数据库表结构
> - `docs/03-DAO方法规范文档.md` -- DAO 方法定义
> - `docs/06-模拟数据规格书.md` -- 模拟数据内容
> - `docs/07-任务分工表.md` -- 任务分工
> - `app/src/main/java/com/serenehealth/db/` 下的所有 DAO 文件
> - `app/src/main/java/com/serenehealth/bean/` 下的所有 Bean 文件
> - `app/src/main/java/com/serenehealth/util/MockDataUtil.java`
> - `app/src/main/java/com/serenehealth/util/SPUtil.java`

### 5.3 告诉 AI 你的角色和功能编号

明确告知 AI 你的队员身份和负责的功能编号，让 AI 生成对应代码。

**队员A 参考提示词：**

```
我是队员A，负责首页模块（功能1,2,3,4,6,7,8,9）。
请先阅读 docs/ 下的规范文档和 bean/ db/ util/ 下的现有代码。
然后帮我开发功能1：首页轮播图和功能宫格入口，在 HomeFragment 中实现。
```

**队员B 参考提示词：**

```
我是队员B，负责健康档案 + 后台管理端（功能11,12,13,18,19,20,21,22,23,24,25）。
请先阅读 docs/ 下的规范文档和 bean/ db/ util/ 下的现有代码。
然后帮我开发功能11：健康档案页面，包含报告、处方、病历三个页签。
```

**队员C 参考提示词：**

```
我是队员C，负责个人中心模块（功能5,10,14,15,16,17,26,27）。
请先阅读 docs/ 下的规范文档和 bean/ db/ util/ 下的现有代码。
然后帮我开发功能14：用户登录注册页面。
```

### 5.4 注意事项

- **每个队员开启独立的 AI 对话会话**，避免上下文互相干扰。
- 每次让 AI 生成代码后，仔细检查代码是否符合规范文档的要求（命名、分层、DAO 调用方式等）。
- 公共模块的代码（Bean、DAO、DBHelper、工具类）已经完成，AI 应该直接使用现有 DAO 方法，不要重复生成。

---

## 6. Git 工作流

### 分支策略

```
main                    -- 稳定版本，可演示可交作业（仅组长合并）
  └── develop           -- 开发主线（仅组长合并）
        ├── feature/01-home          (队员A)
        ├── feature/02-health-admin  (队员B)
        └── feature/03-profile       (队员C)
```

- `main`：禁止直接 push，只从 `develop` 合并
- `develop`：禁止直接 push，只从 `feature` 分支合并
- `feature/xxx`：每人一条自己的开发分支

### Commit 格式

```
<type>: <简短描述（50字内，中文）>

示例：
  feat: 实现登录注册功能
  fix: 修复预约号源扣减不同步的问题
  docs: 更新数据库设计文档
  style: 统一缩进为4空格
  refactor: 抽取公共 DBHelper
```

### 日常开发流程

```
每天开始：
  1. git checkout develop
  2. git pull origin develop
  3. git checkout feature/你的分支
  4. git merge develop
  5. 开始写代码

写完一个功能点：
  1. git add <具体文件名>      # 禁止 git add .
  2. git commit -m "feat: xxx"
  3. git push origin feature/你的分支
  4. 在 GitHub 上创建 Pull Request -> develop
  5. 通知组长 review
```

### 冲突解决

merge develop 时遇到冲突，打开冲突文件搜索 `<<<<<<<` 定位冲突位置，与冲突的组员沟通后保留正确代码，删除冲突标记后提交。

---

## 7. 当前进度

| 模块 | 进度 | 负责人 |
|------|:----:|--------|
| 规范文档（7份） | 已完成 | 组长 |
| 公共模块（DBHelper + Bean + DAO + 工具类） | 已完成 | 组长/队员A |
| 首页模块（功能1,2,3,4,6,7,8,9） | 待开发 | 队员A |
| 健康档案 + 后台管理（功能11,12,13,18,19,20,21,22,23,24,25） | 待开发 | 队员B |
| 个人中心 + 缴费（功能5,10,14,15,16,17,26,27） | 待开发 | 队员C |

---

## 8. 队员信息

| 角色 | 姓名 | 负责模块 | feature 分支 |
|------|------|---------|-------------|
| 组长（兼队员A） | （待填） | 首页 + 公共模块 | `feature/01-home` |
| 队员B | （待填） | 健康档案 + 后台管理 | `feature/02-health-admin` |
| 队员C | （待填） | 个人中心 + 缴费 + 二维码 + 支付 | `feature/03-profile` |

> 队员信息由组长确认后填写。

---

## 9. 许可证

本项目为课程实践项目，仅用于学习交流。
