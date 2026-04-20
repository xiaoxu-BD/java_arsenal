package org.xiaoxu.file;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import org.xiaoxu.pojo.dto.SystemUserDTO;

/**
 * @className: UserListener
 * @author: xiaoxu
 * @date: 2026/4/13 19:52
 * @Version: 1.0
 * @description:
 */
public class UserListener extends AnalysisEventListener<SystemUserDTO> {


    @Override
    public void invoke(SystemUserDTO systemUserDTO, AnalysisContext analysisContext) {

        System.out.println("读取的结果是: " + systemUserDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
