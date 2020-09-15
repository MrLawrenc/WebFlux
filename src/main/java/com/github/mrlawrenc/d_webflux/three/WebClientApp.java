package com.github.mrlawrenc.d_webflux.three;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * @author : MrLawrenc
 * @date : 2020/5/31 17:38
 * @description : WebClient 初体验
 * <p>
 * 以two包里面的{@link com.github.mrlawrenc.d_webflux.two.RouterMain}作为服务端
 * <p>
 * netty 在>9之后启动报错
 * <a herf="https://stackoverflow.com/questions/57885828/netty-cannot-access-class-jdk-internal-misc-unsafe">
 * 详见StackOverFlow
 * </a>
 * <p>
 * -Dio.netty.tryReflectionSetAccessible=true
 * --add-opens java.base/jdk.internal.misc=ALL-UNNAMED
 */
public class WebClientApp {

    public static void main(String[] args) throws Exception {
        dateAndTime("date");
        dateAndTime("time");

        System.out.println("#####################################");
        System.out.println("#####################################");
        times();


        //阻塞任务测试
        System.out.println("#################slow start####################");
        Flux<String> stringFlux = WebClient.create("http://localhost:80")
                .get().uri("/sleep").retrieve().bodyToFlux(String.class);
        stringFlux.subscribe(System.out::println);
        System.out.println("#################slow end####################");
        TimeUnit.DAYS.sleep(1);
    }


    /**
     * 获得响应有 2 种方法，exchange 和 retrieve 方法。两者返回类型不同，exchange 返回的内容更多，包含了响应头信息，Cookie，状态码等信息，
     * 它的类型本质上是 ClientResponse。retrieve 方法则是获得响应 body 的快捷方式。
     * <p>
     * 由于响应的得到是异步的，所以可以调用 block 方法来阻塞当前程序，等待获得响应的结果。
     *
     * <pre>
     *      Mono<ClientResponse> mono = webClient.post().uri("login").syncBody(map).exchange();
     *      ClientResponse response = mono.block();
     *      if (response.statusCode() == HttpStatus.OK) {
     *      Mono<Result> resultMono = response.bodyToMono(Result.class);
     *      resultMono.subscribe(result -> {
     *         if (result.isSuccess()) {
     *             ResponseCookie sidCookie = response.cookies().getFirst("sid");
     *             Flux<User> userFlux = webClient.get().uri("users").cookie(sidCookie.getName(), sidCookie.getValue()).retrieve().bodyToFlux(User.class);
     *             userFlux.subscribe(System.out::println);
     *         }
     *     });
     * }
     * </pre>
     * <pre>
     *     String response2 = request1.exchange()
     *                                  .block()
     *                                  .bodyToMono(String.class)
     *                                  .block();
     * String response3 = request2
     *                      .retrieve()
     *                      .bodyToMono(String.class)
     *                      .block();
     * </pre>
     */
    public static void dateAndTime(String uri) throws Exception {
        WebClient webClient = WebClient.create("http://localhost:80");
        Mono<String> resp = webClient
                .get().uri("/" + uri)
                //异步地获取response信息；
                .retrieve()
                .bodyToMono(String.class);
        resp.subscribe(System.out::println);
        TimeUnit.SECONDS.sleep(1);
    }

    public static void times() throws Exception {
        WebClient webClient = WebClient.create("http://localhost:80");
        webClient.get().uri("/times")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .log()
                .take(10)
                .blockLast();
    }

}