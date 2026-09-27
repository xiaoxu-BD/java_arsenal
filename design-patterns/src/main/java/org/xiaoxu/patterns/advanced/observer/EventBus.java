package org.xiaoxu.patterns.advanced.observer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 通用事件总线（高级版观察者）
 *
 * <p>basic 版的天气站是"专用"观察者：Subject 写死推两个参数。
 * 工程上需要的是<b>通用</b>机制：任意事件类型、可增删订阅、互不拖累。
 *
 * <p>三个工程化设计：
 * <ol>
 *   <li><b>按事件类型订阅</b>：Map&lt;事件Class, 订阅者列表&gt;，事件对象自己携带数据；</li>
 *   <li><b>线程安全</b>：ConcurrentHashMap + CopyOnWriteArrayList，注册和发布可以并发；</li>
 *   <li><b>异常隔离</b>：一个订阅者炸了，其余照常收到事件（广播语义的基本责任）。</li>
 * </ol>
 *
 * <p>Guava EventBus、Spring ApplicationEvent、MQ 广播模式的结构与此同源。
 */
public class EventBus {

    /** 一条订阅：事件类型 + 处理动作。用 record 而不是裸 Consumer，注销时才能按原引用比对 */
    private record Subscription(Class<?> eventType, Consumer<?> handler) {
    }

    /** 事件类型 -> 订阅列表。发布期只读、注册期少量写，COW 列表正合适 */
    private final Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscribers = new ConcurrentHashMap<>();

    /** 订阅：指定事件类型 + 处理动作 */
    public <T> void register(Class<T> eventType, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(new Subscription(eventType, handler));
    }

    /** 注销（返回是否真的删掉了） */
    public <T> boolean unregister(Class<T> eventType, Consumer<T> handler) {
        CopyOnWriteArrayList<Subscription> list = subscribers.get(eventType);
        if (list == null) {
            return false;
        }
        return list.removeIf(s -> s.eventType() == eventType && s.handler().equals(handler));
    }

    /** 发布：同步通知该类型的所有订阅者；单个订阅者异常不影响其他订阅者 */
    @SuppressWarnings("unchecked")
    public void post(Object event) {
        List<Subscription> list = subscribers.get(event.getClass());
        if (list == null) {
            return;
        }
        for (Subscription subscription : list) {
            try {
                ((Consumer<Object>) subscription.handler()).accept(event);
            } catch (Exception e) {
                System.out.println("[EventBus] 订阅者处理 " + event.getClass().getSimpleName()
                        + " 失败(已隔离): " + e.getMessage());
            }
        }
    }
}
