package org.xiaoxu.wecommail.utils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class PDFGenerateUtils {

    public static Document getPDF() {
        Document document = new Document();
        try {
            // 2. 创建输出流（这里直接写本地文件，测试用）
            //    换成 response.getOutputStream() 就能在 Spring 里用
            PdfWriter.getInstance(document, new FileOutputStream("local-test.pdf"));

            // 3. 打开文档，开始写内容
            document.open();
            document.add(new Paragraph("Hello World，这是一个本地测试 PDF。"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        return document;
    }
}