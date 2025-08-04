1. Spring中事务失效的场景: 
  - @Transactional标注到非public方法上
  - 方法内部自调用 (都是无法走到代理)
  - 没有设置正确的事务传播行为: 例如@Transactional(propagation = Propagation.NOT_SUPPORTED)
  - 设置的rollbackFor错误,如果只设置了rollbackFor = RuntimeException.class的话,这样发生 checked Exception就不会回滚;
  - 异常被捕获,会导致rollbackFor失效
  - 事务中使用多线程 (这个自己遇到过) 
    - 原因是: @Transactional的事务管理器使用的是ThreadLocal机制来存储事务上下文,而ThreadLocal变量是线程隔离的,每一个线程都有自己的事务上下文副本,
    - 所以: 多线程下,Spring声明式事务会失效,新线程中的操作不会被包含在原有的事务中;
