package com.github.mrlawrenc.e_r2dbc.controller;

import com.github.mrlawrenc.e_r2dbc.entity.MultiObj;
import com.github.mrlawrenc.e_r2dbc.entity.User;
import com.github.mrlawrenc.e_r2dbc.repository.MultiObjRepository;
import com.github.mrlawrenc.e_r2dbc.repository.MysqlRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author : MrLawrenc
 * date  2020/9/15 21:14
 */
@RestController
@RequestMapping("/r2dbc")
@AllArgsConstructor
public class TestR2dbcController {
    private final MysqlRepository mysqlRepository;
    private final MultiObjRepository multiObjRepository;

    @GetMapping("/mysqlAll")
    public Flux<User> selectAllMysql() {
        return mysqlRepository.findAll();
    }

    @GetMapping("/findByName")
    public Mono<User> findByName() {
        return mysqlRepository.findByUserName("超级管理员");
    }

    @GetMapping("/query")
    public Flux<User> query() {
        int page = 1;
        int size = 10;
        return mysqlRepository.query((page - 1) * size, size);
    }

    @PostMapping("/save")
    public Mono<User> saveUser(@RequestBody User user) {
        return mysqlRepository.save(user);
    }

    @GetMapping("/queryMultiObj")
    public Flux<MultiObj> queryMultiObj() {
        return multiObjRepository.select();
    }
}