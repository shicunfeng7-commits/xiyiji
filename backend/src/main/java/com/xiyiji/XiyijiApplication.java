package com.xiyiji;

import com.xiyiji.common.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableConfigurationProperties(JwtProperties.class)
public class XiyijiApplication {
    public static void main(String[] args) {
        SpringApplication.run(XiyijiApplication.class, args);
    }
}
