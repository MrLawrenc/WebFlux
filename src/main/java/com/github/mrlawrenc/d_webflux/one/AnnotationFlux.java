package com.github.mrlawrenc.d_webflux.one;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author : MrLawrenc
 * @date : 2020/5/31 17:06
 * @description : 测试注解方式使用web flux
 */
@SpringBootApplication
public class AnnotationFlux {

    public static void main(String[] args) {
        SpringApplication.run(AnnotationFlux.class, args);
    }

    @RestController
    public static class AnnotationController {
        @GetMapping("/testAnnotation")
        public Mono<String> test() {
            return Mono.just("我是测试注解方式的web flux");
        }
    }
}