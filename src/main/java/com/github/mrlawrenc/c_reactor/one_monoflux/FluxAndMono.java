package com.github.mrlawrenc.c_reactor.one_monoflux;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static reactor.core.publisher.Flux.zip;

/**
 * @author : MrLawrenc
 * @date : 2020/5/30 20:45
 * @description :
 * 更多reactor移步官网
 * @see <a herf="https://github.com/reactor/reactor-core>reactor github</a>
 */
@SuppressWarnings("all")
public class FluxAndMono {

    public static void main(String[] args) throws Exception {

        subscribe();
    }

    public static void acquaintance() {
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
    }

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


}