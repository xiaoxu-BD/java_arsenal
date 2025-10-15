package org.xiaoxu.message;

import lombok.Data;

/**
 * @className: Demo1Message
 * @author: xiaoxu
 * @date: 2025/10/4 22:10
 * @Version: 1.0
 * @description:
 */
@Data
public class Demo1Message {

    public static final String TOPIC = "DEMO_01";

    /**
     * 编号
     */
    private Integer id;
}
