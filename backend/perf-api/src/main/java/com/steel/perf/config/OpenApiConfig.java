package com.steel.perf.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI 文档信息。UI: /swagger-ui.html，JSON: /v3/api-docs。 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI perfOpenApi() {
        return new OpenAPI().info(new Info()
                .title("钢铁行业多租户绩效核算系统 API")
                .version("1.0.0")
                .description("认证(JWT)、多租户隔离、核算引擎、规则治理、取数、申诉、指标计息等接口。"));
    }
}
