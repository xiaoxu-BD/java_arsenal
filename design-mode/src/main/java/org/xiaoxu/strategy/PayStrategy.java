package org.xiaoxu.strategy;

public interface PayStrategy {
    String getType();

    void pay(double amount);
}
