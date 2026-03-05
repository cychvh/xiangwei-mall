package com.cyc.xiangwei.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.cyc.xiangwei"})
public class XiangweiAuthService {
    public static void main(String[] args) {
        SpringApplication.run(XiangweiAuthService.class, args);
    }
}
