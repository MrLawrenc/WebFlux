# Reactor3

##  概述

reactor3符合响应式编程的几个规范接口（Publisher Subscription  Processor Processor ），Publisher 主要由`Flux`和`Mono`两个类定义

- Flux

  一个Flux对象代表一个包含0/N(零或者N)个元素的响应式序列

- Mono

  而一个Mono对象代表一个包含0/1元素的结果

引入reactor

```xml
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-core</artifactId>
    <version>3.1.4.RELEASE</version>
</dependency>
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-test</artifactId>
    <version>3.1.4.RELEASE</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.12</version>
    <scope>test</scope>
</dependency>
```



## Flux和Mono初步使用

一些使用demo

- 创建

  ```java
  Flux<Integer> just = Flux.just(1, 2, 3, 4, 5, 6);
  Mono<Integer> mono = Mono.just(1);
  
  //类似于Optional.ofNullable(null); 查数据 Mono<User> findById(long id); 结果也可能为空
  Flux.empty();
  Mono.empty();
  
  Integer[] array = new Integer[]{1, 2, 3, 4, 5, 6};
  Flux.fromArray(array);
  List<Integer> list = Arrays.asList(array);
  Flux.fromIterable(list);
  Stream<Integer> stream = list.stream();
  Flux.fromStream(stream);
  ```

- 订阅

  ```java
  /**
       * 在发生订阅之前数据不会有任何动作，和stream的最终收集操作才触发数据流动一样
       * <p>
       * 订阅变体
       * <pre>
       *     // 订阅并触发数据流
       * subscribe();
       * // 订阅并指定对正常数据元素如何处理
       * subscribe(Consumer<? super T> consumer);
       * // 订阅并定义对正常数据元素和错误信号的处理
       * subscribe(Consumer<? super T> consumer,
       *           Consumer<? super Throwable> errorConsumer);
       * // 订阅并定义对正常数据元素、错误信号和完成信号的处理
       * subscribe(Consumer<? super T> consumer,
       *           Consumer<? super Throwable> errorConsumer,
       *           Runnable completeConsumer);
       * // 订阅并定义对正常数据元素、错误信号和完成信号的处理，以及订阅发生时的处理逻辑
       * subscribe(Consumer<? super T> consumer,
       *           Consumer<? super Throwable> errorConsumer,
       *           Runnable completeConsumer,
       *           Consumer<? super Subscription> subscriptionConsumer);
       * </pre>
       */
  public static void subscribe() throws InterruptedException {
      Flux.just(1, 2, 3, 4, 5, 6).subscribe(
          System.out::println,
          System.err::println,
          () -> System.out.println("Completed!"));
  
      //zip它能够将多个流一对一的合并起来
      zip(Flux.just(1, 2), Flux.just(5, 6)).subscribe(System.out::println);
  
      //以空格拆分为字符流
      // 使用Flux.interval声明一个每200ms发出一个元素的long数据流；因为zip操作是一对一的，故而将其与字符串流zip之后，字符串流也将具有同样的速度；
      //zip之后的流中元素类型为Tuple2，使用getT1方法拿到字符串流的元素；定义完成信号的处理为countDown;
      //countDownLatch.await(10, TimeUnit.SECONDS)会等待countDown倒数至0，最多等待10秒钟。
      String desc = "Zip two sources together, that is to say wait for all the sources to emit one element and combine these elements once into a Tuple2.";
      CountDownLatch countDownLatch = new CountDownLatch(1);  // 2
      zip(Flux.fromArray(desc.split("\\s+")),
          Flux.interval(Duration.ofMillis(200)))  // 3
          .subscribe(t -> System.out.println(t.getT1()), null, countDownLatch::countDown);    // 4
      countDownLatch.await(10, TimeUnit.SECONDS);     // 5
  }
  ```

  

- reactor调试

  ```java
   // reactor test  lambda 调试
  StepVerifier.create(Flux.just(1, 2, 3, 4, 5, 6))
      .expectNext(1, 2, 3, 4, 5, 6)
      .expectComplete()
      .verify();
  
  Mono.error(new Exception("some error")).subscribe(
      System.out::println,
      System.err::println,
      () -> System.out.println("Completed!")
  );
  ```

  更多调试参考[刘康专栏](https://blog.csdn.net/get_set/article/details/79611420)

## reactor的schedule

- publishOn和subscribeOn

   publishOn(pool) 会改变后面一个操作所在的线程，后面操作只会在一个线程执行
    subscribeOn(pool) 无论在何处，均只影响源头的线程

  ```java
  public static void on() throws Exception {
      CountDownLatch countDownLatch = new CountDownLatch(111);
  
      Scheduler pool = Schedulers.newParallel(" pool ", 10);
      Flux.range(0, 5)
          .map(i -> {
              try {
                  TimeUnit.SECONDS.sleep(1);
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
              System.out.println("map1 : " + Thread.currentThread().getName());
              return i / 2;
          })
          .publishOn(pool)
          .map(i -> {
              System.out.println("map2 : " + Thread.currentThread().getName());
              return i * 2;
          })
          //会改变的一个任务调度池
          .publishOn(Schedulers.elastic())
          .filter(t -> {
              try {
                  TimeUnit.SECONDS.sleep(1);
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
              System.out.println("filter1 : " + Thread.currentThread().getName());
              return true;
          })
          .publishOn(pool)
          .filter(t -> {
              try {
                  TimeUnit.SECONDS.sleep(2);
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
              System.out.println("filter2 : " + Thread.currentThread().getName());
              return true;
          })
          //无论位置如何，均只影响源头的线程
          .subscribeOn(Schedulers.newSingle("源头"))
          .subscribe(t -> {
              System.out.println("result : " + Thread.currentThread().getName());
          }, null, countDownLatch::countDown);
  }
  ```

  执行结果

  ```java
  public static void main(String[] args) throws Exception {
      on();
      TimeUnit.DAYS.sleep(1);
  }
  ```

  输出

  ```java
  11:07:45.283 [main] DEBUG reactor.util.Loggers$LoggerFactory - Using Slf4j logging framework
  map1 : 源头-1
  map2 :  pool -2
  map1 : 源头-1
  map2 :  pool -2
  filter1 : elastic-2
  map1 : 源头-1
  map2 :  pool -2
  filter1 : elastic-2
  map1 : 源头-1
  map2 :  pool -2
  filter1 : elastic-2
  filter2 :  pool -1
  result :  pool -1
  map1 : 源头-1
  map2 :  pool -2
  filter1 : elastic-2
  filter1 : elastic-2
  filter2 :  pool -1
  result :  pool -1
  ```

- parallel和runOn

  使用parallel（）之后，需要显示开启runOn，则后面任务均是异步的

  且后面的第一个任务和后面的第二个使用的线程有关联，即一个线程从异步开始的第一个任务会执行到订阅结束

  ```java
  public static void parallel() throws Exception {
      Flux.range(0, 11).parallel(31)
          .map(i -> {
              System.out.println("first map : " + Thread.currentThread().getName());
              return i * 2;
          })
          .runOn(Schedulers.parallel())
          .filter(t -> {
              try {
                  TimeUnit.SECONDS.sleep(1);
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
              System.out.println("filter1 : " + Thread.currentThread().getName());
              return true;
          })
          .map(r -> {
              System.out.println("second map : " + Thread.currentThread().getName());
              return r;
          })
          .subscribe(r -> {
              System.out.println("subscribe : " + Thread.currentThread().getName());
          });
  }
  ```

  执行结果

  ```java
  first map : main
  first map : main
  first map : main
  first map : main
  first map : main
  first map : main
  first map : main
  first map : main
  first map : main
  first map : main
  first map : main
  filter1 : parallel-10
  filter1 : parallel-4
  filter1 : parallel-8
  filter1 : parallel-6
  filter1 : parallel-5
  filter1 : parallel-9
  second map : parallel-10
  filter1 : parallel-7
  filter1 : parallel-3
  filter1 : parallel-2
  filter1 : parallel-1
  filter1 : parallel-11
  second map : parallel-5
  second map : parallel-1
  second map : parallel-2
  subscribe : parallel-10
  second map : parallel-3
  second map : parallel-8
  second map : parallel-6
  second map : parallel-7
  second map : parallel-4
  second map : parallel-9
  subscribe : parallel-4
  subscribe : parallel-7
  subscribe : parallel-1
  subscribe : parallel-6
  subscribe : parallel-8
  subscribe : parallel-5
  subscribe : parallel-3
  subscribe : parallel-2
  second map : parallel-11
  subscribe : parallel-9
  subscribe : parallel-11
  ```



## 错误处理

- 错误捕获并修正返回

  ```java
  Flux.range(1, 6)
      .map(i -> 10/(i-3))
      .onErrorReturn(0)   // 1
      .map(i -> i*i)
      .subscribe(System.out::println, System.err::println);
  
  ```

- 当有错误产生时，可以重试

  ```java
  	/**
       * retry对于上游Flux是采取的重订阅（re-subscribing）的方式，因此重试之后实际上已经一个不同的序列了， 发出错误信号的序列仍然是终止了的
       */
  public static void retry() throws Exception {
      Flux.range(1, 6)
          .map(i -> 10 / (3 - i))
          .retry(1)
          .subscribe(System.out::println, System.err::println);
      Thread.sleep(100);
  
  }
  ```

- 当有错误产生时，可以记录日志，包装错误之后再返回

  ```java
  Flux.just(e1, e2)
      .flatMap(k -> callExternalService(k)) 
      .doOnError(e -> {   
          log("error log.....");    
      })
      .onErrorResume(e -> getFromCache(k)); 
  ```

## 背压

以上例子数据均是全给下游，以下例子将体现背压机制

```java
	/**
     * 背压
     * <p>
     * Flux.range是一个快的Publisher；
     * 在每次request的时候打印request个数；
     * 通过重写BaseSubscriber的方法来自定义Subscriber；
     * hookOnSubscribe定义在订阅的时候执行的操作；
     * 订阅时首先向上游请求1个元素；
     * hookOnNext定义每次在收到一个元素的时候的操作；
     * sleep 1秒钟来模拟慢的Subscriber；
     * 打印收到的元素；
     * 每次处理完1个元素后再请求1个
     */
public static void backpressure() {
    Flux.range(1, 6)
        .doOnRequest(n -> System.out.println("Request " + n + " values..."))
        .subscribe(new BaseSubscriber<Integer>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                System.out.println("Subscribed and make a request...");
                request(1); // 5
            }

            @Override
            protected void hookOnNext(Integer value) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Get value [" + value + "]");
                request(1);
            }
        });
}
```

## 总结

- 相对于传统的基于回调和Future的异步开发方式，响应式编程更加具有可编排性和可读性，配合lambda表达式，代码更加简洁，处理逻辑的表达就像装配“流水线”，适用于对数据流的处理；

- 在订阅（subscribe）时才触发数据流，这种数据流叫做“冷”数据流，就像插座插上电器才会有电流一样，还有一种数据流不管是否有订阅者订阅它都会一直发出数据，称之为“热”数据流，Reactor中几乎都是“冷”数据流；
- 调度器对线程管理进行更高层次的抽象，使得我们可以非常容易地切换线程执行环境；
- 灵活的错误处理机制有利于编写健壮的程序；
- “回压”机制使得订阅者可以无限接受数据并让它的源头“满负荷”推送所有的数据，也可以通过使用request方法来告知源头它一次最多能够处理 n 个元素，从而将“推送”模式转换为“推送+拉取”混合的模式。