# Reactive Streams相关笔记



- spring5 在17年中下旬左右发布  java9差不多也是，但当时java的响应式编程规范并未完善，只是有一套标准接口和及少量的实现，因此spring采用了以reactor3为基础来构建web flux

- SSE与WebSocket有相似功能，都是用来建立浏览器与服务器之间的通信渠道。两者的区别在于：

  WebSocket是全双工通道，可以双向通信，功能更强；SSE是单向通道，只能服务器向浏览器端发送。
  WebSocket是一个新的协议，需要服务器端支持；SSE则是部署在 HTTP协议之上的，现有的服务器软件都支持。
  SSE是一个轻量级协议，相对简单；WebSocket是一种较重的协议，相对复杂。
  SSE默认支持断线重连，WebSocket则需要额外部署。
  SSE支持自定义发送的数据类型。

  SSE不支持CORS
  参数url就是服务器网址，必须与当前网页的网址在同一个网域（domain），而且协议和端口都必须相同。

  WebSocket支持
  