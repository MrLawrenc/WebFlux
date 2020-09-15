package com.github.mrlawrenc.b_java9;

import lombok.SneakyThrows;

import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

/**
 * @author : MrLawrenc
 * @date : 2019/10/14 1:54
 * @description : TODO
 */
public class SimpleFlowDemo {
    public static void main(String[] args) throws Exception {
        // 1. 定义发布者, 发布的数据类型是 Integer,该发布者SubmissionPublisher是jdk Publisher的一个实现
        //  SubmissionPublisher<Integer> publiser = new SubmissionPublisher<>();
         SubmissionPublisher<Integer> publiser = new SubmissionPublisher<>(Executors.newFixedThreadPool(10), 4);



        // 2. 定义订阅者
        Flow.Subscriber<Integer> subscriber = new MySub();
        // 3. 发布者和订阅者 建立订阅关系
        publiser.subscribe(subscriber);

        // 4. 生产数据, 并发布
        // 这里忽略数据生产过程
        for (int i = 0; i < 20; i++) {
            System.out.println("生成数据:" + i);
            publiser.submit(i);
        }

        // 5. 结束后 关闭发布者
        // 正式环境 应该放 finally 或者使用 try-~resouce~ 确保关闭,如果结束发布没有关闭，那么消费者可能一直等待
        publiser.close();

        // 主线程延迟停止, 否则数据没有消费就退出
        Thread.currentThread().join(10000000);
    }
}

/**
 * 消费者
 */
class MySub implements Flow.Subscriber<Integer> {

    /**
     * Pub 和 Sub 通过Subscription 沟通
     */
    private Flow.Subscription subscription;


    /**
     * 建立订阅关系
     */
    @SneakyThrows
    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        System.out.println("onSubscribe====");
        // 保存订阅关系, 需要用它来给发布者响应
        this.subscription = subscription;
        // 会阻塞知道当pub发布数据
        this.subscription.request(2);
    }


    /**
     * 接收数据
     */
    @SneakyThrows
    @Override
    public void onNext(Integer item) {
        this.subscription.request(1);
        TimeUnit.MILLISECONDS.sleep(10);
        System.out.println("接受到数据: " + item );
        // 接受到一个数据, 处理
        // 处理完调用request再请求一个数据


        TimeUnit.MILLISECONDS.sleep(1000);
        // 或者 已经达到了目标, 调用cancel告诉发布者不再接受数据了
        // this.subscription.cancel();
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("onError");
        // 出现了异常(例如处理数据的时候产生了异常)
        throwable.printStackTrace();

        // 我们可以告诉发布者, 后面不接受数据了
        this.subscription.cancel();
    }

    @Override
    public void onComplete() {
        // 全部数据处理完了(发布者关闭了)
        System.out.println("处理完了!");
        subscription.cancel();
    }
}