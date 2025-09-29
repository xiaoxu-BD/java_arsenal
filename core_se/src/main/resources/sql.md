查询2023年每个VIP等级用户的平均订单金额（只计算已支付订单），
并按VIP等级升序排列。如果某个VIP等级没有用户，则不需要显示该等级


-- 
筛选2023年的数据
只考虑状态为'paid'的已支付订单
关联用户表获取VIP等级信息
按VIP等级分组计算平均订单金额
按VIP等级升序排列结果
```sql
SELECT 
    u.vip_level,
    AVG(o.total_amount) AS avg_order_amount
FROM 
    users u
JOIN 
    orders o ON u.user_id = o.user_id
WHERE 
    o.status = 'paid'
    AND YEAR(o.order_date) = 2023
GROUP BY 
    u.vip_level
ORDER BY 
    u.vip_level ASC;

-- 优化

SELECT 
   u.vip_level,
    AVG(o.total_amount) AS avg_order_amount
from 
   (SEELCT * from orders where status = 'paid' and YEAR(order_date) = 2023) as o
JOIN 
  users u ON o.user_id = u.user_id
group by 
    u.vip_leval
    order by
    u.vip_level
    
    
``` 
找出那些购买了"电子产品"类别商品，但从未购买过"服装"类别商品的用户。
要求返回这些用户的用户名、邮箱和注册日期，并按注册日期降序排列

```sql

SELECT 
    u.name, u.email, u.register_date
from 
  users u 
JOIN 
  orders o ON u.user_id = o.user_id
JOIN
    order_items oi on o.order_id = oi.order_id
JOIN 
    (SELECT * from products where category != '服装' and category = '电子产品') p on oi_product_id = p.product_id
    
order by u.register_date desc  
-- 错误



SELECT 
    u.username, 
    u.email, 
    u.register_date
FROM 
    users u
WHERE 
    u.user_id IN (
        -- 购买了电子产品的用户
        SELECT DISTINCT o.user_id
        FROM orders o
        JOIN order_items oi ON o.order_id = oi.order_id
        JOIN products p ON oi.product_id = p.product_id
        WHERE p.category = '电子产品'
    )
    AND u.user_id NOT IN (
        -- 从未购买过服装的用户
        SELECT DISTINCT o.user_id
        FROM orders o
        JOIN order_items oi ON o.order_id = oi.order_id
        JOIN products p ON oi.product_id = p.product_id
        WHERE p.category = '服装'
    )
ORDER BY 
    u.register_date DESC;       
```


：计算每个商品类别的平均售价（使用products表中的price字段）
和平均销售价（使用实际销售价格，即order_items表中的unit_price字段），
并计算这两个价格的差异百分比。
最后只显示差异百分比绝对值大于10%的类别，按差异百分比绝对值降序排列
```sql
SELECT
avg(oi.unit_price) AS avg_sale_price,
avg(p.price) AS avg_price,
(avg(oi.unit_price) - avg(p.price)) / avg(p.price) AS diff_percent
FROM order_items oi 
JOIN products p ON oi.product_id = p.product_id
group by p.product_id
having abs(diff_percent) > 0.1



SELECT 
    p.category,
    AVG(p.price) AS avg_list_price,
    AVG(oi.unit_price) AS avg_sale_price,
    AVG(oi.unit_price) - AVG(p.price) AS price_difference,
    (AVG(oi.unit_price) - AVG(p.price)) / AVG(p.price) * 100 AS diff_percent
FROM 
    products p
JOIN 
    order_items oi ON p.product_id = oi.product_id
GROUP BY 
    p.category
HAVING 
    ABS((AVG(oi.unit_price) - AVG(p.price)) / AVG(p.price) * 100) > 10
ORDER BY 
    ABS((AVG(oi.unit_price) - AVG(p.price)) / AVG(p.price) * 100) DESC;
```



  
    