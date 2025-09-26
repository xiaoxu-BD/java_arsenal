package org.xiaoxu.web_boot;

import com.google.common.base.CaseFormat;
import org.junit.jupiter.api.Test;

/**
 * @className: StringTest
 * @author: xiaoxu
 * @date: 2025/9/16 9:23
 * @Version: 1.0
 * @description:
 */

public class StringTest {




    public static String getBeanName(String strategyName, String serviceName) {
        //将服务转换成小写字母开头的驼峰形式，如A_BCD 转成 aBcd

//        return CaseFormat.UPPER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL).convert(strategyName) + serviceName;
        return CaseFormat.UPPER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL).convert(strategyName) + serviceName;
    }

    public static void main(String[] args) {
        System.out.println(getBeanName("WE_CHAT", "ChainService"));
    }
}
