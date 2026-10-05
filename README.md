# Camunda + Drools + DMN 集成演示项目

基于 **Spring Boot 3.5.16 + Camunda 7.24.0 + Drools 10.2.0** 的开箱即用示例，演示工作流中同时使用：

- **Drools DRL 规则引擎**：复杂推理、链式风控规则
- **Camunda 原生 DMN**：决策表，业务人员可维护
- **BPMN 工作流**：串联整个审批流程


## 🚀 快速启动

### 环境要求
- JDK 17+
- Maven 3.8+

### 方式一：Maven 直接运行（推荐）
```bash
mvn spring-boot:run
```
pom 中已配置 `-Dfile.encoding=UTF-8`，可直接启动。

### 方式二：打包运行
```bash
mvn -DskipTests package
java -Dfile.encoding=UTF-8 -jar target/camunda-drools-dmn-demo-1.0.0.jar
```
> `-Dfile.encoding=UTF-8` 不能省。容器（Docker/K8s）的 locale 常为 POSIX，此时 JVM 默认字符集退化成 ASCII，Drools 读取 DRL 里的中文会变成乱码。

### 访问地址
- Camunda 控制台：http://localhost:8080/camunda ，账号 `admin / admin`
- 测试接口：http://localhost:8080/api/loan/apply

## 🧪 接口测试

`POST http://localhost:8080/api/loan/apply`，以下三个用例均已实测通过。

**用例 1：优质客户自动通过**
```json
{
  "applicantName": "张三",
  "creditScore": 780,
  "annualIncome": 600000,
  "loanAmount": 400000,
  "loanTerm": 36,
  "debtRatio": 0.2,
  "hasGuarantor": false,
  "hasCollateral": true
}
```
实测结果：`approve=true`，等级 A，利率 4.200%

**用例 2：自动拒绝**
```json
{
  "applicantName": "李四",
  "creditScore": 450,
  "annualIncome": 100000,
  "loanAmount": 1500000,
  "loanTerm": 36,
  "debtRatio": 0.85,
  "hasGuarantor": false,
  "hasCollateral": false
}
```
实测结果：`approve=false`，拒绝原因「负债比超过70%，还款能力不足」

**用例 3：转人工审核**
```json
{
  "applicantName": "王五",
  "creditScore": 600,
  "annualIncome": 400000,
  "loanAmount": 300000,
  "loanTerm": 60,
  "debtRatio": 0.5,
  "hasGuarantor": false,
  "hasCollateral": false
}
```
实测结果：`approve=null`，等级 D，提示「未命中自动审批规则，转人工审核」

## 📁 项目结构
```
src/main/java/com/example/camundadrools/
├── CamundaDroolsApplication.java    # 启动类
├── config/
│   └── DroolsConfig.java            # Drools 配置（只暴露 KieContainer）
├── delegate/
│   └── DroolsRiskCheckDelegate.java # 规则执行 Delegate
├── model/
│   └── LoanApplication.java         # 贷款申请实体（需实现 Serializable）
└── controller/
    └── LoanController.java          # 测试接口

src/main/resources/
├── application.yml                  # 配置文件
├── META-INF/kmodule.xml             # Drools KBase 配置
├── rules/loanRisk.drl               # 风控规则（8 条）
├── bpmn/loanApproval.bpmn           # BPMN 审批流程
└── dmn/maxLoanAmount.dmn            # DMN 额度决策表
```

## 📌 核心规则说明

8 条规则按 salience 优先级执行：

1. **拒绝类**
   - 负债比 > 70% 拒绝（salience 100）
   - 信用分 < 400 黑名单拒绝（salience 100）
   - 信用分 < 500 且无担保人拒绝（salience 90）
   - 申请额度 > 年收入 10 倍拒绝（salience 80）
2. **通过类**
   - A 级：信用分 > 750 + 负债比 < 30% + 额度 < 50 万，利率 4.2%（salience 50）
   - B 级：信用分 > 650 + 负债比 < 50% + 有抵押，利率 4.8%（salience 40）
   - C 级：信用分 > 550 + 有担保人 + 负债比 < 60%，利率 5.6%（salience 30）
3. **兜底**：未命中以上规则则转人工审核（salience 10）

DMN 决策表 `maxLoanAmountDecision` 独立负责「最高可贷额度」计算，与 DRL 规则各司其职。

## ⚠️ 启动报错排查清单

以下 7 个报错本项目在开发过程中全部踩到并已修复，按「报错 → 根因 → 修复」列出，供排查参考：

| 报错信息 | 根因 | 修复 |
| --- | --- | --- |
| `Field transactionManager in ...DefaultDatasourceConfiguration required a bean of type 'PlatformTransactionManager'` | Camunda starter 不传递 `spring-boot-starter-jdbc`，Spring Boot 无法自动装配 `DataSourceTransactionManager` | 显式引入 `spring-boot-starter-jdbc` |
| `Unknown KieSession name: defaultKieSession` + `please add the module org.drools:drools-xml-support` | Drools 10 起解析 `kmodule.xml` 需要独立的 XML 支持模块 | 引入 `org.drools:drools-xml-support` |
| 依赖解析报 `'dependencies.dependency.version' for org.kie:kie-spring is missing` | `kie-spring` 是 KIE 7 时代的工件，最后版本停在 7.74.1.Final，Drools 8+ 已不再提供 | 移除 `kie-spring`，直接用 `KieServices` API 集成 |
| `ENGINE-12018 History Time To Live (TTL) cannot be null` | Camunda 强制要求流程与 DMN 决策声明 TTL | BPMN 的 `<process>` 与 DMN 的 `<decision>` 加 `camunda:historyTimeToLive="180"` |
| `Cannot find serializer for value '... LoanApplication ...'` | 作为流程变量存入引擎的 POJO 未序列化 | `LoanApplication implements Serializable` |
| `Illegal method call. This session was previously disposed.`（第一次调用正常，第二次报错） | `KieSession` 被注册成单例 Bean，而委托里调用 `dispose()` 释放了它 | 只暴露 `KieContainer`，每次执行 `newKieSession()` 并在 `finally` 中释放 |
| `no processes deployed with key 'loanApprovalProcess'` | `@EnableProcessApplication` 会关闭基于 `deployment-resource-pattern` 的自动部署，改为要求 `META-INF/processes.xml` | 不用 `@EnableProcessApplication`，配置 `camunda.bpm.deployment-resource-pattern` |
| DRL 中的中文变成 `??????` 或乱码 | 容器 locale 为 POSIX，JVM 默认字符集退化为 ASCII | 启动加 `-Dfile.encoding=UTF-8` |

## 🔧 生产环境优化建议

1. 生产数据库切换为 MySQL/PostgreSQL，H2 仅用于开发测试
2. 启用 `KieScanner` 实现规则热加载，无需重启更新规则
3. 规则包打包为独立 Kjar，通过 Maven 仓库管理版本
4. 规则执行日志接入审计，记录每条规则的命中情况
5. 大流量场景用 `KieSession` 池化替代「每次新建」，降低对象创建开销
6. 谨慎把 POJO 当作流程变量：优先拆成基础类型变量，减少序列化耦合
