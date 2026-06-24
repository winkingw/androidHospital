# 04 — Git 协作规范

## 1. 分支策略

```
main ─────────────────────────────────────────────
  │
  └── develop ───────────────────────────────────
        │
        ├── feature/01-home              (队员A)
        ├── feature/02-health-admin        (队员B)
        └── feature/03-profile             (队员C)
```

| 分支 | 用途 | 谁维护 | 规则 |
|------|------|--------|------|
| `main` | 稳定版本，可演示可交作业 | 组长 | **禁止直接 push**，只从 develop 合并 |
| `develop` | 开发主线，各组员代码的汇集点 | 组长 | 禁止直接 push，只从 feature 合并 |
| `feature/xxx` | 个人开发分支 | 组员 | 一人一条，开发完合并到 develop 后删除 |

### 分支命名

```
feature/编号-功能描述
例如：
  feature/01-home
  feature/02-health-admin
  feature/03-profile
```

---

## 2. Commit 格式

```
<type>: <简短描述>

例如：
  feat: 实现登录注册功能
  fix: 修复预约号源扣减不同步的问题
  docs: 更新数据库设计文档
```

### type 分类

| type | 含义 | 示例 |
|------|------|------|
| `feat:` | 新功能 | `feat: 添加科室列表页面` |
| `fix:` | 修 bug | `fix: 取消预约后号源未恢复` |
| `docs:` | 文档/注释 | `docs: 补充 DAO 方法注释` |
| `style:` | 代码格式（空格、缩进） | `style: 统一缩进为4空格` |
| `refactor:` | 重构（不提新功能、不修 bug） | `refactor: 抽取公共 DBHelper` |
| `test:` | 测试 | `test: 测试用户注册登录流程` |

### 规则

- 描述用中文，**50 字以内**
- 一次 commit 只做一件事
- **禁止** `fix bug`、`update` 这种模糊描述
- 写完一个功能点就 commit，不要攒几天一起交

---

## 3. 日常开发流程

```
每人每天开始：
  1. git checkout develop
  2. git pull origin develop        # 拉最新代码
  3. git checkout feature/你的分支
  4. git merge develop              # 同步主分支的变更到自己的分支
  5. 开始写代码

写完一个功能点：
  1. git add <具体文件名>
  2. git commit -m "feat: xxx"
  3. git push origin feature/你的分支
  4. 在 GitHub 上创建 Pull Request → develop
  5. 通知组长 review

禁止：
  ✗ git add . 或 git add -A（一把梭，容易把 .class / .apk 提交进去）
  ✗ git push --force
  ✗ 直接在 develop 分支上写代码
  ✗ commit 里包含密码、API Key 等敏感信息
```

---

## 4. Pull Request 流程

```
提交 PR 时，写清楚：
  ┌─────────────────────────────────┐
  │ 标题：feat: 添加预约挂号功能       │
  │                                 │
  │ 改动内容：                       │
  │ - 新增 AppointmentActivity      │
  │ - 新增 DoctorListAdapter        │
  │ - AppointmentDao 新增 cancel 方法│
  │                                 │
  │ 涉及文档变更：无                  │
  │                                 │
  │ 测试：                          │
  │ - [x] 选择科室 → 医生的流程正常   │
  │ - [x] 号源不足时提示正确         │
  │ - [x] 取消预约后号源恢复         │
  └─────────────────────────────────┘
```

| 步骤 | 谁做 |
|------|------|
| 发起 PR | 组员 |
| Review 代码 | 组员互查（至少一人点通过） |
| 合并到 develop | 组长 |
| 合并后删 feature 分支 | 组长或组员自己删 |

---

## 5. 冲突解决

```
出现冲突时：
  1. git checkout develop && git pull
  2. git checkout feature/你的分支
  3. git merge develop          # 会提示 CONFLICT
  4. 打开冲突文件，搜 <<<<<<<  找到冲突位置
  5. 和冲突的组员沟通，决定保留谁的代码
  6. 删除 <<<<<<<  ======= >>>>>>> 标记
  7. git add 冲突文件 && git commit -m "fix: 合并develop冲突"
  8. git push
```

**防冲突最佳实践**：每个人只改自己负责的包，改之前先 merge develop。

---

## 6. .gitignore 检查清单

确保以下内容**不会被提交**（仓库已有 `.gitignore` 覆盖）：

| 不该提交的 | 原因 |
|-----------|------|
| `*.apk` `*.aab` | 编译产物，不是源码 |
| `build/` | Gradle 输出目录 |
| `.idea/` | IDE 个人配置，每人不同 |
| `local.properties` | 含本机 SDK 路径 |
| `*.iml` | IDE 模块文件 |
| `*.jks` `*.keystore` | 签名密钥，泄露有安全风险 |

---

## 7. 分工协作表（模板，后续填写）

| 组员 | 负责模块 | feature 分支 | 功能编号 |
|------|---------|-------------|----------|
| 组员A | 首页 + 公共模块(数据库/工具类/模拟数据) | `feature/01-home` | 1,2,3,4,6,7,8,9 |
| 组员B | 健康档案 + 后台管理 | `feature/02-health-admin` | 11,12,13,18,19,20,21,22,23,24,25 |
| 组员C | 个人中心 + 缴费+二维码+支付 | `feature/03-profile` | 5,10,14,15,16,17,26,27 |

> 上表为建议分工，最终由组长确认后更新。
>
> **备注**：以 **07-任务分工表.md** 为最终分工方案，本表内容已同步对齐。
