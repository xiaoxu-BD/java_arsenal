package org.xiaoxu.designpattern.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExcelReportGenerator implements ReportGenerator {

    @Override
    public String type() {
        return "EXCEL";
    }

    @Override
    public byte[] generate(String data) {
        log.info("【报表】生成Excel报表: data={}", data);
        return ("EXCEL:" + data).getBytes();
    }
}
