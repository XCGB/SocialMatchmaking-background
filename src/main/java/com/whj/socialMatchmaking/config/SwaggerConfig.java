package com.whj.socialMatchmaking.config;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * @author: Baldwin
 * @createTime: 2023-07-17 13:14
 * @description: Swagger接口文档配置
 */
@Configuration
@Profile({"dev"})
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        // 配置OAS 3.0协议
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                // 查找有@Tag注解的类，并生成一个对应的分组；类下面的所有http请求方法，都会生成对应的API接口
                // 通过这个配置，就可以将那些没有添加@Tag注解的控制器类排除掉
                .apis(RequestHandlerSelectors.withClassAnnotation(Tag.class))
                // 这里是扫描指定位置的全部类及其中的方法
//                 .apis(RequestHandlerSelectors.basePackage("com.whj.socialMatchmaking.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("伙伴匹配系统")
                .description("伙伴匹配系统文档")
                .version("0.0.1")
                .build();
    }


}

