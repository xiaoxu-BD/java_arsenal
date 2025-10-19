package org.xiaoxu;

import com.google.common.base.CaseFormat;

/**
 * @className: BeanNameUtil
 * @author: xiaoxu
 * @date: 2025/9/16 9:20
 * @Version: 1.0
 * @description:
 */
public class BeanNameUtil {



    public static String getBeanName(String strategyName, String serviceName) {
        //将服务转换成小写字母开头的驼峰形式，如A_BCD 转成 aBcd
        return CaseFormat.UPPER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL).convert(strategyName) + serviceName;
    }
}
