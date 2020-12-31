# RiseDemo
工作中，学习中遇到一些问题的解决

//共享内存(shared memory)的实现
MemoryFile是android在最开始就引入的一套框架，其内部实际上是封装了android特有的内存共享机制Ashmem匿名共享内存，
简单来说，Ashmem在Android内核中是被注册成一个特殊的字符设备，Ashmem驱动通过在内核的一个自定义slab缓冲区中初始化一段内存区域，
然后通过mmap把申请的内存映射到用户的进程空间中（通过tmpfs），这样子就可以在用户进程中使用这里申请的内存了，
另外，Ashmem的一个特性就是可以在系统内存不足的时候，回收掉被标记为"unpin"的内存，这个后面会讲到，
另外，MemoryFile也可以通过Binder跨进程调用来让两个进程共享一段内存区域。
由于整个申请内存的过程并不再Java层上，可以很明显的看出使用MemoryFile申请的内存实际上是并不会占用Java堆内存的。
MemoryFileDemo copy from:https://github.com/yifei8/ShareMemDemo


关于进程通信：
文件共享、Binder、ContentProvider、Messenger+Handler、socket
SharedPreferences不支持多进程

多线程交互：
Handler、AsyncTask

线程间的通信方式
锁机制：包括互斥锁、条件变量、读写锁
互斥锁提供了以排他方式防止数据结构被并发修改的方法。
读写锁允许多个线程同时读共享数据，而对写操作是互斥的。
条件变量可以以原子的方式阻塞进程，直到某个特定条件为真为止。对条件的测试是在互斥锁的保护下进行的。条件变量始终与互斥锁一起使用。

wait/notify 等待

Volatile 内存共享

CountDownLatch 并发工具

CyclicBarrier 并发工具

信号量机制(Semaphore)
包括无名线程信号量和命名线程信号量。

信号机制(Signal)
类似进程间的信号处理。

线程间的通信目的主要是用于线程同步，所以线程没有像进程通信中的用于数据交换的通信机制