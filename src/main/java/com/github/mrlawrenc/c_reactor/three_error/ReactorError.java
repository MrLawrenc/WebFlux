package com.github.mrlawrenc.c_reactor.three_error;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author : MrLawrenc
 * @date : 2020/5/31 15:25
 * @description : TODO
 */
public class ReactorError {

    public static void main(String[] args) {
        //error();
        System.out.println("=======================");
        //error1();

        //背压机制
        backpressure();
    }

    /**
     * 捕获并返回一个静态的缺省值
     */
    public static void error() {
        Flux.range(1, 6)
                .map(i -> 10 / (i - 3))
                .onErrorReturn(0)
                .map(i -> i * i)
                .subscribe(System.out::println, System.err::println);
    }

    /**
     * 捕获并执行一个异常处理方法或计算一个候补值来顶替
     */
    public static void error1() {
        Flux.range(1, 6)
                .map(i -> 10 / (i - 3))
                // 提供新的数据流
                .onErrorResume(e -> Mono.just(new Random().nextInt(6)))
                .map(i -> i * i)
                .subscribe(System.out::println, System.err::println);
    }

    /**
     * 捕获，并再包装为某一个业务相关的异常，然后再抛出业务异常
     */
    public static void error2() {
     /*   Flux.just("timeout1")
                .flatMap(k -> callExternalService(k))   // 1
                .onErrorMap(original -> new BusinessException("SLA exceeded", original)); // 2*/

    }

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
}