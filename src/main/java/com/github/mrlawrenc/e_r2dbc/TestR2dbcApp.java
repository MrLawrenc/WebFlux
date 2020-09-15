package com.github.mrlawrenc.e_r2dbc;

import org.springframework.boot.SpringApplication;

/**
 * @author : MrLawrenc
 * @date : 2020/5/31 17:36
 * @description :
 * @GetMapping(value = "", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
 * public Flux<User> findAll() {
 * return this.userService.findAll().delayElements(Duration.ofSeconds(2));
 * }
 * <p>
 * doc：https://spring.io/projects/spring-data-r2dbc
 */
public class TestR2dbcApp {
    public static void main(String[] args) {
        SpringApplication.run(TestR2dbcApp.class, args);
    }

}
