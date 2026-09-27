package org.xiaoxu.patterns.advanced.observer;

import java.math.BigDecimal;

/**
 * 事件：订单已创建。用 record 定义——不可变、自带 equals/hashCode/toString，
 * 事件对象理应是只读的数据快照。
 */
public record OrderCreatedEvent(String orderNo, String userId, BigDecimal amount) {
}
