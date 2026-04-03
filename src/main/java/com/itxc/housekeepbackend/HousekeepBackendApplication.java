package com.itxc.housekeepbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.itxc.housekeepbackend.mapper")
public class HousekeepBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HousekeepBackendApplication.class, args);
    }

}
