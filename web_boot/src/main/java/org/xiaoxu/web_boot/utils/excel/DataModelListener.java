package org.xiaoxu.web_boot.utils.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import org.xiaoxu.web_boot.entity.excel.Customer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @className: DataModelListener
 * @author: xiaoxu
 * @date: 2025/9/10 10:24
 * @Version: 1.0
 * @description:
 */
public class DataModelListener implements ReadListener<Customer> {

    //设置批量处理的数据大小
    private static final int BATCH_SIZE = 1000;
    //用于暂存读取的数据，直到达到批量大小
    private List<Customer> batch = new ArrayList<>();

    @Override
    public void invoke(Customer customer, AnalysisContext analysisContext) {

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
