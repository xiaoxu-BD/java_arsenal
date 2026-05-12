package org.xiaoxu;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xiaoxu.channel.dto.ChannelRequest;
import org.xiaoxu.channel.executor.ChannelExecutor;
import org.xiaoxu.domain.Order;
import org.xiaoxu.service.OrderService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    @Test
    public void testMap(){
        List<Order> orderList = orderService.getOrderList();
        List<Map<String,String>> resultList = new ArrayList<>();
        for (Order order : orderList) {


            if (order.getProductId() != 3){
                continue;
            }

            Map<String,String> map = new HashMap<String,String>();


            map.put("userId",String.valueOf(order.getUserId()));
            resultList.add(map);


            String policyNo = StringUtils.isNotBlank(order.getOrderNo()) ? order.getOrderNo() :  order.getId().toString();

            // SELECT * from t_e_print_log where policy_no = #{policyNo} and system_source = 1 and remark is not null;
        }
        LOGGER.info("resultList: {}",resultList);




    }

    @Autowired
    private ChannelExecutor channelExecutor;

    @Test
    public void mainTest(){
        ChannelRequest req = ChannelRequest.builder()
                .channelCode("TEST_HTTPBIN2")
                .bizType("PAYMENT")
                .bizId("ORD20260512002")
                .action("CREATE")
                .url("https://httpbin.org/post")
                .requestBody("{\"amount\":200,\"out_trade_no\":\"ORD20260512002\"}")
                .maxRetry(3)
                .build();

        Object result = channelExecutor.execute(req, Object.class);

        System.out.println(result);
    }

}
