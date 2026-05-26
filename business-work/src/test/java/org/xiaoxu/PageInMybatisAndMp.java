package org.xiaoxu;

import cn.hutool.core.lang.Assert;
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
import org.xiaoxu.domain.ModTab;
import org.xiaoxu.domain.Order;
import org.xiaoxu.mapper.ModTabMapper;
import org.xiaoxu.service.OrderService;

import java.util.*;

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

    /**
     * 痛点1: COUNT 查询的性能开销
     * PageHelper 默认会额外执行一条 SELECT COUNT(*) 来获取总数
     * 对于复杂 SQL（多表 JOIN / 子查询），这条 count 查询可能很慢
     * 观察控制台 SQL 日志，会看到两条 SQL: 一条 count + 一条分页查询
     */
    @Test
    public void testCountQueryOverhead(){
        PageHelper.startPage(1, 10);
        List<Order> orderList = orderService.getOrderList();
        PageInfo<Order> pageInfo = new PageInfo<>(orderList);
        LOGGER.info("返回记录数: {}", orderList.size());
        LOGGER.info("总数: {}", pageInfo.getTotal());
        LOGGER.info("总页数: {}", pageInfo.getPages());
        LOGGER.info("=> 看控制台: count 查询 + limit 查询，共两条 SQL");
    }

    /**
     * 痛点1 修复: 关闭 count 查询，只查数据
     * 适用场景: 只需要翻页不需要总数（如"加载更多"的无限滚动）
     */
    @Test
    public void testSkipCountQuery(){
        PageHelper.startPage(1, 10, false);  // 第三个参数 false = 不查 count
        List<Order> orderList = orderService.getOrderList();
        PageInfo<Order> pageInfo = new PageInfo<>(orderList);
        LOGGER.info("返回记录数: {}", orderList.size());
        LOGGER.info("总数: {} (0 表示未执行 count)", pageInfo.getTotal());
        LOGGER.info("=> 看控制台: 只有一条 SQL，没有 count 查询");
    }

    /**
     * 痛点2: startPage 只对紧跟的下一条 SQL 生效
     * 如果方法里有多条 SQL，只有第一条被分页，后续 SQL 全量执行
     * 实际场景: Service 方法里查了主表又查了关联表，分页加到了错误的 SQL 上
     */
    @Test
    public void testMultipleQueriesAfterStartPage(){
        PageHelper.startPage(1, 5);

        // 第一条查询: 被加了 LIMIT 5
        List<Order> first = orderService.getOrderList();
        LOGGER.info("第一条(被分页): count={}", first.size());

        // 第二条查询: 没有分页，全量执行
        List<Order> second = orderService.getOrderList();
        LOGGER.info("第二条(全量执行): count={}", second.size());
        LOGGER.info("=> startPage 只对第一条 SQL 生效，第二条被忽略了");
    }

    /**
     * 痛点3: 嵌套调用中的分页不可控
     * Service A 调用 Service B，两边都用了 PageHelper.startPage
     * 外层的 startPage 可能被内层的查询消费掉
     */
    @Test
    public void testNestedStartPage(){
        // 外层: 想给"汇总查询"分页
        PageHelper.startPage(1, 10);
        LOGGER.info("外层 startPage(1, 10) 已调用");

        // 内层: 另一个方法里也调用了 startPage + 查询
        // 这条查询会消费掉外层的分页参数
        List<Order> inner = orderService.getOrderList();
        LOGGER.info("内层查询(消费了外层分页): count={}", inner.size());

        // 外层真正想分页的查询: 已经没有分页参数了
        List<Order> outer = orderService.getOrderList();
        LOGGER.info("外层查询(反而没分页了): count={}", outer.size());
        LOGGER.info("=> 内层查询消费了外层的分页参数，外层查询全量返回");
    }

    /**
     * 痛点4: PageInfo 在空集合上的陷阱
     * 查询返回空 List 时，PageInfo 的 pageNum 会被重置为 0
     * 前端拿到 pageNum=0 可能导致分页组件异常
     */
    @Test
    public void testEmptyResultPageInfo(){
        // 用一个不可能命中的条件查出空结果
        PageHelper.startPage(99999, 10);  // 第 99999 页，大概率没数据
        List<Order> orderList = orderService.getOrderList();
        PageInfo<Order> pageInfo = new PageInfo<>(orderList);

        LOGGER.info("返回记录数: {}", orderList.size());
        LOGGER.info("pageNum: {} (空结果时会被重置为0!)", pageInfo.getPageNum());
        LOGGER.info("total: {}", pageInfo.getTotal());
        LOGGER.info("=> 前端拿到 pageNum=0，分页组件可能显示异常");
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

    @Autowired
    private ModTabMapper modTabMapper;

    @Test
    public void mybatis01(){
        List<String> stringList = Arrays.asList("1", "2", "3", "4", "5");
        List<Integer> idList = stringList.stream().map(Integer::valueOf).toList();
        Assert.notEmpty(idList,()-> new RuntimeException("id集合为空"));
        List<ModTab> modList =  modTabMapper.selectList(idList);

        modList.forEach(System.out::println);
    }


}
