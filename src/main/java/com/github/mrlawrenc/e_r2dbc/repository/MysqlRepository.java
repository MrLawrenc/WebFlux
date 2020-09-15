package com.github.mrlawrenc.e_r2dbc.repository;

import com.github.mrlawrenc.e_r2dbc.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author : MrLawrenc
 * date  2020/9/15 21:09
 */
@Repository
public interface MysqlRepository extends ReactiveCrudRepository<User, Long> {

    /**
     * 完美支持jpa
     *
     * @param userName 用户名
     * @return 结果
     */
    Mono<User> findByUserName(String userName);

    /**
     * jpa删除
     *
     * @param userName 用户名
     * @param isDel    是否删除
     * @return 结果
     */
    Mono<Void> deleteByUserNameAndAndIsDel(String userName, int isDel);

    /**
     * 自定义sql 分页查询
     *
     * @param begin 从begin+1条记录开始  展示的行数是由limit参数决定
     * @param size 每页大小
     * @return 结果集
     */
    @Query("select * from user limit :size offset :begin")
    Flux<User> query(@Param("begin") int begin,@Param("size") int size);
}