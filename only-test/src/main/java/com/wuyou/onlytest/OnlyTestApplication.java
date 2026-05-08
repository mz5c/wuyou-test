package com.wuyou.onlytest;

import com.wuyou.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Import(GlobalExceptionHandler.class)
@SpringBootApplication
public class OnlyTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(OnlyTestApplication.class, args);
    }
}
