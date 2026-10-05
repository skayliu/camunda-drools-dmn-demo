package com.example.camundadrools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动类
 *
 * 说明：这里刻意不使用 @EnableProcessApplication。
 * 一旦启用 ProcessApplication，Camunda starter 会关闭基于
 * camunda.bpm.deployment-resource-pattern 的自动部署，改为要求
 * META-INF/processes.xml；未提供该文件时流程文件不会被部署，
 * 于是接口会报 no processes deployed with key 'xxx'。
 */
@SpringBootApplication
public class CamundaDroolsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CamundaDroolsApplication.class, args);
        System.out.println("✅ Camunda + Drools + DMN 项目启动成功！");
        System.out.println("🌐 访问地址: http://localhost:8080/camunda");
        System.out.println("👤 默认账号: admin / admin");
    }
}
