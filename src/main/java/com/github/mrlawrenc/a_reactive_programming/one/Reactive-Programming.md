# 响应式编程

响应式编程（reactive programming）是一种基于数据流（data stream）和变化传递（propagation of change）的声明式（declarative）的编程范式。

## 变化传递

- 变化传递最为常见的例子当属Excel的计算公式。如求和SUM（a，b）公式，当a和b任意数据源改变，其结果会随之改变
- 观察者模式也是变化传递的一个体现，如java的swings、openjfx、springboot的event等。
- 变化传递（propagation of change）是响应式的核心特点之一。任意源改变之后之后，会像多米诺骨牌一样，导致直接和间接引用它的对象均发生相应变化。

## 数据流

- 如购物车。若有若干商品，每次往购物车里添加或移除一种商品，或调整商品的购买数量，这种事件都会像过电一样流过这由公式串起来的多米诺骨牌一次。这一次一次的操作事件连起来就是一串数据流。

- 如java8的Stream

  ```java
  List<String> list = IntStream.range(0, 1000)
      .mapToObj(i -> "hello java8" + i)
      .filter(s -> !s.contains("8"))
      .collect(toList());
  ```

  在这当中，流的源头由range操作产生，随后流会依次经过mapToObj、filter，最后经由collect收集，这其中的range、mapToObj、filter三者则构成了一串数据流。

- 基于数据流（data stream）是响应式的另一个核心特点

## 声明式

- 同样java8的stream也是一种声明式的编程风格
- 声明式的编程范式只会表达出要做什么事，而不关心具体怎么做，以不变应万变
- 这是响应式编程的第三个特点，声明式的编程风格（declarative）

## 总结

- 响应式编程（reactive programming）是一种基于数据流（data stream）和变化传递（propagation of change）的声明式（declarative）的编程范式。

## 附 

- 声明式和命令式

  [声明式和命令式区别](https://juejin.im/entry/5840317ca22b9d006c1007b8)

- 