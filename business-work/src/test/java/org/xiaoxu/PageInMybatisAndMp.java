package org.xiaoxu;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.xiaoxu.domain.Order;
import org.xiaoxu.service.OrderService;

import java.util.List;

@SpringBootTest
public class PageInMybatisAndMp {

    private static Logger LOGGER = LoggerFactory.getLogger(PageInMybatisAndMp.class);
    @Resource
    private OrderService orderService;

    @Test
    public void testPageHelper(){
        int pageNum = 1;
        int pageSize = 10;
        PageHelper.startPage(pageNum,pageSize);

        List<Order> orderList = orderService.getOrderList();

        PageInfo<Order> orderPageInfo = new PageInfo<>(orderList);
        LOGGER.info("datas:{}",orderPageInfo.getList());
        LOGGER.info("page:{}",orderPageInfo.getPages());
        LOGGER.info("pageNum:{}",orderPageInfo.getPageNum());
        LOGGER.info("totalPage:{}",(orderPageInfo.getPageSize() + orderPageInfo.getTotal() - 1 )/ orderPageInfo.getPageSize());



    }

    @Test
    public void testPagePlus(){
        Page<Order> orderPage = new Page<>();

        orderPage.setSize(10); // 每页记录数
        orderPage.setCurrent(1); //当前页

        LambdaQueryWrapper<Order> orderLambdaQueryWrapper = new LambdaQueryWrapper<Order>().gt(Order::getUserId, 1213);

        Page<Order> page = orderService.page(orderPage,orderLambdaQueryWrapper);
        LOGGER.info("datas:{}",page.getRecords());
    }

}
