package org.xiaoxu.chunkupload.controller;

import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ai.ppocr.autoconfigure.PPOcrTemplate;
import net.dreamlu.mica.ai.ppocr.engine.PPOcrV6Result;
import net.dreamlu.mica.ai.ppocr.structured.parser.business.BusinessLicenseResult;
import net.dreamlu.mica.ai.ppocr.structured.parser.idcard.IdCardResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OCR 识别接口（mica-ppocr，本地 ONNX 推理，无需外部 API）。
 * 模拟上传身份证 / 营业执照图片，返回识别出的结构化 JSON。
 */
@RestController
@RequestMapping("/api/ocr")
@Slf4j
public class OcrController {


    private final PPOcrTemplate ppocr;

    public OcrController(PPOcrTemplate ppocr) {
        this.ppocr = ppocr;
    }

    /** 身份证识别（人像面 / 国徽面自动判定） */
    @PostMapping("/id-card")
    public Map<String, Object> idCard(@RequestParam("file") MultipartFile file) throws IOException {
        IdCardResult r = ppocr.idCard().parse(file.getBytes());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("side", r.getSide());
        data.put("name", r.getName());
        data.put("gender", r.getGender());
        data.put("nation", r.getNation());
        data.put("birthDate", r.getBirthDate());
        data.put("address", r.getAddress());
        data.put("idNumber", r.getIdNumber());
        data.put("issuingAuthority", r.getIssuingAuthority());
        data.put("validFrom", r.getValidFrom());
        data.put("validTo", r.getValidTo());
        data.put("rawText", toJson(r.getRawResults()));

        log.info("idNumber is {}",r.getIdNumber());
        return data;
    }

    /** 营业执照识别 */
    @PostMapping("/business-license")
    public Map<String, Object> businessLicense(@RequestParam("file") MultipartFile file) throws IOException {
        BusinessLicenseResult r = ppocr.businessLicense().parse(file.getBytes());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("creditCode", r.getCreditCode());
        data.put("name", r.getName());
        data.put("type", r.getType());
        data.put("legalPerson", r.getLegalPerson());
        data.put("registeredCapital", r.getRegisteredCapital());
        data.put("establishDate", r.getEstablishDate());
        data.put("operatingPeriod", r.getOperatingPeriod());
        data.put("address", r.getAddress());
        data.put("businessScope", r.getBusinessScope());
        data.put("rawText", toJson(r.getRawResults()));
        return data;
    }

    /** 通用文字识别：不做结构化，返回散落的文字框 */
    @PostMapping("/raw")
    public List<Map<String, Object>> raw(@RequestParam("file") MultipartFile file) throws IOException {
        return toJson(ppocr.run(file.getBytes()));
    }

    /** PPOcrV6Result 是 fluent 访问器（text()/score()，无 getter），Jackson 序列化不了，统一转 Map */
    private static List<Map<String, Object>> toJson(List<PPOcrV6Result> results) {
        return results.stream()
                .map(r -> Map.<String, Object>of(
                        "text", r.text(),
                        "score", r.score(),
                        "box", r.boxAsNestedList()))
                .toList();
    }
}
