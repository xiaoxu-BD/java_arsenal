package org.xiaoxu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xiaoxu.pojo.PracticeOrder;
import org.xiaoxu.pojo.vo.OrderDetailVO;
import org.xiaoxu.pojo.vo.UserOrderStatVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @className: PracticeOrderMapper
 * @description: 【练习区】需求都写在每个方法的注释里,XML 在
 *               resources/mapper/PracticeOrderMapper.xml,目前只有骨架。
 *               每写完一个 statement,启动服务调 Controller 里对应接口即可验证;
 *               没写的调用会报 org.apache.ibatis.binding.BindingException:
 *               Invalid bound statement (not found) —— 这是 MyBatis 第一经典报错,
 *               含义:接口方法找不到对应的 XML statement(id 不匹配/没写/namespace 错/XML 没被扫描到)。
 */
@Mapper
public interface PracticeOrderMapper {

    // ========================================================================
    // 【练习1·热身】订单列表页多条件查询(综合度最高,建议最后写,或写完示例后直接挑战)
    // 需求:运营后台订单列表页,筛选条件全部可选,满足"与"关系:
    //   a) 订单号模糊匹配(orderNo LIKE)
    //   b) 按用户精确筛选(userId)
    //   c) 按状态批量筛选(statusList,如"待支付+已支付"一起查,IN)
    //   d) 下单时间范围 [beginTime, endTime](闭区间,用 >= 和 <=)
    //   e) 按下单时间倒序,分页返回
    // 验收:GET /practice/mapper/order/list 传入不同参数组合,结果条数与直觉一致
    // 提示:对照示例套路一,额外多一个 foreach-IN 和两个时间的 if
    // ========================================================================
    List<PracticeOrder> selectByCondition(@Param("orderNo") String orderNo,
                                          @Param("userId") Long userId,
                                          @Param("statusList") List<Integer> statusList,
                                          @Param("beginTime") LocalDateTime beginTime,
                                          @Param("endTime") LocalDateTime endTime,
                                          @Param("offset") int offset,
                                          @Param("size") int size);

    // ========================================================================
    // 【练习2·多对一】订单关联出下单用户
    // 需求:订单详情页要展示"谁下的单"。
    //   JOIN practice_user,把 username、level 一并查回,映射进 OrderDetailVO.user
    // 验收:GET /practice/mapper/order/withUser/1 -> user.username = "张三"
    // 提示:两表都有 id,SELECT 里必须给重复列起别名(如 u.id AS user_id),
    //      然后用 <association> 把用户列聚合成嵌套对象 —— 这是 resultMap 的核心考点
    // ========================================================================
    OrderDetailVO selectOrderWithUser(@Param("orderId") Long orderId);

    // ========================================================================
    // 【练习3·一对多】订单详情 = 订单 + 用户 + 明细列表(最难,留到最后)
    // 需求:订单详情页完整展示:订单基本信息 + 下单用户 + 该订单所有明细。
    //   practice_order JOIN practice_user JOIN practice_order_item 一次查出,
    //   一个订单对多条明细,主键去重后用 <collection> 聚合成 List<PracticeOrderItem>
    // 验收:GET /practice/mapper/order/detail/6 -> items.size() = 4(耳机/咖啡豆/坚果/巧克力)
    // 坑:join 之后"一行明细 = 一条结果记录",订单主信息靠 <id> 去重合并;
    //     若 items 少了或订单重复,99% 是 <id> 列放错了位置
    // ========================================================================
    OrderDetailVO selectOrderDetail(@Param("orderId") Long orderId);

    // ========================================================================
    // 【练习4·聚合】用户订单统计报表
    // 需求:查每个用户的订单数和累计消费金额(含用户名)。
    //   excludeCancel = true 时剔除已取消(status=4)的单子再统计
    // 验收:GET /practice/mapper/order/stat
    //   -> 张三:3单/1125.00元(528+99+498);李四:2单/2798.00;赵六:1单/1073.40
    //   (钱七、王五按同样口径自己笔算一遍,对不上就是 SQL 错了)
    // 提示:GROUP BY + COUNT/SUM;SUM 列起别名,别名对应 VO 属性名即可自动映射,
    //      也可以顺手练一下把统计结果也挂到 resultMap
    // ========================================================================
    List<UserOrderStatVO> statUserOrders(@Param("excludeCancel") boolean excludeCancel);

    // ========================================================================
    // 【练习5·批量写】批量取消超时未支付订单
    // 需求:定时任务把一批待支付订单置为已取消(status 0 -> 4)。
    //   只允许从"待支付"改为"已取消",已经是别的状态的单不许被误伤
    // 验收:POST /practice/mapper/order/batchCancel?orderIds=7,8
    //   -> 返回受影响行数 2;再查库确认订单7、8的 order_status 变成 4
    // 提示:UPDATE ... WHERE id IN (foreach) AND order_status = 0
    // ========================================================================
    int batchCancel(@Param("orderIds") List<Long> orderIds);

    // ========================================================================
    // 【练习6·防超卖】扣库存
    // 需求:下单扣库存,库存不足时不允许扣成负数。
    //   用一条 UPDATE 实现,返回受影响行数:1=扣成功,0=库存不足(或商品不存在)
    // 验收:POST /practice/mapper/order/deductStock?productId=8&quantity=3
    //   -> 影响行数 1(坚果库存 200->197);再扣 quantity=99999 -> 影响行数 0,库存不变
    // 提示:UPDATE practice_product SET stock = stock - #{quantity}
    //      WHERE id = #{productId} AND stock >= #{quantity}
    //      —— "判断和修改在同一条 SQL 里原子完成"就是防超卖的全部秘密
    // ========================================================================
    int deductStock(@Param("productId") Long productId,
                    @Param("quantity") Integer quantity);
}
