package com.github.mrlawrenc.e_r2dbc.repository;

import com.github.mrlawrenc.e_r2dbc.ApplicationConfiguration;
import com.github.mrlawrenc.e_r2dbc.entity.MultiObj;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @author : MrLawrenc
 * date  2020/9/15 23:15
 * 组合对象查询
 */
@Component
@AllArgsConstructor
public class MultiObjRepository {

    /**
     * 连表查询
     * 非单表关联的结果集都可以使用DatabaseClient解决
     *
     * @return 结果集
     */
    public Flux<MultiObj> select() {
        return ApplicationConfiguration.getDbClient()
                .execute("select * from user left join test on user.id=test.id ")
                .as(MultiObj.class)
                .fetch().all();
    }
}