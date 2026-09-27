package org.xiaoxu.patterns.basic.factory;

/** 具体产品：三角形 */
public class Triangle implements Shape {

    @Override
    public void draw() {
        System.out.println("画一个三角形 △");
    }
}
