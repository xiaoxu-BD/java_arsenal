package org.xiaoxu.designpattern.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PdfReportGenerator implements ReportGenerator {

    @Override
    public String type() {
        return "PDF";
    }

    @Override
    public byte[] generate(String data) {
        log.info("【报表】生成PDF报表: data={}", data);
        return ("PDF:" + data).getBytes();
    }
}
