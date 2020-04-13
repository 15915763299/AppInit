# App启动优化
* 代码是在一个项目中拿到的，主要实现逻辑在`com.demo.appinit.anchors`中，修改了其中的一些BUG，一些会造成误解的命名，整理了代码并加入大量注释，并完成了这个Demo
* 经过不断地查找，找到了最初项目[开源的地方](https://github.com/YummyLau/Anchors)

### 使用方式
* 自定义Task继承`BaseTask`，实现一个任务的执行。
* 使用`Project.Builder`创建Task图。
* 使用自定义工厂创建对应的Task，工厂继承自`Project.TaskFactory`。
* 在这个示例中，在Application的onCreate方法中调用以下代码启动整个过程。
```
MainProcessStarter.start(checkPermission);
```
* 更多注释都在代码中

### 原理解释
##### BaseTask
* 它是一个任务单元，其中定义了前置任务`dependTasks`与后置任务`behindTasks`。
* 当向taskB中加入一个前置条件taskA时，taskA会被加入taskB的前置任务`dependTasks`，同时taskB也会被加入taskA的后置任务`behindTasks`中。
* 当向taskB中加入一个后置条件taskC时，taskC会被加入taskB的后置任务`behindTasks`，同时taskB也会被加入taskC的前置任务`dependTasks`中。
* 当一个task执行完成时，会将自己的后置任务`behindTasks`逐个启动，此时后置的任务会判断自己还有没有前置的任务，如果有就不执行，没有才执行。
##### Project
* Project的存在意义在于他的Builder，他能构造一个链式依赖的task链，就是一一依赖，最终会逐个执行。
##### AnchorsManager
* `BaseTask`是不能直接调用`start`方法执行的，必须通过`AnchorsManager`的`start`才能执行，它定义了如何正确地执行一个task。
* 从`start`方法可以看出，锚点任务都会在`start`所在方法内执行完（比如说我在onCreate中调用了start，则锚点任务都会在onCreate方法中执行）
##### AnchorsRuntime
* 会对整个执行过程记录信息，并打印出log
* 管理着一个线程池，用于执行异步任务
##### LockableTask & LockableAnchor
* 可以通用过这两个类实现整个执行过程的阻塞，当然也可以自定义，比如`AwaitPermStartTask`

### 为什么会变快
首先看到每个task的启动方法`start`
```
    /**
     * 调用start启动当前task
     */
    protected synchronized void start() {
        if (mState != TaskState.IDLE) {
            throw new RuntimeException("can no run task " + getId() + " again!");
        }
        toStart();
        setExecuteTime(System.currentTimeMillis());
        AnchorsRuntime.executeTask(this);
    }
```
最终都会调用到`AnchorsRuntime.executeTask(this);`，如下：
```
    /**
     * 同步使用handler发送至主线程，异步使用线程池
     * @param task 任务
     */
    static void executeTask(BaseTask task) {
        if (task.isAsyncTask()) {
            S_POOL.executeTask(task);
        } else {
            if (AnchorsRuntime.hasAnchorTasks()) {
                AnchorsRuntime.addRunTasks(task);
            } else {
                sHandler.post(task);
            }
        }
    }
```
* 首先异步执行当然会减少启动时间，那如果全部都是同步执行呢？经过测试，启动过程也是变快了的。
* 最终启动变快的原因就在于这个`sHandler.post(task);`（`sHandler`的Looper是主线程的Looper）。
* 当每个task执行时，会放入主线程的消息队列的末尾，相当于不是立即执行，而是等待主线程现有的任务执行完了才执行，相当于给App启动“让路”。
* 从整体来看，大部分启动任务都后移了，App启动的任务会被稍微“提前”，所以达到了优化的效果。

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



