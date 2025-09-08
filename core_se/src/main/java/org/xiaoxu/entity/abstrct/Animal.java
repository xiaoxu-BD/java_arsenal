package org.xiaoxu.entity.abstrct;

/**
 * @className: Annimal
 * @author: xiaoxu
 * @date: 2025/9/5 9:29
 * @Version: 1.0
 * @description:
 */
// 抽象类：提供公共属性和方法，实现代码复用
public abstract class Animal {
    protected String name;  // 受保护，子类可访问

    public Animal(String name) {
        this.name = name;
    }

    // 通用方法：eat()
    public void eat() {
        System.out.println(name + " is eating.");
    }

    // 通用方法：sleep()
    public void sleep() {
        System.out.println(name + " is sleeping.");
    }

    // 抽象方法：每个动物叫声不一样，交给子类实现
    //抽象方法没有方法体,子类必须重写
    public abstract void makeSound();
}