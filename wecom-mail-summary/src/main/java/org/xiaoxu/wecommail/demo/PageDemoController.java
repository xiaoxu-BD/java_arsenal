package org.xiaoxu.wecommail.demo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaoxu.wecommail.common.PageResponse;
import org.xiaoxu.wecommail.demo.DemoUserService.DemoUser;
import org.xiaoxu.wecommail.utils.PDFGenerateUtils;

import java.io.OutputStream;

/**
 * PageResponse.of 教学演示(纯 Mock 数据,不触库)。
 *
 * <p>调用链路:
 * <pre>
 * 浏览器/curl  GET /api/demo/page?page=2&pageSize=5
 *      │
 *      ▼
 * ① Controller      收参(page/pageSize,带默认值) ── 见日志 [demo] ①
 *      │
 *      ▼
 * ② Service         内存分页(真实项目这里是 Mapper.selectPage + 分页插件) ── 见日志 [demo] ②
 *      │
 *      ▼
 * ③ PageResponse.of(dataList, total, pageSize, currentPage) 组装统一分页结构 ── 见日志 [demo] ③
 *      │
 *      ▼
 * Jackson 序列化为 JSON 返回
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class PageDemoController {

    private final DemoUserService demoUserService;

    @GetMapping("/page")
    public PageResponse<DemoUser> page(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "5") int pageSize) {
        log.info("[demo] ① Controller 收到请求: page={}, pageSize={}", page, pageSize);
        Page<DemoUser> pager = demoUserService.pageUsers(page, pageSize);
        PageResponse<DemoUser> response =
                PageResponse.of(pager.getRecords(), (int) pager.getTotal(), pageSize, page);
        log.info("[demo] ③ PageResponse.of 组装完成: total={}, totalPage={}, 本页返回 {} 条",
                response.getTotal(), response.getTotalPage(), response.getDataList().size());
        return response;
    }



}
