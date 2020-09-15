package com.github.mrlawrenc.d_webflux.two;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;

/**
 * @author : MrLawrenc
 * @date : 2020/5/31 17:19
 * @description : 基于路由的函数式配置
 */
@SpringBootApplication
public class RouterMain {


    public static void main(String[] args) {
        SpringApplication.run(RouterMain.class, args);
    }

    @RestController
    public static class SlowController {
        @GetMapping("/sleep")
        public Flux<String> slow() throws InterruptedException {
            TimeUnit.SECONDS.sleep(10);
            return Flux.just("slow task done...");
        }
    }
}