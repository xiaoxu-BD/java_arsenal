package org.xiaoxu.chainofRes.basic;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Context {

    private  String data;
    boolean stop = false; // 🚦 标记是否中断链条

    Context(String data) {
        this.data = data;
    }

    void stop() {
        this.stop = true;
    }

    boolean isStopped() {
        return stop;
    }
}
