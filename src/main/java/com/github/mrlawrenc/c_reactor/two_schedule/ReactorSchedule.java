package com.github.mrlawrenc.c_reactor.two_schedule;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author : MrLawrenc
 * @date : 2020/5/30 23:44
 * @description : TODO
 * <p>
 * 当前线程（Schedulers.immediate()）；
 * 可重用的单线程（Schedulers.single()）。注意，这个方法对所有调用者都提供同一个线程来使用， 直到该调度器被废弃。如果你想使用独占的线程，请使用Schedulers.newSingle()；
 * 弹性线程池（Schedulers.elastic()）。它根据需要创建一个线程池，重用空闲线程。线程池如果空闲时间过长 （默认为 60s）就会被废弃。对于 I/O 阻塞的场景比较适用。Schedulers.elastic()能够方便地给一个阻塞 的任务分配它自己的线程，从而不会妨碍其他任务和资源；
 * 固定大小线程池（Schedulers.parallel()），所创建线程池的大小与CPU个数等同；
 * 自定义线程池（Schedulers.fromExecutorService(ExecutorService)）基于自定义的ExecutorService创建 Scheduler（虽然不太建议，不过你也可以使用Executor来创建）。
 */
public class ReactorSchedule {

    public static void main(String[] args) throws Exception {

        //on();

        parallel();

        TimeUnit.DAYS.sleep(1);
    }

    /**
     * publishOn(pool) 会改变后面一个操作所在的线程，后面操作只会在一个线程执行
     * subscribeOn(pool) 无论在何处，均只影响源头的线程
     */
    public static void on() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(111);

        Scheduler pool = Schedulers.newParallel(" pool ", 10);
        Flux.range(0, 5)
                //.log()
                .map(i -> {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("map1 : " + Thread.currentThread().getName());
                    return i / 2;
                })
                //.log()
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


    /**
     * 使用parallel（）之后，需要显示开启runOn，则后面任务均是异步的
     *
     * 且后面的第一个任务和后面的第二个使用的线程有关联，即一个线程从异步开始的第一个任务会执行到订阅结束
     */
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

    private static String getStringSync() {
        try {
            System.out.println("pub : " + Thread.currentThread().getName());
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Hello, Reactor!";
    }
}