package org.xiaoxu.component;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @className: TaskServiceImpl
 * @author: xiaoxu
 * @date: 2025/11/19 14:55
 * @Version: 1.0
 * @description:
 */
@Service
public class TaskServiceImpl implements TaskService2{
    @Override
    public void executeTask() {
        System.out.println("正在执行的代理对象......");
    }



}
