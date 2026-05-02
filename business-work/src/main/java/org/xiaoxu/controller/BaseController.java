package org.xiaoxu.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.domain.dto.ProductInitRequest;
import org.xiaoxu.domain.dto.ProductRes;
import org.xiaoxu.domain.dto.SeckillRequest;
import org.xiaoxu.service.ProductService;
import org.xiaoxu.service.SecKillService;

@RestController
public class BaseController {

    @Resource
    private ProductService productService;

    @Resource
    private SecKillService seckillService;

    @PostMapping("/product/init")
    public String initProduct(@RequestBody ProductInitRequest request) {

        Integer i = productService.initProduct(request);
        if (i != 1) {
            return "fail";
        }
        return "success";
    }

    @GetMapping("/product/{productId}")
    public ProductRes getProductInfo(@PathVariable Long productId){
      return   productService.getInfomation(productId);
    }


    //下单购买商品 有效性验证/库存查询数量/ 生成订单、
    @PostMapping("/seckill")
    public String deductProduct(@RequestBody SeckillRequest request){
        Integer result = seckillService.dedicatedDeduction(request);
        return  result >0 ? "success" : "failed";
    }
}
