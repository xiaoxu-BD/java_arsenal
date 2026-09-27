package org.xiaoxu.patterns.basic.factory;

/** 具体工厂：专门造正方形 */
public class SquareFactory extends ShapeFactory {

    @Override
    protected Shape create() {
        return new Square();
    }
}
