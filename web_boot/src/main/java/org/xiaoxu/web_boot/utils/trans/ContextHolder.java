package org.xiaoxu.web_boot.utils.trans;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.springframework.stereotype.Component;

/**
 * @className: ContextHolder
 * @author: xiaoxu
 * @date: 2025/9/4 15:46
 * @Version: 1.0
 * @description:
 */
@Component
public class ContextHolder {
    // 定义可传递的 ThreadLocal
    public static final TransmittableThreadLocal<String> CONTEXT = new TransmittableThreadLocal<>();
}
