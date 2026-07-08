package com.steel.perf.config;

import com.steel.perf.calc.CalcEngine;
import com.steel.perf.engine.coefficient.CoefficientMapper;
import com.steel.perf.engine.cost.CostEngine;
import com.steel.perf.engine.formula.ExpressionEngine;
import com.steel.perf.engine.scope.ScopeResolver;
import com.steel.perf.engine.tier.TierEngine;
import com.steel.perf.metric.ContractInterestService;
import com.steel.perf.metric.MetricService;
import com.steel.perf.rule.ChangeRequestService;
import com.steel.perf.rule.ParamDiff;
import com.steel.perf.rule.PublishValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 将核算引擎/指标/核算/治理组件注册为 Spring Bean。 */
@Configuration
public class EngineBeans {

    @Bean
    public MetricService metricService() {
        return new MetricService();
    }

    @Bean
    public ContractInterestService contractInterestService() {
        return new ContractInterestService();
    }

    @Bean
    public CalcEngine calcEngine(ExpressionEngine expressionEngine) {
        return new CalcEngine(expressionEngine);
    }

    @Bean
    public ChangeRequestService changeRequestService() {
        return new ChangeRequestService();
    }

    @Bean
    public PublishValidator publishValidator() {
        return new PublishValidator();
    }

    @Bean
    public ParamDiff paramDiff() {
        return new ParamDiff();
    }

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
