package org.xiaoxu.web_boot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;

/**
 * @className: JsonFormat
 * @author: xiaoxu
 * @date: 2025/7/5 16:49
 * @Version: 1.0
 * @description:
 */
public class JsonFormat {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 入口方法：处理 params 字符串，返回格式化 JSON 字符串
     */
    public static String formatParams(String paramsStr) {
        try {
            // 第一步：解析外层 JSON
            JsonNode root = mapper.readTree(paramsStr);

            // 第二步：递归解析内层嵌套字段中是 JSON 字符串的部分
            deepParseJsonStrings((ObjectNode) root);

            // 第三步：格式化为可视化字符串
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);

        } catch (Exception e) {
            return "解析失败: " + e.getMessage();
        }
    }

    /**
     * 遍历所有字段，将字符串形式的 JSON 自动解析为嵌套 JSON 对象
     */
    private static void deepParseJsonStrings(ObjectNode node) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();

            if (value.isObject()) {
                // 递归处理子对象
                deepParseJsonStrings((ObjectNode) value);
            } else if (value.isTextual()) {
                String text = value.asText();
                String cleaned = removeJsonComments(text);

                // 尝试解析成 JSON
                try {
                    JsonNode parsed = mapper.readTree(cleaned);
                    node.set(key, parsed); // 替换成 JSON 对象
                    // 递归继续解析里面的字段
                    if (parsed.isObject()) {
                        deepParseJsonStrings((ObjectNode) parsed);
                    }
                } catch (Exception ignored) {
                    // 忽略不能解析成 JSON 的字段
                }
            }
        }
    }

    /**
     * 去除 JSON 字符串中的行内注释（//）
     */
    private static String removeJsonComments(String json) {
        // 只处理每一行中 // 开头的注释，保留代码逻辑
        return json.replaceAll("(?m)//.*", "");
    }

    // 测试用 main 方法
    public static void main(String[] args) {
        String rawParams = "{ \"parameters\": { \"topic\": \"12312\", \"jsonParameters\": true, \"messageJson\": \"{\\n  \\\"code\\\": 0,     // 2131231\\n  \\\"msg\\\": \\\"ok\\\",     // 张金海1\\n  \\\"count\\\": 0,\\n  \\\"data\\\": {\\n    \\\"permissions\\\": true,\\n    \\\"allCount\\\": 6,     // 年度问题数量\\n    \\\"rectificationCount\\\": 3,     // 整改数量\\n    \\\"notRectificationCount\\\": 3     // 未整改数量\\n  },\\n  \\\"define\\\": {},\\n  \\\"ok\\\": true\\n}\" }, \"credentials\": { \"kafka\": { \"clientId\": \"12312\", \"brokers\": \"1231\", \"ssl\": false, \"authentication\": false, \"username\": \"\", \"password\": \"\", \"saslMechanism\": \"\" } } }";

        String formatted = formatParams(rawParams);
        System.out.println(formatted);
    }
}
