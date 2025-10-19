package org.xiaoxu.entity;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @className: Serializable
 * @author: xiaoxu
 * @date: 2025/7/30 7:37
 * @Version: 1.0
 * @description:
 */
@Data
@ToString
public class PersonSerializable implements Serializable {
    private String name;
    ///* transient修饰的属性不会被序列化 */
    private  transient String password;
    private int age;
}
