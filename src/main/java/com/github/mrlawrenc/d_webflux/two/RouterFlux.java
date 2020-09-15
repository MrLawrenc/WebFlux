package com.github.mrlawrenc.d_webflux.two;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * @author : MrLawrenc
 * @date : 2020/5/31 17:14
 * @description : 基于路由的函数式配置
 */
@Configuration
public class RouterFlux {


    @Bean
    public RouterFunction<ServerResponse> timerRouter() {
        return route(GET("/time"), request -> ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).body(
                Mono.just("Today is " + new SimpleDateFormat("yyyy-MM-dd").format(new Date())), String.class))

                .andRoute(GET("/date"), request -> ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).body(
                        Mono.just("Now is " + new SimpleDateFormat("HH:mm:ss").format(new Date())), String.class))

                //这是一个实时推送流
                .andRoute(GET("/times"), RouterFlux.this::sendTimePerSec);


    }


    public Mono<ServerResponse> sendTimePerSec(ServerRequest serverRequest) {
        return ok().contentType(MediaType.TEXT_EVENT_STREAM).body(
                Flux.interval(Duration.ofSeconds(1)).
                        map(l -> new SimpleDateFormat("HH:mm:ss").format(new Date())),
                String.class);
    }

}