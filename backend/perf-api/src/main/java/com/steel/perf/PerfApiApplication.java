package com.steel.perf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 钢铁行业多租户自动化绩效核算系统 - 启动类。
 */
@SpringBootApplication
@EnableScheduling
public class PerfApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PerfApiApplication.class, args);
    }
}
