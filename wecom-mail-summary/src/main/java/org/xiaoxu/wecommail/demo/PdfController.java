package org.xiaoxu.wecommail.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import org.xiaoxu.wecommail.service.PdfService;

import java.io.OutputStream;

@RestController
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping("/download/pdf")
    public void downloadPdf(HttpServletResponse response) {
        try (OutputStream out = response.getOutputStream()) {
            // 设置 Content-Type 和文件名
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"report.pdf\"");
            
            // 将响应输出流交给 service 生成 PDF
            pdfService.generatePdfToStream(out);
        } catch (Exception e) {
            // 生产环境建议记录日志，返回友好错误
            throw new RuntimeException("PDF 生成失败", e);
        }
    }
}