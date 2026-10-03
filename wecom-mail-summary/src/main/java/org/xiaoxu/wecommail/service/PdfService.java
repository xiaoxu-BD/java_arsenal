package org.xiaoxu.wecommail.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.List;

@Service
public class PdfService {

    public void generatePdfToStream(OutputStream out) throws Exception {
        // 1. 创建 Document（A4 页面）
        Document document = new Document(PageSize.A4);
        
        try {
            // 2. 将 PdfWriter 绑定到响应输出流
            PdfWriter.getInstance(document, out);
            
            // 3. 打开文档（必须在写内容前）
            document.open();

            // 4. 加载中文字体（关键！否则中文不显示）
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bfChinese, 18, Font.BOLD);
            Font contentFont = new Font(bfChinese, 12, Font.NORMAL);

            // 5. 写入标题
            Paragraph title = new Paragraph("用户列表报告", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // 6. 创建表格（5 列）
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 1f, 1.5f, 1f, 1f}); // 列宽比例
            table.setSpacingBefore(20);

            // 7. 表头
            String[] headers = {"姓名", "年龄", "邮箱", "状态", "注册时间"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, titleFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                table.addCell(cell);
            }

            // 8. 动态数据行（此处可替换为从数据库查询）
            List<String[]> dataRows = List.of(
                    new String[]{"张三", "28", "zhangsan@example.com", "启用", "2024-01-15"},
                    new String[]{"李四", "32", "lisi@example.com", "禁用", "2024-03-22"},
                    new String[]{"王五", "25", "wangwu@example.com", "启用", "2024-05-08"}
            );

            for (String[] row : dataRows) {
                for (String value : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(value, contentFont));
                    cell.setPadding(6);
                    table.addCell(cell);
                }
            }

            // 9. 将表格添加到文档
            document.add(table);

        } finally {
            // 10. 关闭文档（触发 PDF 内容写入输出流）
            document.close();
        }
    }
}