package org.xiaoxu.entity.sub;

import org.xiaoxu.entity.abstrct.Animal;

/**
 * @className: Bird
 * @author: xiaoxu
 * @date: 2025/9/5 9:31
 * @Version: 1.0
 * @description:
 */
public class Bird extends Animal {
    public Bird(String name) {
        super(name);
    }

    @Override
    public void sleep() {
        System.out.println("鸟不睡觉");
    }



    @Override
    public void makeSound() {
        System.out.println("鸟的叫声");
    }
}
