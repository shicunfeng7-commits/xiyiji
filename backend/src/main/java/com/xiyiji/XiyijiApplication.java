package com.xiyiji;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan(basePackages = {
    "com.xiyiji.modules.admin.mapper",
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