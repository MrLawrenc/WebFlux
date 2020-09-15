# Java9对于响应式的支持

## JUC下的响应式规范

```java
package java.util.concurrent;
/**
 *
 * @author Doug Lea
 * @since 9
 */
public final class Flow {

    private Flow() {} // uninstantiable


    @FunctionalInterface
    public static interface Publisher<T> {
        public void subscribe(Subscriber<? super T> subscriber);
    }

    public static interface Subscriber<T> {

        public void onSubscribe(Subscription subscription);
        public void onNext(T item);
        public void onError(Throwable throwable);
        public void onComplete();
    }

    /**
     * Pub和Sub桥梁
     */
    public static interface Subscription {
        public void request(long n);
        public void cancel();
    }

	/**
     * 处理器，中间者，例如map() filter()等
     */
    public static interface Processor<T,R> extends Subscriber<T>, Publisher<R> {
    }

    static final int DEFAULT_BUFFER_SIZE = 256;

    public static int defaultBufferSize() {
        return DEFAULT_BUFFER_SIZE;
    }

}
```



## Demo

- 不带自定义中间处理器

  ```java
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
  ```

  

- 带有中间处理器

  ```java
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
  ```

  

