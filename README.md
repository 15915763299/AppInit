### 知识点
+ com.demo.appinit.start.anchors.BaseTask
  1. Trace.beginSection()/Trace.endSection() 手动设置Systrace追踪
  2. Set元素如果是自定义类，需要重写hashCode/equals方法

+ com.demo.appinit.start.anchors.AnchorsRuntime
  1. ThreadPoolExecutor自定义线程池与参数的理解（对比AsyncTask）

+ com.demo.appinit.start.task.AwaitPermStartTask
  1. （弃用）使用LocalBroadcastManager来注册、发送本地广播
  > 弃用原因是BroadcastManager是应用级事件总线，越级了，官网建议我们自己选择一种观察者模式的实现。  
  > 关于如何选择，[这篇文章](https://juejin.im/post/5cbe81f75188250a85160d72)讲的很好。  
  > 最终选择[RxBus](https://github.com/Blankj/RxBus)进行事件监听的实现。  
  > 如果通讯频率高的话，建议使用[EventBus](https://github.com/greenrobot/EventBus)
  2. RxBus的使用方式、原理，粘性事件与非粘性事件。
  3. CountDownLatch的使用方法及原理。

+ com.demo.appinit.start.task.NetRequestTask
  1. 使用IdleHandler，在主线程空闲的时候执行任务



