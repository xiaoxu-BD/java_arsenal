package org.xiaoxu.web_boot.service.impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xiaoxu.web_boot.entity.NodeDesc;

/**
 * @className: JSONService
 * @author: xiaoxu
 * @date: 2025/7/6 16:46
 * @Version: 1.0
 * @description:
 */
@Service
@Slf4j
public class JSONService {

    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    public String receive(NodeDesc desc) {
//        return JSONUtil.toJsonStr(desc);
       return objectMapper.writeValueAsString(desc);
    }
    @SneakyThrows
    public NodeDesc parse(NodeDesc desc) {
//        return JSONUtil.toBean(JSONUtil.toJsonStr(desc), NodeDesc.class);
        return    objectMapper.readValue(objectMapper.writeValueAsString(desc), NodeDesc.class);
    }
}
