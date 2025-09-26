package org.xiaoxu.web_boot.listener;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @className: MyAppReadyListener
 * @author: xiaoxu
 * @date: 2025/8/5 20:08
 * @Version: 1.0
 * @description:
 */
@Component
public class MyAppReadyListener {

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        System.out.println("✅ SpringBoot 启动完成，bloomFilter 被触发！");
    }
}
