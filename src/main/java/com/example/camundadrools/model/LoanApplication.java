package com.example.camundadrools.model;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 贷款申请实体类
 *
 * 注意：作为流程变量存入 Camunda 时必须可序列化，
 * 否则启动流程实例会报 Cannot find serializer for value ...
 */
@Data
public class LoanApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申请人姓名
     */
    private String applicantName;
    
    /**
     * 身份证号
     */
    private String idCard;
    
    /**
     * 信用分 300-850
     */
    private Integer creditScore;
    
    /**
     * 年收入（元）
     */
    private BigDecimal annualIncome;
    
    /**
     * 申请贷款金额（元）
     */
    private BigDecimal loanAmount;
    
    /**
     * 贷款期限（月）
     */
    private Integer loanTerm;
    
    /**
     * 负债比 = 月负债/月收入
     */
    private BigDecimal debtRatio;
    
    /**
     * 是否有担保人
     */
    private Boolean hasGuarantor;
    
    /**
     * 是否有抵押物
     */
    private Boolean hasCollateral;

    // ========== 审批结果字段 ==========
    /**
     * 是否通过审批
     */
    private Boolean approve;
    
    /**
     * 拒绝原因
     */
    private String rejectReason;
    
    /**
     * 贷款利率
     */
    private BigDecimal interestRate;
    
    /**
     * 最高可贷额度
     */
    private BigDecimal maxLoanAmount;
    
    /**
     * 审批等级：A/B/C/D
     */
    private String approvalLevel;
}
