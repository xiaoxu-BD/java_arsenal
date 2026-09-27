package org.xiaoxu.patterns.basic.factory;

/** 具体工厂：专门造圆形 */
public class CircleFactory extends ShapeFactory {

    @Override
    protected Shape create() {
        return new Circle();
    }
}
