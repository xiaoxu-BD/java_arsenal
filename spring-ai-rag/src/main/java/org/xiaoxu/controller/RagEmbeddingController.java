package org.xiaoxu.controller;

import com.alibaba.cloud.ai.transformer.splitter.RecursiveCharacterTextSplitter;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.embedding.EmbeddingService;
import org.xiaoxu.reader.DocumentReaderFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/rag/embedding")
public class RagEmbeddingController {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private DocumentReaderFactory documentReaderFactory;


    @Autowired
    private EmbeddingService embeddingService;

    @RequestMapping("/test")
    public String test() {
        for (float i : embeddingModel.embed("test")) {
            System.out.println(i);
        }
        return "success";
    }



    @RequestMapping("embed")
    public String embed(@PathParam("filePath") String filePath) {
        List<Document> documents;
        try {
            documents = documentReaderFactory.read(new File(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Document> allChunkedDocuments = documents.stream()
                .flatMap(document -> {
                    RecursiveCharacterTextSplitter splitter = new RecursiveCharacterTextSplitter(300, new String[]{"\n\n", "\n"});
                    return splitter.split(document).stream();
                })
                .collect(Collectors.toList());
        embeddingService.embedAndStore(allChunkedDocuments);
        return "success";
    }


    @RequestMapping("chunk")
    public String chunk(@PathParam("filePath") String filePath) {
        List<Document> documents;
        try {
            documents = documentReaderFactory.read(new File(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<float[]> floatsDocuments = embeddingService.embed(documents);
        return "success";
    }

}