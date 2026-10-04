package org.xiaoxu.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xiaoxu.pojo.PracticeProduct;

import java.math.BigDecimal;
import java.util.List;

/**
 * @className: PracticeProductMapper
 * @description: 【示例】手写 XML mapper 的标准套路演示,XML 见 resources/mapper/PracticeProductMapper.xml。
 *               注意:这里故意不继承 BaseMapper,纯手写;和 MyBatis-Plus 在同一个项目里混用毫无问题。
 *
 *               多参数的两种写法:
 *               1. 每个参数都加 @Param("名字") —— XML 里用 #{名字} 取值(推荐,最清晰)
 *               2. 参数封装成对象/Map,不加 @Param —— XML 里直接 #{对象属性名}
 */
@Mapper
public interface PracticeProductMapper {

    /**
     * 动态多条件查询(开发中出场率 No.1)
     * 所有条件都是"可选"的:传了就参与过滤,不传就忽略 —— 对应 XML 的 <where> + <if>
     */
    List<PracticeProduct> selectByCondition(@Param("productName") String productName,
                                            @Param("category") String category,
                                            @Param("minPrice") BigDecimal minPrice,
                                            @Param("maxPrice") BigDecimal maxPrice,
                                            @Param("status") Integer status);

    /**
     * 按 id 集合批量查询(购物车结算、批量拉取详情都会用)—— 对应 XML 的 <foreach>
     */
    List<PracticeProduct> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 插入并回填自增主键 —— useGeneratedKeys + keyProperty
     * 返回值 int = 受影响行数;插入成功后 product.id 会被 MyBatis 自动填上
     */
    int insert(PracticeProduct product);

    /**
     * 批量插入 —— <foreach> 拼 values (...),(...),(...)
     * 对比:循环调单条 insert 是 N 次网络往返,批量是一条 SQL,量大时性能差距很大
     */
    int batchInsert(@Param("list") List<PracticeProduct> products);

    /**
     * 选择性更新:只更新对象里非 null 的字段 —— <set> + <if>
     * 场景:编辑商品时只想改价格不想动库存,若用全字段 UPDATE 会把没传的字段覆盖成 NULL
     */
    int updateSelective(PracticeProduct product);

    /**
     * 分页查询 —— MySQL 的 LIMIT #{offset}, #{size}
     * offset = (pageNo - 1) * size,由 Controller/Service 换算,Mapper 只管偏移量
     */
    List<PracticeProduct> selectPage(@Param("offset") int offset, @Param("size") int size);
}
