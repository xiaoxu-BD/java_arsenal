package org.xiaoxu.web_boot;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @className: JSONRevicwTest
 * @author: xiaoxu
 * @date: 2025/9/4 9:42
 * @Version: 1.0
 * @description:
 */
public class JSONReviewTest {

 static    String JSONStr = "{\n" +
            "  \"name\": \"李小明\",\n" +
            "  \"age\": 29,\n" +
            "  \"isVip\": true,\n" +
            "  \"birthday\": \"1995-06-18\",\n" +
            "  \"tags\": [\"技术宅\", \"运动\", \"摄影\"],\n" +
            "  \"address\": {\n" +
            "    \"province\": \"广东省\",\n" +
            "    \"city\": \"深圳市\",\n" +
            "    \"district\": \"南山区\",\n" +
            "    \"detail\": \"科技园南区深南大道10000号\"\n" +
            "  },\n" +
            "  \"orders\": [\n" +
            "    {\n" +
            "      \"orderId\": \"ORD10001\",\n" +
            "      \"amount\": 299.99,\n" +
            "      \"orderDate\": \"2024-01-15\",\n" +
            "      \"products\": [\n" +
            "        {\"productName\": \"无线耳机\", \"price\": 199.99, \"quantity\": 1},\n" +
            "        {\"productName\": \"充电宝\", \"price\": 100.00, \"quantity\": 1}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"orderId\": \"ORD10002\",\n" +
            "      \"amount\": 59.90,\n" +
            "      \"orderDate\": \"2024-01-16\",\n" +
            "      \"products\": [\n" +
            "        {\"productName\": \"数据线\", \"price\": 9.90, \"quantity\": 2}\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";


    public static void main(String[] args) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jsonObject = objectMapper.readValue(JSONStr, JSONObject.class);
        JSONArray orders = jsonObject.getJSONArray("orders");
        System.out.println(orders);


    }
}
