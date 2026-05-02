package org.xiaoxu.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaoxu.domain.dto.ProductRes;
import org.xiaoxu.domain.dto.SeckillRequest;
import org.xiaoxu.service.*;

@Service
public class SecKillServiceImpl implements SecKillService {



    @Resource
    private ProductService productService;

    @Resource
    private StockService stockService;

    @Resource
    private OrderService orderService;

    @Resource
    private UserService userService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer dedicatedDeduction(SeckillRequest request) {


        // 校验用户状态；
//       boolean isTrue =  userService.verified(request.getUserId());
//
//       if (!isTrue){
//           throw new RuntimeException("USER STATUS IS ILLEGAL");
//       }

       //校验库存状态；查询并锁定库存；
       Integer remainMount =  stockService.queryAndLockInventory(request.getProductId());

       if (remainMount <=0 ){
           throw new RuntimeException("REMAIN MOUNT IS ZERO");
       }


       //执行扣减逻辑；
        stockService.realDeductAmount(request.getQuantity(),request.getProductId());

       //生成订单逻辑；
        ProductRes productRes = productService.getInfomation(request.getProductId());
        if (productRes == null){
            throw new RuntimeException("ILLEGAL");
        }
        orderService.createOrder(productRes.getName(),request.getUserId(),request.getProductId());

        return 1;
   }
}
