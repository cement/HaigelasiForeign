package com.haigelasi.mall.foreign;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(value = "com.haigelasi.mall.foreign.mapper")
public class ForeignApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForeignApplication.class, args);
    }

}
