package com.whj.socialMatchmaking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

//开启swagger
@EnableSwagger2
@EnableOpenApi
@EnableScheduling
@SpringBootApplication
@MapperScan("com.whj.socialMatchmaking.mapper")
public class SocialMatchmakingBackgroundApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialMatchmakingBackgroundApplication.class, args);
    }

}
