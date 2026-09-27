package org.xiaoxu.patterns.basic.factory;

/** 具体产品：正方形 */
public class Square implements Shape {

    @Override
    public void draw() {
        System.out.println("画一个正方形 □");
    }
}
