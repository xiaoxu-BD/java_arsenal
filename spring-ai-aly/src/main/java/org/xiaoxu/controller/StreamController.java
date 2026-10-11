package org.xiaoxu.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/stream")
public class StreamController {


    /**
     * 注意注入的是 ChatModel 接口而不是 DashScopeChatModel 具体类：
     * 自动装配会按当前生效的 starter 提供实现（现在是 OpenAI starter -> DeepSeek 的 openAiChatModel）。
     * 写成具体厂商类型会在换厂商时直接启动失败（bean 不存在）。
     */
    @Autowired
    private ChatModel chatModel;


    /**
     * ChatClient 由 Spring AI 自动配置的 Builder 构建（底层就是 DashScope 的 ChatModel），
     * 这里不做任何 Markdown -> HTML 转换，只负责把大模型输出的 Markdown 原文流式吐给前端。
     */
    private final ChatClient chatClient;

    public StreamController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }



    @GetMapping("/sse")
    public SseEmitter sse() {
        SseEmitter emitter = new SseEmitter(60_000L);
        Executors.newFixedThreadPool(10).submit(() -> {
            try {
                for (int i = 0; i < 1000; i++) {
                    emitter.send("Message " + i);
                    Thread.sleep(500);
                }
            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            } finally {
                emitter.complete();
            }
        });
        return emitter;
    }

    @GetMapping("/entity")
    public ResponseEntity<StreamingResponseBody> chat() {

        StreamingResponseBody body = outputStream -> {
            for (int i = 0; i < 1000; i++) {
                String data = "Message " + i + "\n";
                outputStream.write(data.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                try {
                    Thread.sleep(500); // 模拟延迟
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
                .body(body);
    }


    /**
     * 基于 ChatClient 的流式对话接口。
     *
     * 1.返回值 Flux<String> 会被 Spring MVC
     *    按 SSE 协议逐块推送（每个元素一条 data: 消息）。
     * 2. 后端只返回大模型输出的 Markdown 原文，不做 Markdown -> HTML 转换，
     *    渲染交给前端（marked + DOMPurify）完成。
     * 3. 每个正文块先做 percent 编码再下发：SSE 规范要求浏览器解析 data: 行时
     *    剥掉字段值开头的一个空格，而 LLM 的 token 常以空格开头（如 " static"），
     *    不编码会丢空格（英文/代码粘成 publicstaticvoid）。前端用 decodeURIComponent 还原；
     *    [DONE] 哨兵不参与编码，原样比较。
     * 4. 流的正常结束靠 [DONE] 哨兵事件通知：前端收到后主动 es.close()，
     *    避免 EventSource 在服务端关闭连接后自动重连、导致问题被重复发送。
     */
    @GetMapping(value = "/client", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> client(@RequestParam("message") String message, HttpServletResponse response) {
        // SSE 规范固定使用 UTF-8，显式声明一次，避免中文消息经 Tomcat 输出时乱码
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        return chatClient.prompt()
                .user(message)                        // 用户输入，作为本次对话的问题
                .stream()                             // 流式调用大模型
                .content()                            // 只取文本增量（Markdown 原文），逐块下发
                .map(chunk -> URLEncoder.encode(chunk, StandardCharsets.UTF_8)
                        .replace("+", "%20"))         // URLEncoder 把空格编成 +，换成 %20 才能和 JS decodeURIComponent 对齐
                .concatWith(Flux.just("[DONE]"));     // 哨兵：标记输出结束（不是正文内容）
    }

    @GetMapping(value = "/flux")
    public Flux<String> fluxStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .take(100)
                .map(seq -> "Message " + seq + "\r\n");
    }
}