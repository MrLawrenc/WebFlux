package com.github.mrlawrenc.doc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;

/**
 * @author : MrLawrenc
 * date  2020/9/16 23:20
 * @see springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc 开启mvc
 */
@Configuration
@EnableSwagger2WebFlux
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .description("这是学习Reactor和WebFlux相关的API文档")
                        .title("web flux doc")
                        .version("1.0.0")
                        .contact(new Contact("逍遥", "https://lmy25.wang/", "mrliu943903861@163.com"))
                        .licenseUrl("www.baidu.com")
                        .build())
                .groupName("https://github.com/MrLawrenc/WebFlux")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.github.mrlawrenc"))
                .paths(PathSelectors.any())
                .build();

    }
}