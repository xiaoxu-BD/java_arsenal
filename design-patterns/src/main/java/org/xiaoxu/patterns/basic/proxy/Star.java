package org.xiaoxu.patterns.basic.proxy;

/**
 * 明星接口（Subject）：代理和真明星都要实现它，调用方分不清谁是谁
 */
public interface Star {

    /** 面谈合作（琐事） */
    void negotiate();

    /** 唱歌（专业活） */
    void sing();
}
