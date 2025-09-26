package org.xiaoxu.web_boot.service.excel;

import com.alibaba.excel.EasyExcel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.xiaoxu.web_boot.entity.excel.Customer;

import java.util.concurrent.TimeUnit;

/**
 * @className: ExcelImportService
 * @author: xiaoxu
 * @date: 2025/9/10 10:18
 * @Version: 1.0
 * @description:
 */
@Service
public class ExcelImportService {

    @Autowired
    @Qualifier("commonThreadPoolTaskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public void doImport(){
        String fileName = "D:/customer_data.xlsx";
        int numberSheet = 1;

//        threadPoolTaskExecutor.submit(()->{
//            EasyExcel.read(fileName, Customer.class,new ).sheet(numberSheet).doRead();
//        });


    }
}
