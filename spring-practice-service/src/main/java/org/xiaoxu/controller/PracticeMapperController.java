package org.xiaoxu.controller;

import org.xiaoxu.mapper.PracticeOrderMapper;
import org.xiaoxu.mapper.PracticeProductMapper;
import org.xiaoxu.pojo.PracticeProduct;
import org.xiaoxu.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @className: PracticeMapperController
 * @description: 手写 XML mapper 练习的验证入口。
 *               /product/** 走的是已完成的示例 mapper;
 *               /order/**   走的是练习 mapper,对应 XML 没写完时调用会报
 *               Invalid bound statement (not found),写完一个验证一个。
 */
@RestController
@RequestMapping("/practice/mapper")
public class PracticeMapperController {

    private final PracticeProductMapper productMapper;
    private final PracticeOrderMapper orderMapper;

    public PracticeMapperController(PracticeProductMapper productMapper,
                                    PracticeOrderMapper orderMapper) {
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
    }

    // ==================== 示例:商品表(可直接调用) ====================

    /** 动态多条件查询:不传参=查全部;传 minPrice=100&maxPrice=500&category=DIGITAL 试试组合 */
    @GetMapping("/product/list")
    public Result<List<PracticeProduct>> listProducts(String productName, String category,
                                                      BigDecimal minPrice, BigDecimal maxPrice,
                                                      Integer status) {
        return Result.success(productMapper.selectByCondition(
                productName, category, minPrice, maxPrice, status));
    }

    /** IN 查询:ids=1,3,5 。空集合在进 Mapper 前拦掉,防拼出 IN () 语法错误 */
    @GetMapping("/product")
    public Result<List<PracticeProduct>> getByIds(@RequestParam List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        return Result.success(productMapper.selectByIds(ids));
    }

    /** 插入+主键回填:返回值里能看到自动生成的 id */
    @PostMapping("/product/add")
    public Result<PracticeProduct> add(@RequestBody PracticeProduct product) {
        productMapper.insert(product);
        return Result.success(product);
    }

    /** 批量插入:body 传数组,返回受影响行数 */
    @PostMapping("/product/batchAdd")
    public Result<Integer> batchAdd(@RequestBody List<PracticeProduct> products) {
        return Result.success(productMapper.batchInsert(products));
    }

    /** 选择性更新:只传 id + 要改的字段,其它字段为 null 就不会被更新 */
    @PostMapping("/product/update")
    public Result<Integer> update(@RequestBody PracticeProduct product) {
        return Result.success(productMapper.updateSelective(product));
    }

    /** 分页:pageNo 从 1 开始,内部换算 offset */
    @GetMapping("/product/page")
    public Result<List<PracticeProduct>> page(@RequestParam(defaultValue = "1") int pageNo,
                                              @RequestParam(defaultValue = "3") int pageSize) {
        return Result.success(productMapper.selectPage((pageNo - 1) * pageSize, pageSize));
    }

    // ==================== 练习:订单表(XML 未写完会报错) ====================

    /** 练习1:订单列表多条件查询。示例:orderNo=SO&statusList=0,1&beginTime=2026-10-01T00:00:00 */
    @GetMapping("/order/list")
    public Result<?> listOrders(String orderNo, Long userId,
                                @RequestParam(required = false) List<Integer> statusList,
                                LocalDateTime beginTime, LocalDateTime endTime,
                                @RequestParam(defaultValue = "1") int pageNo,
                                @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(orderMapper.selectByCondition(orderNo, userId, statusList,
                beginTime, endTime, (pageNo - 1) * pageSize, pageSize));
    }

    /** 练习2:订单+下单用户。/practice/mapper/order/withUser/1 应看到 username=张三 */
    @GetMapping("/order/withUser/{orderId}")
    public Result<?> orderWithUser(@PathVariable Long orderId) {
        return Result.success(orderMapper.selectOrderWithUser(orderId));
    }

    /** 练习3:订单完整详情。/practice/mapper/order/detail/6 应看到 4 条明细 */
    @GetMapping("/order/detail/{orderId}")
    public Result<?> orderDetail(@PathVariable Long orderId) {
        return Result.success(orderMapper.selectOrderDetail(orderId));
    }

    /** 练习4:用户订单统计。excludeCancel=true 剔除已取消订单 */
    @GetMapping("/order/stat")
    public Result<?> statOrders(@RequestParam(defaultValue = "false") boolean excludeCancel) {
        return Result.success(orderMapper.statUserOrders(excludeCancel));
    }

    /** 练习5:批量取消待支付订单。orderIds=7,8 应返回影响行数 2 */
    @PostMapping("/order/batchCancel")
    public Result<Integer> batchCancel(@RequestParam List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Result.success(0);
        }
        return Result.success(orderMapper.batchCancel(orderIds));
    }

    /** 练习6:扣库存。productId=8&quantity=3 返回 1;quantity=99999 返回 0 */
    @PostMapping("/order/deductStock")
    public Result<Integer> deductStock(@RequestParam Long productId,
                                       @RequestParam Integer quantity) {
        return Result.success(orderMapper.deductStock(productId, quantity));
    }

    /** 便捷入口:一眼看到所有可用验证 URL */
    @GetMapping("/help")
    public Result<List<String>> help() {
        return Result.success(Arrays.asList(
                "GET  /practice/mapper/product/list?category=FOOD&minPrice=50&maxPrice=100",
                "GET  /practice/mapper/product?ids=1,3,5",
                "POST /practice/mapper/product/add        body: {\"productName\":\"测试商品\",\"category\":\"FOOD\",\"price\":9.9,\"stock\":10,\"status\":1}",
                "POST /practice/mapper/product/batchAdd   body: [商品数组]",
                "POST /practice/mapper/product/update     body: {\"id\":1,\"price\":359.0}  只改价格",
                "GET  /practice/mapper/product/page?pageNo=2&pageSize=3",
                "---- 练习区(XML 写完才可用) ----",
                "GET  /practice/mapper/order/list?statusList=0,1&beginTime=2026-10-01T00:00:00",
                "GET  /practice/mapper/order/withUser/1",
                "GET  /practice/mapper/order/detail/6",
                "GET  /practice/mapper/order/stat?excludeCancel=true",
                "POST /practice/mapper/order/batchCancel?orderIds=7,8",
                "POST /practice/mapper/order/deductStock?productId=8&quantity=3"
        ));
    }
}
