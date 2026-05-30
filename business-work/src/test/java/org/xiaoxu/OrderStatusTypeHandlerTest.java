package org.xiaoxu;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.xiaoxu.domain.Order;
import org.xiaoxu.enums.OrderStatusEnum;
import org.xiaoxu.mapper.OrderMapper;

import java.util.Date;
import java.util.List;

/**
 * OrderStatusTypeHandler 读写测试
 *
 * 验证 @TableField(typeHandler=...) 在 MyBatis-Plus 内置方法中是否生效
 * 同时对比 XML 自定义查询的 TypeHandler 行为
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderStatusTypeHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderStatusTypeHandlerTest.class);

    @Resource
    private OrderMapper orderMapper;

    private static Long mpInsertedId;
    private static Long xmlInsertedId;

    // ==================== 写入方向测试 ====================

    /**
     * MP 内置 insert：依赖 @TableName(autoResultMap=true) + @TableField(typeHandler=...)
     * 如果 typeHandler 生效，数据库应存入 1（PAID 的 code），而非字符串 "PAID"
     */
    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("写入-MP insert: 枚举 PAID 应通过 TypeHandler 写入数据库")
    void testMpInsert() {
        Order order = new Order();
        order.setProductId(1001L);
        order.setUserId(2001L);
        order.setOrderNo("MP-" + System.currentTimeMillis());
        order.setProductName("MP-insert测试");
        order.setCreateTime(new Date());
        order.setStatus(OrderStatusEnum.PAID); // code=1

        orderMapper.insert(order);
        mpInsertedId = order.getId();

        LOGGER.info("MP insert: id={}, status={}", mpInsertedId, order.getStatus());
        Assertions.assertNotNull(mpInsertedId, "插入后应返回自增ID");
    }

    /**
     * XML insertSelective：#{status, typeHandler=...} 显式指定 TypeHandler
     * 这条路径是确定能工作的
     */
    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("写入-XML insertSelective: 枚举 SHIPPED 应写入数据库")
    void testXmlInsertSelective() {
        Order order = new Order();
        order.setProductId(1002L);
        order.setUserId(2002L);
        order.setOrderNo("XML-" + System.currentTimeMillis());
        order.setProductName("XML-insertSelective测试");
        order.setCreateTime(new Date());
        order.setStatus(OrderStatusEnum.SHIPPED); // code=2

        orderMapper.insertSelective(order);
        xmlInsertedId = order.getId();

        LOGGER.info("XML insertSelective: id={}, status={}", xmlInsertedId, order.getStatus());
        Assertions.assertNotNull(xmlInsertedId);
    }

    // ==================== 读取方向测试 ====================

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("读取-MP selectById: 数据库 Integer 应转为枚举")
    void testMpSelectById() {
        Order order = orderMapper.selectById(mpInsertedId);

        LOGGER.info("MP selectById: id={}, status={}, desc={}",
                order.getId(), order.getStatus(), order.getStatus().getDesc());

        Assertions.assertEquals(OrderStatusEnum.PAID, order.getStatus());
        Assertions.assertEquals("已支付", order.getStatus().getDesc());
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("读取-XML selectByPrimaryKey: 数据库 Integer 应转为枚举")
    void testXmlSelectByPrimaryKey() {
        Order order = orderMapper.selectByPrimaryKey(xmlInsertedId);

        LOGGER.info("XML selectByPrimaryKey: id={}, status={}, desc={}",
                order.getId(), order.getStatus(), order.getStatus().getDesc());

        Assertions.assertEquals(OrderStatusEnum.SHIPPED, order.getStatus());
        Assertions.assertEquals("已发货", order.getStatus().getDesc());
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("读取-MP selectList: 批量查询 status 也应转为枚举")
    void testMpSelectList() {
        List<Order> list = orderMapper.selectList(null);
        LOGGER.info("MP selectList: {} 条", list.size());

        for (Order order : list) {
            if (order.getStatus() != null) {
                Assertions.assertInstanceOf(OrderStatusEnum.class, order.getStatus(),
                        "id=" + order.getId() + " 的 status 应为枚举");
                LOGGER.info("  id={}, status={}, desc={}",
                        order.getId(), order.getStatus(), order.getStatus().getDesc());
            }
        }
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("读取-MP LambdaQueryWrapper 条件查询")
    void testMpLambdaQuery() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderStatusEnum.PAID);

        List<Order> list = orderMapper.selectList(wrapper);
        LOGGER.info("MP 条件查询 status=PAID: {} 条", list.size());

        for (Order order : list) {
            Assertions.assertEquals(OrderStatusEnum.PAID, order.getStatus());
        }
    }

    // ==================== 更新方向测试 ====================

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("更新-MP updateById: 将 status 从 PAID 改为 COMPLETED")
    void testMpUpdateById() {
        Order order = new Order();
        order.setId(mpInsertedId);
        order.setStatus(OrderStatusEnum.COMPLETED);

        orderMapper.updateById(order);

        Order fetched = orderMapper.selectById(mpInsertedId);
        LOGGER.info("MP updateById: status 改为 {}", fetched.getStatus());
        Assertions.assertEquals(OrderStatusEnum.COMPLETED, fetched.getStatus());
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    @DisplayName("更新-MP LambdaUpdateWrapper: 按条件批量更新 status")
    void testMpLambdaUpdate() {
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, xmlInsertedId)
                .set(Order::getStatus, OrderStatusEnum.CANCELLED);

        orderMapper.update(null, wrapper);

        Order fetched = orderMapper.selectByPrimaryKey(xmlInsertedId);
        LOGGER.info("MP LambdaUpdate: status 改为 {}", fetched.getStatus());
        Assertions.assertEquals(OrderStatusEnum.CANCELLED, fetched.getStatus());
    }

    // ==================== 枚举全覆盖往返测试 ====================

    @Test
    @org.junit.jupiter.api.Order(9)
    @DisplayName("往返-MP insert + selectById: 所有枚举值都能正确转换")
    void testAllEnumValuesMpRoundTrip() {
        for (OrderStatusEnum status : OrderStatusEnum.values()) {
            Order order = new Order();
            order.setProductId(9999L);
            order.setUserId(9999L);
            order.setOrderNo("MP-ENUM-" + status.getCode() + "-" + System.nanoTime());
            order.setProductName("MP枚举全覆盖");
            order.setCreateTime(new Date());
            order.setStatus(status);

            orderMapper.insert(order);
            Order fetched = orderMapper.selectById(order.getId());

            Assertions.assertEquals(status, fetched.getStatus(),
                    String.format("%s(code=%d) MP往返失败", status.name(), status.getCode()));
            LOGGER.info("  MP: {}(code={}) → insert → selectById → {} ✅",
                    status.name(), status.getCode(), fetched.getStatus().name());

            orderMapper.deleteById(order.getId());
        }
    }

    @Test
    @org.junit.jupiter.api.Order(10)
    @DisplayName("往返-XML insertSelective + selectByPrimaryKey: 所有枚举值都能正确转换")
    void testAllEnumValuesXmlRoundTrip() {
        for (OrderStatusEnum status : OrderStatusEnum.values()) {
            Order order = new Order();
            order.setProductId(9998L);
            order.setUserId(9998L);
            order.setOrderNo("XML-ENUM-" + status.getCode() + "-" + System.nanoTime());
            order.setProductName("XML枚举全覆盖");
            order.setCreateTime(new Date());
            order.setStatus(status);

            orderMapper.insertSelective(order);
            Order fetched = orderMapper.selectByPrimaryKey(order.getId());

            Assertions.assertEquals(status, fetched.getStatus(),
                    String.format("%s(code=%d) XML往返失败", status.name(), status.getCode()));
            LOGGER.info("  XML: {}(code={}) → insertSelective → selectByPrimaryKey → {} ✅",
                    status.name(), status.getCode(), fetched.getStatus().name());

            orderMapper.deleteByPrimaryKey(order.getId());
        }
    }

    // ==================== 边界测试 ====================

    @Test
    @org.junit.jupiter.api.Order(11)
    @DisplayName("边界-fromCode(999) 未知 code 应返回 null")
    void testFromCodeUnknown() {
        Assertions.assertNull(OrderStatusEnum.fromCode(999));
    }

    @Test
    @org.junit.jupiter.api.Order(12)
    @DisplayName("边界-fromCode(null) 应返回 null")
    void testFromCodeNull() {
        Assertions.assertNull(OrderStatusEnum.fromCode(null));
    }

    // ==================== 清理 ====================

    @Test
    @org.junit.jupiter.api.Order(13)
    @DisplayName("清理-删除测试数据")
    void cleanup() {
        if (mpInsertedId != null) {
            orderMapper.deleteById(mpInsertedId);
            LOGGER.info("清理 MP 测试数据 id={}", mpInsertedId);
        }
        if (xmlInsertedId != null) {
            orderMapper.deleteByPrimaryKey(xmlInsertedId);
            LOGGER.info("清理 XML 测试数据 id={}", xmlInsertedId);
        }
    }
}
