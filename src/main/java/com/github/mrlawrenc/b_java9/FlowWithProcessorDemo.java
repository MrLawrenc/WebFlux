package com.github.mrlawrenc.b_java9;


import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;


/**
 * 带有“中间者”的发布订阅模型
 *
 * @author MrLawrenc
 * date  2019/10/20 13:14
 * <p>
 * <p>
 * 响应式流的流程 也是 代理模式/装饰器模式的体现
 * <p>
 * 每个 Subscriber 都可以被包装  当产生订阅关系的时候
 * <pre>
 *     publiser.subscribe(processor);
 * </pre>
 * @see SubmissionPublisher#subscribe(java.util.concurrent.Flow.Subscriber) 内部会有缓存处理器
 * <pre>
 *       BufferedSubscription<T> subscription =
 *             new BufferedSubscription<T>(subscriber, executor, onNextHandler,
 *                                         array, max);
 * </pre>
 * @see SubmissionPublisher.BufferedSubscription
 *
 *
 * 当pub发布数据的时候，会先进缓存
 * {@link SubmissionPublisher#submit(java.lang.Object)}  ->
 * {@link SubmissionPublisher#doOffer(java.lang.Object, long, java.util.function.BiPredicate)}
 * <pre>
 *      int stat = b.offer(item, unowned);
 * </pre>
 *
 * 场景化:
 * 1. pub和sub建立关系后
 * 2. pub告诉sub，数据我放到BufferedSubscription这里，取数据直接找他
 * 3. BufferedSubscription 是 缓存实现的背压机制的体现
 */
public class FlowWithProcessorDemo {

    public static void main(String[] args) throws Exception {
        // 1. 定义发布者, 发布的数据类型是 Integer
        SubmissionPublisher<Integer> publiser = new SubmissionPublisher<Integer>();

        // 2. 定义处理器, 对数据进行过滤, 并转换为String类型
        MyProcessor processor = new MyProcessor();

        // 3. 发布者 和 中间者 处理器 建立订阅关系
        publiser.subscribe(processor);

        // 4. 定义最终订阅者, 消费 String 类型数据
        Subscriber<String> subscriber = new Subscriber<String>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                this.subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                System.out.println("result : " + item);
                this.subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
                this.subscription.cancel();
            }

            @Override
            public void onComplete() {
                System.out.println("finish !");
            }
        };

        // 5. 处理器 和 最终订阅者 建立订阅关系
        processor.subscribe(subscriber);

        try {
            // 6. 生产数据, 并发布
            publiser.submit(-111);
            publiser.submit(111);
            publiser.submit(1111);
            publiser.submit(11111);
        } finally {
            publiser.close();
        }
        Thread.currentThread().join(1000);
    }
}

/**
 * Processor, 需要继承SubmissionPublisher并实现Processor接口
 * 带 process 的 flow demo
 * 输入源数据 integer, 过滤掉小于0的, 然后转换成字符串发布出去
 */
class MyProcessor extends SubmissionPublisher<String>
        implements Processor<Integer, String> {

    private Subscription subscription;

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(Integer item) {
        System.out.println("processor accept : " + item);
        if (item > 0) {
            this.submit("This is the value that we changed --> " + item);
        }
        this.subscription.request(1);

    }

    @Override
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
        this.subscription.cancel();
    }

    @Override
    public void onComplete() {
        System.out.println("processor done");
        this.close();
    }

}