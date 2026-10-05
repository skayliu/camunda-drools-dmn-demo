package com.example.camundadrools.controller;

import com.example.camundadrools.model.LoanApplication;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 贷款审批测试接口
 */
@RestController
@RequestMapping("/api/loan")
public class LoanController {

    @Autowired
    private RuntimeService runtimeService;

    /**
     * 提交贷款申请
     * 测试用例：
     * 1. 优质客户：creditScore=780, debtRatio=0.2, loanAmount=300000, annualIncome=500000, hasGuarantor=false, hasCollateral=true
     * 2. 拒绝客户：creditScore=450, debtRatio=0.8, loanAmount=1000000, annualIncome=200000, hasGuarantor=false, hasCollateral=false
     * 3. 人工审核：creditScore=600, debtRatio=0.5, loanAmount=300000, annualIncome=400000, hasGuarantor=true, hasCollateral=false
     */
    @PostMapping("/apply")
    public Map<String, Object> applyLoan(@RequestBody LoanApplication application) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("loanApplication", application);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "loanApprovalProcess",
                variables
        );

        // 查询最终结果
        Boolean approve = (Boolean) runtimeService.getVariable(processInstance.getId(), "approve");
        String rejectReason = (String) runtimeService.getVariable(processInstance.getId(), "rejectReason");
        BigDecimal interestRate = (BigDecimal) runtimeService.getVariable(processInstance.getId(), "interestRate");
        String approvalLevel = (String) runtimeService.getVariable(processInstance.getId(), "approvalLevel");

        Map<String, Object> result = new HashMap<>();
        result.put("processInstanceId", processInstance.getId());
        result.put("approve", approve);
        result.put("approvalLevel", approvalLevel);
        result.put("interestRate", interestRate != null ? interestRate.multiply(new BigDecimal(100)) + "%" : null);
        result.put("rejectReason", rejectReason);
        result.put("message", approve == null ? "已转人工审核" : 
                (approve ? "自动审批通过" : "自动审批拒绝"));
        
        return result;
    }
}
