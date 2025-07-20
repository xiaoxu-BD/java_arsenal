package org.xiaoxu.web_boot;

import cn.hutool.core.lang.Assert;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xiaoxu.web_boot.consts.UserConst;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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


	@Test
	public void testAssert(){
		String condition = "null";
//		Assert.isNull(condition,"内容必须为空");
		Assert.isTrue(true,"内容必须为true");

	}


	@Autowired
	private RedissonClient redissonClient;

		@Test
		public void testCurrentThreadPoolTest() throws InterruptedException {
			String limiterKey = "rRateLimiter:test";

			RRateLimiter rateLimiter = redissonClient.getRateLimiter(limiterKey);

			rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);
			// 3. 使用线程池模拟并发请求
			int requestCount = 20;
			ExecutorService executor = Executors.newFixedThreadPool(10);
			CountDownLatch latch = new CountDownLatch(requestCount);

			for (int i = 0; i < requestCount; i++) {
				int finalI = i;
				executor.submit(() -> {
					try {
						boolean allowed = rateLimiter.tryAcquire(); // 非阻塞获取许可
						if (allowed) {
							System.out.println("✅ 请求 " + finalI + " 成功");
						} else {
							System.err.println("❌ 请求 " + finalI + " 被限流");
						}
					} finally {
						latch.countDown();
					}
				});
			}

			latch.await();
			executor.shutdown();
			redissonClient.shutdown();
		}
	/**
	 * 测试限流器
	 */
	@Test
	public void testRRateLimiter(){
		String limiterKey = "rRateLimiter:test";

		RRateLimiter rateLimiter = redissonClient.getRateLimiter(limiterKey);

		rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);

		for (int i = 0; i < 10; i++) {
			try {
				boolean acquired = rateLimiter.tryAcquire(); // 尝试获取许可，不阻塞
				if (!acquired) {
					throw new RuntimeException("请求过多，被限流！");
				}
				System.out.println("第 " + i + " 个请求通过");
			} catch (Exception e) {
				System.err.println("第 " + i + " 个请求失败: " + e.getMessage());
			}
		}
	}

	/**
	 * 测试 Redisson 锁
	 * @throws InterruptedException
	 */
	@Test
	public void testRedisson() throws InterruptedException {
		RLock lock = redissonClient.getLock(UserConst.USER_ID);
		lock.lock();
		boolean isLocked = lock.isLocked();
		if (isLocked) {
			try {
				// 每秒打印一次 TTL，观察是否不断回到 30s
				for (int i = 0; i < 60; i++) {
					Long ttl = lock.remainTimeToLive();
					System.out.println("第 " + i + " 秒，锁剩余时间：" + ttl + " ms");
					TimeUnit.SECONDS.sleep(1);
				}
			} finally {
				// 释放锁
				lock.unlock();
				log.info("锁已释放");
			}
		} else {
			log.info("获取锁失败");
		}
	}
	@Test
	public void testWatchdog() throws InterruptedException {
		String lockKey = "test:lock";
		RLock lock = redissonClient.getLock(lockKey);

		// 方式1：不带 leaseTime -> 会触发看门狗续期
		lock.lock();
		System.out.println("加锁成功");

		// 每秒打印一次 TTL，观察是否不断回到 30s
		for (int i = 0; i < 60; i++) {
			Long ttl = redissonClient.getBucket(lockKey).remainTimeToLive();
			System.out.println("第 " + i + " 秒，锁剩余时间：" + ttl + " ms");
			TimeUnit.SECONDS.sleep(1);
		}

		lock.unlock();
		System.out.println("锁已释放");
	}

		public static void main(String[] args) {

			String paramsStr = "{\"name\":\"xiaoxu\",\"age\":20,\"address\":{\"city\":\"beijing\",\"street\":\"beijing\"}}";

			String str = "{\\\"parameters\\\":{\\\"topic\\\":\\\"12312\\\",\\\"jsonParameters\\\":true,\\\"messageJson\\\":\\\"{\\\\n  \\\\\\\"code\\\\\\\": 0,     // 2131231\\\\n  \\\\\\\"msg\\\\\\\": \\\\\\\"ok\\\\\\\",     // 张金海1\\\\n  \\\\\\\"count\\\\\\\": 0,\\\\n  \\\\\\\"data\\\\\\\": {\\\\n    \\\\\\\"permissions\\\\\\\": true,\\\\n    \\\\\\\"allCount\\\\\\\": 6,     // 年度问题数量\\\\n    \\\\\\\"rectificationCount\\\\\\\": 3,     // 整改数量\\\\n    \\\\\\\"notRectificationCount\\\\\\\": 3     // 未整改数量\\\\n  },\\\\n  \\\\\\\"define\\\\\\\": {},\\\\n  \\\\\\\"ok\\\\\\\": true\\\\n}\\\",\\\"options\\\":{\\\"acks\\\":true,\\\"compression\\\":true}},\\\"credentials\\\":{\\\"kafka\\\":{\\\"clientId\\\":\\\"12312\\\",\\\"brokers\\\":\\\"1231\\\",\\\"ssl\\\":false,\\\"authentication\\\":false,\\\"username\\\":\\\"\\\",\\\"password\\\":\\\"\\\",\\\"saslMechanism\\\":\\\"\\\"}}}";
			String result = parseAndFormatParams(str);
			log.info("result: {}", result);
		}
	}



