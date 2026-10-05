package com.example.camundadrools.delegate;

import com.example.camundadrools.model.LoanApplication;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Drools 规则风控审批 Delegate
 *
 * KieSession 非线程安全，用完即废，因此每次执行都从 KieContainer
 * 新建一个会话，并在 finally 中释放，避免会话被复用或泄漏。
 */
@Component("droolsRiskCheckDelegate")
public class DroolsRiskCheckDelegate implements JavaDelegate {

    @Autowired
    private KieContainer kieContainer;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        System.out.println("====================================");
        System.out.println("🔍 执行 Drools 自动风控规则检查...");

        // 从流程变量获取贷款申请
        LoanApplication app = (LoanApplication) execution.getVariable("loanApplication");
        System.out.println("📋 申请人: " + app.getApplicantName() +
                          ", 信用分: " + app.getCreditScore() +
                          ", 申请额度: " + app.getLoanAmount() +
                          ", 负债比: " + app.getDebtRatio());

        // 每次执行新建会话，用完即释放
        KieSession kieSession = kieContainer.newKieSession("defaultKieSession");
        int firedRules;
        try {
            kieSession.insert(app);
            firedRules = kieSession.fireAllRules();
        } finally {
            kieSession.dispose();
        }

        System.out.println("⚡ 触发规则数量: " + firedRules);
        System.out.println("📊 审批结果: " + app.getApprove() +
                          ", 等级: " + app.getApprovalLevel() +
                          ", 利率: " + (app.getInterestRate() != null
                                  ? app.getInterestRate().multiply(new BigDecimal(100)) + "%" : "N/A"));
        if (app.getRejectReason() != null) {
            System.out.println("⚠️  备注: " + app.getRejectReason());
        }
        System.out.println("====================================");

        // 将结果写回流程变量
        execution.setVariable("approve", app.getApprove());
        execution.setVariable("rejectReason", app.getRejectReason());
        execution.setVariable("interestRate", app.getInterestRate());
        execution.setVariable("approvalLevel", app.getApprovalLevel());
        execution.setVariable("loanApplication", app);
    }
}
