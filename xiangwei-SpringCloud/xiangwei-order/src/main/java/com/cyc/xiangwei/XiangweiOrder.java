package com.cyc.xiangwei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication(scanBasePackages = {"com.cyc.xiangwei"})
public class XiangweiOrder {
    public static void main(String[] args) {
        SpringApplication.run(XiangweiOrder.class, args);
    }
}