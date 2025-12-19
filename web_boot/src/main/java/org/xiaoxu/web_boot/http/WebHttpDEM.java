package org.xiaoxu.web_boot.http;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @className: WebHttpDEM
 * @author: xiaoxu
 * @date: 2025/12/19 23:50
 * @Version: 1.0
 * @description:
 */
public class WebHttpDEM {


    private static final String URL = "http://localhost:9191/api/log";
    private static final Logger log = LoggerFactory.getLogger(WebHttpDEM.class);

    public static void main(String[] args) {
        String response = getResponse();
        log.info("使用Hutools工具获取远程请求的返回结果:{}", response);
    }


    public static String getResponse(){

        Map<String, String> map = new HashMap<>();

        map.put("name", "xiaoxu");

        // 对于GET请求，参数应该作为查询参数传递，而不是请求体
        HttpResponse execute = HttpRequest.get(URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer-1234567890")
                .form(JSON.toJSONString(map))  // 使用form方法将参数作为查询参数传递
                .execute();

        String responseBody = execute.body();
        if (StringUtils.isNotBlank(responseBody)){
            return responseBody;
        }else
            return "请求失败";

    }
}
