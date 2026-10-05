package com.example.camundadrools.config;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Drools 规则引擎配置
 *
 * 只暴露 KieContainer 这一个 Bean。
 * 不要在这里把 KieSession 注册成单例 Bean：KieSession 既非线程安全，
 * 又会在使用后被 dispose()，做成单例后第二次调用会抛
 * Illegal method call. This session was previously disposed.
 * 正确做法是每次执行时用 kieContainer.newKieSession(...) 新建会话。
 */
@Configuration
public class DroolsConfig {

    @Bean
    public KieContainer kieContainer() {
        KieServices kieServices = KieServices.Factory.get();
        // 直接从 classpath 加载 kmodule.xml，无需额外 Kjar
        return kieServices.getKieClasspathContainer();
    }
}
