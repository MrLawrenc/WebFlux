# Web Flux

[官方文档](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-framework-choice)

引入web flux依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

## Hello World

### 基于注解

和原有的spring mvc使用方式几乎一样

```java
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
```

增加application.yml配置

```yml
server:
  port: 80
```

启动并访问，一个最基础的web flux应用就构建完成了

### 链式路由

这是一种函数式编程构建路由的方式,先构建配置类

```java
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
```

再构建启动类

```java
@SpringBootApplication
public class RouterMain {


    public static void main(String[] args) {
        SpringApplication.run(RouterMain.class, args);
    }
}
```

启动并访问/date /time /times 三个路由

## WebClient

### 初体验

以上面链式路由的三个接口作为服务端

```java
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
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
        System.out.println("#####################################");
        times("times");
        TimeUnit.DAYS.sleep(1);
    }

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

    public static void times(String uri) throws Exception {
        WebClient webClient = WebClient.create("http://localhost:80");
        webClient
            .get().uri("/times")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(String.class)
            .log()
            .take(10)
            .blockLast();
    }

}
```

### 和其余比较

链式路由里面增加一个慢任务用于测试

```java
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
```

启动链式路由主方法作为服务端

- java11的httpclient

  ```java
  /**
   * @author : MrLawrenc
   * @date : 2020/5/31 17:52
   * @description :
   * <p>
   * {@link org.springframework.web.client.RestTemplate} 差不多
   */
  public class Java11HttpClient {
  
      public static void main(String[] args) throws Exception {
          HttpClient client = HttpClient.newBuilder().build();
  
  
          HttpResponse<String> response = client.send(
              HttpRequest.newBuilder().uri(URI.create("http://localhost/date")).build()
              , HttpResponse.BodyHandlers.ofString());
  
          //阻塞任务
          CompletableFuture<HttpResponse<String>> future = client.sendAsync(
              HttpRequest.newBuilder().uri(URI.create("http://localhost/sleep")).build()
              , HttpResponse.BodyHandlers.ofString());
  
          System.out.println(response + "\n" + response.body());
  
  
          //优雅一点
          future.whenComplete((r, t) -> System.out.format("listen result:%s\n", r.body()));
          //阻塞等待
          System.out.println("wait result:" + future.get().body());
  
      }
  }
  ```

- restTemplate

  [性能测试](https://blog.csdn.net/get_set/article/details/79506373?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-2.nonecase&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-2.nonecase)

- WebClient调用阻塞方法

  异步方式

  ```java
  //阻塞任务测试
  System.out.println("#################slow start####################");
  Flux<String> stringFlux = WebClient.create("http://localhost:80")
      .get().uri("/sleep").retrieve().bodyToFlux(String.class);
  stringFlux.subscribe(System.out::println);
  System.out.println("#################slow end####################");
  TimeUnit.DAYS.sleep(1);
  ```

  



## 性能测试

- [对比mvc的性能测试](https://www.jianshu.com/p/b2d53667e7e2)