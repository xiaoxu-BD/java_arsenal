package org.xiaoxu.se.enums;

/**
 * 枚举实现接口
 */
public enum Color implements Printable {

    RED("#FF0000"), GREEN("#00FF00"), BLUE("#0000FF");

    private final String hex;

    Color(String hex) { this.hex = hex; }

    @Override
    public void printInfo() {
        System.out.println(name() + " = " + hex);
    }
}
