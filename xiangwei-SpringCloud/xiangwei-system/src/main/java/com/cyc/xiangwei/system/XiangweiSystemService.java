package com.cyc.xiangwei.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class XiangweiSystemService {
    public static void main(String[] args) {
        SpringApplication.run(XiangweiSystemService.class, args);
    }
}
