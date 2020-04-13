# App启动优化
* 利用线程池异步执行各组件、SDK的启动。
* 自定义`BaseTask`实现自定义优先运行（锚点，最高优先级）、依赖运行（等待被依赖者运行完成才开始执行）。
* 整个异步过程可以在任意task前后进行阻断，等待权限申请完成，参考`AwaitPermStartTask`
* 在Application的onCreate方法中调用以下代码启动整个过程。
```
MainProcessStarter.start(checkPermission);
```
* 更多注释都在代码中（该框架暂称为anchors框架，是之前项目中的代码，我修改了其中的一些BUG，一些会造成误解的命名，整理了代码并加入大量注释，并完成了这个Demo）

### 部分知识点
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



