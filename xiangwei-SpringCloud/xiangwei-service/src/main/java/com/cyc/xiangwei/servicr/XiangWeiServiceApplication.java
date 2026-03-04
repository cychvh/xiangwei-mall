package com.cyc.xiangwei.servicr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class XiangWeiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(XiangWeiServiceApplication.class, args);
    }
}
