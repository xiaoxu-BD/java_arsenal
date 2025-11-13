package org.xiaoxu.json;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 * @className: DEM
 * @author: xiaoxu
 * @date: 2025/11/10 18:22
 * @Version: 1.0
 * @description:
 */
public class DEM {
    public static void main(String[] args) {
        String jsonStr = "{\n" +
                "  \"department\": \"技术部\",\n" +
                "  \"manager\": {\n" +
                "    \"name\": \"王五\",\n" +
                "    \"age\": 35,\n" +
                "    \"email\": \"wangwu@example.com\"\n" +
                "  },\n" +
                "  \"employees\": [\n" +
                "    {\n" +
                "      \"id\": 101,\n" +
                "      \"name\": \"张三\",\n" +
                "      \"skills\": [\"Java\", \"Spring\", \"MySQL\"]\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 102,\n" +
                "      \"name\": \"李四\",\n" +
                "      \"skills\": [\"Vue\", \"Node.js\"]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"active\": true\n" +
                "}";
//        JSONObject root = new JSONObject(jsonStr);
//        Object manager = root.get("manager");
//        System.out.println( manager);
//        Object employees = root.get("employees");
//        if (employees instanceof JSONArray){
//
//            for (Object employee : (JSONArray) employees) {
//                System.out.println(employee);
//            }
//        }

        JSONObject jsonObject = JSON.parseObject(jsonStr);
        Object employees = jsonObject.get("employees");
        System.out.println(employees);

        JSONArray employeess = jsonObject.getJSONArray("employees");
        employeess.get(0);
        System.out.println(employeess.get(0));

        //{} 是JSONObject 类似 Map<String,Object>
        //[] 是JSONArray  类似List<Object> List<List<Object>>
    }


}
