# 03 — DAO 方法规范文档（最终版）

## 1. 约定

| 规则 | 说明 |
|------|------|
| 统一入口 | 所有数据库操作通过 `DBHelper` 单例调用 |
| 线程 | Activity 中直接调用，不额外开线程 |
| 返回值 | 查询返回 `List<T>` 或 `T`（可为 null）；增删改返回 `long`(新ID) 或 `int`(影响行数) 或 `boolean`(成功/失败) |
| 空值处理 | 查询无结果返回空 List 或 null，不抛异常 |
| 日期格式 | 统一 `yyyy-MM-dd HH:mm:ss`；纯日期 `yyyy-MM-dd` |
| 事务 | 涉及多表写操作（预约+扣号源+创建订单）用事务包裹 |

---

## 2. 表-DAO 对照

| 表名 | DAO | 必须 |
|------|-----|:--:|
| t_user | UserDao | ✅ |
| t_department | DepartmentDao | ✅ |
| t_doctor | DoctorDao | ✅ |
| t_doctor_schedule | DoctorScheduleDao | ✅ |
| t_register_source | RegisterSourceDao | ✅ |
| t_appointment | AppointmentDao | ✅ |
| t_payment_order | PaymentOrderDao | ✅ |
| t_medical_card | MedicalCardDao | ✅ |
| t_medical_card_record | MedicalCardRecordDao | ✅ |
| t_visit_history | VisitHistoryDao | ✅ |
| t_message | MessageDao | ✅ |
| t_banner | BannerDao | ✅ |
| t_feedback | FeedbackDao | ✅ |
| t_admin_user | AdminUserDao | ✅ |
| t_symptom_department_rule | SymptomDepartmentRuleDao | 选做 |
| t_help_content | HelpContentDao | 选做 |

---

## 3. DAO 方法定义

### 3.1 UserDao — 用户

适用范围：功能14(登录注册)、功能15(个人中心)

```java
// 注册新用户，返回用户ID；手机号已存在返回-1
public long register(String phone, String password, String realName);

// 登录验证，成功返回User对象，失败返回null
public User login(String phone, String password);

// 根据ID查询用户
public User queryUserById(long userId);

// 更新用户信息（姓名、性别、生日等），返回影响行数
public int updateUser(User user);

// 修改密码
public int updatePassword(long userId, String oldPwd, String newPwd);

// 检查手机号是否已注册
public boolean isPhoneExist(String phone);
```

---

### 3.2 DepartmentDao — 科室

适用范围：功能2(科室列表)、功能21(后台维护科室)、功能7(智能导诊)

```java
// 查询全部科室，按sort_no升序
public List<Department> queryAllDepartments();

// 根据ID查科室
public Department queryDepartmentById(long deptId);

// 新增科室，返回新ID
public long insertDepartment(Department dept);

// 更新科室
public int updateDepartment(Department dept);

// 删除科室（需检查是否有医生关联）
public int deleteDepartment(long deptId);
```

---

### 3.3 DoctorDao — 医生

适用范围：功能2(医生列表)、功能3(医生简介)、功能22(后台维护医生)

```java
// 查询某科室下所有在职医生
public List<Doctor> queryDoctorsByDepartment(long deptId);

// 根据ID查医生详情
public Doctor queryDoctorById(long doctorId);

// 按姓名搜索医生
public List<Doctor> searchDoctors(String keyword);

// 新增医生
public long insertDoctor(Doctor doctor);

// 更新医生信息
public int updateDoctor(Doctor doctor);

// 删除医生（停用：status=0）
public int disableDoctor(long doctorId);
```

---

### 3.4 DoctorScheduleDao — 医生排班

适用范围：功能3(排班展示)、功能23(后台维护排班)

```java
// 查询某医生某日的排班（上/下午）
public List<DoctorSchedule> querySchedulesByDoctor(long doctorId, String date);

// 查询某日期某科室所有医生的排班
public List<DoctorSchedule> querySchedulesByDeptAndDate(long deptId, String date);

// 根据ID查排班
public DoctorSchedule queryScheduleById(long scheduleId);

// 新增排班
public long insertSchedule(DoctorSchedule schedule);

// 编辑排班
public int updateSchedule(DoctorSchedule schedule);

// 停诊
public int cancelSchedule(long scheduleId);
```

---

### 3.5 RegisterSourceDao — 号源（20分钟段）

适用范围：功能3(号源展示)、功能4(预约挂号)、功能24(后台维护号源)

```java
// 查询某排班下的所有20分钟号源段
public List<RegisterSource> querySourcesBySchedule(long scheduleId);

// 根据ID查号源
public RegisterSource querySourceById(long sourceId);

// 扣减号源（带乐观锁），成功返回true，已满返回false
public boolean decreaseRemainNum(long sourceId, int currentVersion);

// 恢复号源（取消预约时）
public void increaseRemainNum(long sourceId);

// 批量生成20分钟号源（排班创建时自动调用）
public void generateSlots(long scheduleId, String startTime, String endTime);

// 编辑号源
public int updateSource(RegisterSource source);
```

---

### 3.6 AppointmentDao — 预约

适用范围：功能4(预约挂号)、功能16(预约记录)、功能19(医生查看预约)

```java
/**
 * 创建预约（事务包裹）：
 * 1. 扣减号源 remain_num（乐观锁）
 * 2. 插入预约记录
 * 3. 生成消息通知
 * 返回预约ID；号源不足返回-1
 */
public long createAppointment(Appointment appointment);

// 查询用户全部预约，按时间倒序
public List<Appointment> queryAppointmentsByUser(long userId);

// 按状态筛选预约
public List<Appointment> queryAppointmentsByStatus(long userId, String status);

// 查预约详情（含号源、排班、医生、科室信息）
public Appointment queryAppointmentDetail(long appointmentId);

/**
 * 取消预约（事务包裹）：
 * 1. 状态→CANCELED
 * 2. 恢复号源
 * 3. 更新支付订单状态
 */
public boolean cancelAppointment(long appointmentId, String reason);

// 将预约状态改为VISITED（就诊完成）
public int completeAppointment(long appointmentId);

// 将预约状态改为EXPIRED（过期未就诊）
public int expireAppointment(long appointmentId);

// 医生端：查某医生名下的预约
public List<Appointment> queryAppointmentsByDoctor(long doctorId);
```

---

### 3.7 PaymentOrderDao — 缴费订单

适用范围：功能5(自助缴费)、功能26(模拟支付)

```java
// 创建缴费订单，返回订单ID
public long createOrder(PaymentOrder order);

// 根据预约ID查订单
public PaymentOrder queryOrderByAppointment(long appointmentId);

// 模拟支付成功：状态→PAID，记录支付时间
public int payOrder(long orderId, String payChannel);

// 取消订单
public int cancelOrder(long orderId);
```

---

### 3.8 MedicalCardDao — 医保卡

适用范围：功能27(模拟绑定医保卡)

```java
// 查询用户绑定的医保卡，未绑定返回null
public MedicalCard queryByUserId(long userId);

// 绑定医保卡，返回记录ID
public long bind(MedicalCard card);

// 解绑（bind_status→UNBOUND）
public int unbind(long userId);

// 更新余额
public int updateBalance(long userId, double newBalance);
```

---

### 3.9 MedicalCardRecordDao — 医保消费记录

适用范围：功能27(医保卡-消费记录)

```java
// 查询某医保卡的消费记录，按时间倒序
public List<MedicalCardRecord> queryByCardId(long cardId);

// 新增消费记录
public long insert(MedicalCardRecord record);
```

---

### 3.10 VisitHistoryDao — 就诊历史

适用范围：功能11(健康档案-就诊历史)

```java
// 查用户全部就诊历史，按就诊时间倒序
public List<VisitHistory> queryByUserId(long userId);

// 查某次预约对应的就诊历史
public VisitHistory queryByAppointmentId(long appointmentId);

// 新增就诊历史（预约完成后生成）
public long insert(VisitHistory history);

// 手动录入就诊历史（后台）
public long insertManual(VisitHistory history);
```

---

### 3.11 MessageDao — 消息

适用范围：功能6(消息中心)

```java
// 查询用户全部消息，按时间倒序
public List<Message> queryByUserId(long userId);

// 查询未读消息数
public int queryUnreadCount(long userId);

// 标记单条已读
public int markAsRead(long messageId);

// 全部标记已读
public int markAllAsRead(long userId);

// 新增消息（系统触发时调用）
public long insert(Message message);
```

---

### 3.12 BannerDao — 轮播图

适用范围：功能1(首页轮播图)、功能25(后台维护轮播图)

```java
// 查询当前有效的轮播图，按sort_no升序
public List<Banner> queryActiveBanners();

// 管理员查全部
public List<Banner> queryAllBanners();

// 新增轮播图
public long insert(Banner banner);

// 更新轮播图
public int update(Banner banner);

// 删除轮播图
public int delete(long bannerId);
```

---

### 3.13 FeedbackDao — 满意度评价

适用范围：功能17(满意度调查)

```java
// 查医生收到的评价列表
public List<Feedback> queryByDoctorId(long doctorId);

// 查某预约对应的评价
public Feedback queryByAppointmentId(long appointmentId);

// 提交评价（三维评分）
public long insert(Feedback feedback);
```

---

### 3.14 AdminUserDao — 后台账号

适用范围：功能18-25(后台管理端登录)

```java
// 登录验证，返回AdminUser对象，失败返回null
public AdminUser login(String username, String password);

// 根据ID查询
public AdminUser queryById(long adminId);

// 根据医生ID查对应的医生端账号
public AdminUser queryByDoctorId(long doctorId);
```

---

### 3.15 SymptomDepartmentRuleDao — 智能导诊（选做）

适用范围：功能7(智能导诊)

```java
// 根据症状关键词匹配科室列表，按sort_no升序
public List<SymptomDepartmentRule> queryBySymptom(String keyword);
```

---

### 3.16 HelpContentDao — 帮助内容（选做）

适用范围：功能15(使用帮助/隐私政策/挂号流程)

```java
// 根据内容类型查询
public List<HelpContent> queryByType(String contentType);
```

---

## 4. 方法汇总

| DAO | 方法数 | 对应功能 |
|-----|:--:|------|
| UserDao | 6 | 登录注册、个人中心 |
| DepartmentDao | 5 | 科室列表、后台维护 |
| DoctorDao | 6 | 医生列表、搜索、后台维护 |
| DoctorScheduleDao | 6 | 排班展示、后台维护 |
| RegisterSourceDao | 6 | 号源管理、预约扣减 |
| AppointmentDao | 8 | 预约挂号、预约记录 |
| PaymentOrderDao | 4 | 缴费、模拟支付 |
| MedicalCardDao | 4 | 医保卡绑定 |
| MedicalCardRecordDao | 2 | 医保消费记录 |
| VisitHistoryDao | 4 | 健康档案-就诊历史 |
| MessageDao | 5 | 消息中心 |
| BannerDao | 5 | 轮播图管理 |
| FeedbackDao | 3 | 满意度评价 |
| AdminUserDao | 3 | 后台登录 |
| SymptomDepartmentRuleDao | 1 | 智能导诊(选做) |
| HelpContentDao | 1 | 帮助内容(选做) |
| **合计** | **69** | |

---

## 5. 核心事务流程

### 5.1 预约挂号

```
createAppointment() {
    db.beginTransaction();
    try {
        1. RegisterSourceDao.decreaseRemainNum(sourceId, version); // 乐观锁扣号源
           if (!decreaseRemainNum) return -1;                      // 号源不足，事务在 finally 中回滚
        2. 插入预约记录到数据库                                   // 通过 db.insert() 写入 t_appointment 表
        3. PaymentOrderDao.createOrder(order);                      // 生成缴费单
        4. MessageDao.insert(message);                              // 发送预约成功消息
        db.setTransactionSuccessful();
    } finally {
        db.endTransaction();
    }
}
```

### 5.2 取消预约

```
cancelAppointment() {
    db.beginTransaction();
    try {
        1. AppointmentDao → status = CANCELED
        2. RegisterSourceDao.increaseRemainNum(sourceId);  // 恢复号源
        3. PaymentOrderDao → status = CANCELED             // 取消缴费单
        4. MessageDao.insert(message);                     // 发送取消通知
        db.setTransactionSuccessful();
    } finally {
        db.endTransaction();
    }
}
```
