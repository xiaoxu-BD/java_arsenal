package org.xiaoxu.web_boot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class WebBootApplicationTests {

	@Test
	void contextLoads() {
	}


		private static final ObjectMapper mapper = new ObjectMapper();

		/**
		 * 解析未知结构 JSON 字符串（params），自动处理嵌套的字符串 JSON
		 */
		public static String parseAndFormatParams(String paramsStr) {
			try {
				// 第 1 步：反序列化为树结构（不需要 DTO 类）
				JsonNode root = mapper.readTree(paramsStr);
				// 第 2 步：尝试将所有子字段中是“字符串 JSON”的值，自动再解析一次
				deepParseEmbeddedJson((ObjectNode) root);
				// 第 3 步：格式化输出
				return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
			} catch (Exception e) {
				return "解析失败: " + e.getMessage();
			}
		}

		/**
		 * 递归地尝试解析所有字符串类型的字段为嵌套 JSON
		 */
		private static void deepParseEmbeddedJson(ObjectNode node) {
			node.fieldNames().forEachRemaining(fieldName -> {
				JsonNode valueNode = node.get(fieldName);
				if (valueNode.isObject()) {
					// 递归嵌套结构
					deepParseEmbeddedJson((ObjectNode) valueNode);
				} else if (valueNode.isTextual()) {
					String text = valueNode.asText();
					try {
						JsonNode parsed = mapper.readTree(text); // 如果是 JSON 字符串
						node.set(fieldName, parsed); // 替换为真正的 JSON 对象
					} catch (Exception ignored) {

					}
				}
			});
		}

	private static String removeJsonComments(String json) {
		// 只处理每一行中 // 开头的注释，保留代码逻辑
		return json.replaceAll("(?m)//.*", "");
	}

		public static void main(String[] args) {

			String paramsStr = "{\"name\":\"xiaoxu\",\"age\":20,\"address\":{\"city\":\"beijing\",\"street\":\"beijing\"}}";

			String str = "{\\\"parameters\\\":{\\\"topic\\\":\\\"12312\\\",\\\"jsonParameters\\\":true,\\\"messageJson\\\":\\\"{\\\\n  \\\\\\\"code\\\\\\\": 0,     // 2131231\\\\n  \\\\\\\"msg\\\\\\\": \\\\\\\"ok\\\\\\\",     // 张金海1\\\\n  \\\\\\\"count\\\\\\\": 0,\\\\n  \\\\\\\"data\\\\\\\": {\\\\n    \\\\\\\"permissions\\\\\\\": true,\\\\n    \\\\\\\"allCount\\\\\\\": 6,     // 年度问题数量\\\\n    \\\\\\\"rectificationCount\\\\\\\": 3,     // 整改数量\\\\n    \\\\\\\"notRectificationCount\\\\\\\": 3     // 未整改数量\\\\n  },\\\\n  \\\\\\\"define\\\\\\\": {},\\\\n  \\\\\\\"ok\\\\\\\": true\\\\n}\\\",\\\"options\\\":{\\\"acks\\\":true,\\\"compression\\\":true}},\\\"credentials\\\":{\\\"kafka\\\":{\\\"clientId\\\":\\\"12312\\\",\\\"brokers\\\":\\\"1231\\\",\\\"ssl\\\":false,\\\"authentication\\\":false,\\\"username\\\":\\\"\\\",\\\"password\\\":\\\"\\\",\\\"saslMechanism\\\":\\\"\\\"}}}";
			String result = parseAndFormatParams(str);
			log.info("result: {}", result);
		}
	}



