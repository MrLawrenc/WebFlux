package com.github.mrlawrenc.e_r2dbc;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.core.DatabaseClient;

import java.time.Duration;
import java.util.Objects;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

/**
 * @author : MrLawrenc
 * date  2020/9/15 21:04
 */
@Configuration
public class ApplicationConfiguration extends AbstractR2dbcConfiguration {

    private static ConnectionFactory realPoolFactory;

    @Bean
    @Override
    public ConnectionFactory connectionFactory() {
        //配置方式1 r2dbc:数据库类型://用户名:密码@主机ip:端口/数据库名称
        //return ConnectionFactories.get("r2dbc:mysql://root:admin@192.168.126.1:3306/study");

        //配置方式2 没有连接池
        /*var connection = MySqlConnectionConfiguration.builder()
                .host("localhost")
                .port(3306)
                .database("study")
                .username("root")
                .password("admin").build();
        return MySqlConnectionFactory.from(connection);*/

        //配置3 池化(1.设置连接工工厂参数  2.设置连接池配置  3.创建连接池)
        ConnectionFactory connectionFactory = ConnectionFactories.get(builder()
                .option(DRIVER, "pool")
                //如果是pgsql 只需将mysql更改为postgresql即可
                .option(PROTOCOL, "mysql")
                .option(HOST, "localhost")
                .option(PORT, 3306)
                .option(USER, "root")
                .option(PASSWORD, "admin")
                .option(DATABASE, "study")
                //.option(MAX_SIZE, 12)
                .build());

        var poolConnConfig = ConnectionPoolConfiguration.builder(connectionFactory)
                .maxSize(12)
                .maxIdleTime(Duration.ofMillis(30))
                .initialSize(2)
                .maxCreateConnectionTime(Duration.ofSeconds(1))
                .build();

        ConnectionPool connectionPool = new ConnectionPool(poolConnConfig);
        connectionPool.create().block();
        realPoolFactory = connectionPool;
        return connectionPool;
    }

    /**
     * 当非单表查询，即查询返回的实体不能和数据表一一绑定时，可以借助DatabaseClient来执行
     *
     * @return db执行器
     * <p>
     * 获取到了用法如下：
     * <pre>
     *      DatabaseClient client = DatabaseClient.create(connectionFactory);
     *
     *         Mono<Integer> affectedRows = client.execute()
     *                 .sql("UPDATE user SET userName = 'Joe'")
     *                 .fetch().rowsUpdated();
     *
     *         Flux<User> all = client.execute()
     *                 .sql("SELECT * FROM user")
     *                 .as(User.class)
     *                 .fetch().all();
     * </pre>
     */
    public static DatabaseClient getDbClient() {
        if (Objects.isNull(realPoolFactory)) {
            throw new RuntimeException("realPoolFactory is not initialize");
        }
        return DatabaseClient.create(realPoolFactory);
    }
}