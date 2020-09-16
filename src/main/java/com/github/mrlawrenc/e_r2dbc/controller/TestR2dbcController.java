package com.github.mrlawrenc.e_r2dbc.controller;

import com.github.mrlawrenc.e_r2dbc.entity.MultiObj;
import com.github.mrlawrenc.e_r2dbc.entity.User;
import com.github.mrlawrenc.e_r2dbc.repository.MultiObjRepository;
import com.github.mrlawrenc.e_r2dbc.repository.MysqlRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "WebFlux Controller入口")
public class TestR2dbcController {
    private final MysqlRepository mysqlRepository;
    private final MultiObjRepository multiObjRepository;

    @ApiOperation("查询mysql所有数据")
    @GetMapping("/mysqlAll")
    public Flux<User> selectAllMysql() {
        return mysqlRepository.findAll();
    }


    @ApiOperation("根据jpa规则进行条件查询")
    @GetMapping("/findByName")
    public Mono<User> findByName() {
        return mysqlRepository.findByUserName("超级管理员");
    }

    @ApiOperation("分页")
    @GetMapping("/query")
    public Flux<User> query() {
        int page = 1;
        int size = 10;
        return mysqlRepository.query((page - 1) * size, size);
    }

    @ApiOperation("存数据")
    @PostMapping("/save")
    public Mono<User> saveUser(@RequestBody User user) {
        user.setId(null);
        return mysqlRepository.save(user);
    }

    @ApiOperation("结果非一个表对应得对象，而是多个表对应得实体的组合对象")
    @GetMapping("/queryMultiObj")
    public Flux<MultiObj> queryMultiObj() {
        return multiObjRepository.select();
    }
}