package com.cyc.xiangwei.commodity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.cyc.xiangwei"})
public class XiangweiCommodityApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiangweiCommodityApplication.class, args);
    }

}
