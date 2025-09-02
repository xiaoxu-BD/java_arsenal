package org.xiaoxu.web_boot.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xiaoxu.web_boot.common.Result;
import org.xiaoxu.web_boot.entity.NodeDesc;
import org.xiaoxu.web_boot.entity.vo.UserInfo;
import org.xiaoxu.web_boot.service.ItUserService;
import org.xiaoxu.web_boot.service.OrderService;
import org.xiaoxu.web_boot.service.impl.JSONService;
import org.xiaoxu.web_boot.utils.RustFSUploadUtils;


@RestController
@RequestMapping("/api")
public class HelloController {

   private  static final Logger logger  = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("/log")
    public String hello() {
        logger.info("处理请求");
        return "Hello, JavaArsenal!";
    }

    @GetMapping("/hello/{name}")
    public Result<?> testAop(@PathVariable("name") String name) {
        logger.info("处理请求PathVariable 带斜杠");
        String res = "hello" + name;
        return Result.success(res);
    }
    @GetMapping("/hello")
    public Result<?> testRequestParam(@RequestParam("name") String name) {
        logger.info("处理请求路径参数RequestParam 带问号");
        String res = "hello" + name;
        return Result.success(res);
    }


    @Autowired
    private OrderService orderService;

    @GetMapping("/test")
    public String test() {
        orderService.processHighPriorityOrder("A001");
        orderService.processLowPriorityLog("异步日志信息");

        // 带返回值异步
        orderService.asyncGetResult("B001")
                .thenAccept(result -> System.out.println("异步结果：" + result));

        return "已发起异步任务";
    }

    @GetMapping("/test2")
    public String test2(@RequestParam("orderId") String orderId) {
        orderService.aiPoolUse(orderId);
        return "已使用ThreadPool发起异步任务";
    }


    @GetMapping("/testAsync")
    public String testAsync(@RequestParam("orderId") String orderId) {
        orderService.processOrder(orderId);
        return "已使用Async发起异步任务";
    }

    @Resource
    JSONService jsonService;

    @PostMapping("/testMap")
    public Result jsonTest(@RequestBody NodeDesc desc) {
       String json =  jsonService.receive(desc);
       return  Result.success(json);
    }

    @PostMapping("/testMap2")
    public Result jsonTest2(@RequestBody NodeDesc desc) {
      NodeDesc entity  =   jsonService.parse(desc);
      return  Result.success(entity);
    }


    @Operation(summary = "使用编程式事务")
    @GetMapping("/testTransaction")
    public String testTransaction() {
        orderService.testTransaction();
        return "已使用编程式事务";
    }

//    @Autowired todo
    private RustFSUploadUtils rustFSUploadUtils;

    @Operation(summary = "使用rustfs上传文件")
//    @PostMapping("/upload")  todo
    public Result uploadFile(@RequestParam("file") MultipartFile file) {
        return Result.success(rustFSUploadUtils.upload(file));
    }


    @GetMapping("/test3")
    public Result test3(@RequestParam("orderId") String orderId) {
        int i = 1;
        int result = i / Integer.parseInt(orderId);
        return Result.success(result);
    }

    @Autowired
    private ItUserService   itUserService;


    @GetMapping("mapToVO")
    public Result<?> mapToVO() {
        UserInfo userInfo = itUserService.getUserInfo();
        return Result.success(userInfo);
    }

}