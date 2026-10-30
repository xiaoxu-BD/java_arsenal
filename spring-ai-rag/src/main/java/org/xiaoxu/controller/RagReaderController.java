package org.xiaoxu.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.reader.DocumentReaderFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/rag")
@Slf4j
public class RagReaderController {

    @Autowired
    private DocumentReaderFactory documentReaderFactory;

    @RequestMapping("/read")
    public String read(String filePath) {
        List<Document> documents;
        try {
//            documents = DocumentCleaner.cleanDocuments(documentReaderFactory.read(new File(filePath)));
            documents = documentReaderFactory.read(new File(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(documents.size());
        StringBuffer sb = new StringBuffer();
        for (Document document : documents) {
            sb.append(document.getText());
            log.info("文档 details content:{}", document.getText());
            log.info("文档 details metadata:{}", document.getMetadata());
            System.out.println("========");
            sb.append("========================");
        }
        return sb.toString();
    }


}
