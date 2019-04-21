package com.sec;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sec.dao")
public class SecApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecApplication.class, args);
    }

}
