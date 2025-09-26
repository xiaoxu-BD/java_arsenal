package org.xiaoxu.web_boot.utils.excel;

import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import org.xiaoxu.web_boot.entity.excel.Customer;
import org.xiaoxu.web_boot.utils.ExcelStyleHandler;
import org.xiaoxu.web_boot.utils.RandomDataUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @className: ExcelGenerator
 * @author: xiaoxu
 * @date: 2025/9/10 9:50
 * @Version: 1.0
 * @description:
 */
public class ExcelGenerator {


    public static void main(String[] args) {
        String fileName = "D:/customer_data.xlsx";
        int total = 1_000_000; // 100万条
        int batchSize = 10_000; // 每批写入1万条

        // 创建流式写入对象
        var writer = EasyExcel.write(fileName, Customer.class).registerWriteHandler(ExcelStyleHandler.defaultStyleHandler()).build();
        var sheet = EasyExcel.writerSheet("客户数据generator").build();

        for (int i = 0; i < total; i += batchSize) {
            List<Customer> customers = new ArrayList<>();
            for (int j = 0; j < batchSize && i + j < total; j++) {
                Customer customer = new Customer();
                customer.setId(IdUtil.simpleUUID());
                customer.setName(RandomDataUtil.randomName());
                customer.setPhone(RandomDataUtil.randomPhone());
                customer.setEmail(RandomDataUtil.randomEmail());
                customers.add(customer);
            }
            writer.write(customers, sheet); // 流式写入
            System.out.println("已写入 " + (i + batchSize) + " 条");
        }

        writer.finish(); // 一定要关闭流，否则文件可能损坏
        System.out.println("Excel 文件生成完成！");
    }
}
