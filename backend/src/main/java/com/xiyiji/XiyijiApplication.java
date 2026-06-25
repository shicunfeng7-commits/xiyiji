package com.xiyiji;

import com.xiyiji.common.config.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableConfigurationProperties(JwtProperties.class)
@MapperScan(basePackages = {
    "com.xiyiji.modules.employee.mapper",
    "com.xiyiji.modules.order.mapper",
    "com.xiyiji.modules.system.mapper",
    "com.xiyiji.modules.user.mapper"
})
public class XiyijiApplication {
    public static void main(String[] args) {
        SpringApplication.run(XiyijiApplication.class, args);
    }
}
