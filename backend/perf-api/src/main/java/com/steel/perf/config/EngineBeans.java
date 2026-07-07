package com.steel.perf.config;

import com.steel.perf.engine.coefficient.CoefficientMapper;
import com.steel.perf.engine.cost.CostEngine;
import com.steel.perf.engine.formula.ExpressionEngine;
import com.steel.perf.engine.scope.ScopeResolver;
import com.steel.perf.engine.tier.TierEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 将核算引擎组件注册为 Spring Bean。 */
@Configuration
public class EngineBeans {

    @Bean
    public ScopeResolver scopeResolver() {
        return new ScopeResolver();
    }

    @Bean
    public TierEngine tierEngine() {
        return new TierEngine();
    }

    @Bean
    public CoefficientMapper coefficientMapper() {
        return new CoefficientMapper();
    }

    @Bean
    public CostEngine costEngine() {
        return new CostEngine();
    }

    @Bean
    public ExpressionEngine expressionEngine() {
        return new ExpressionEngine();
    }
}
