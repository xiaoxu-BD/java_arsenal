package org.xiaoxu.web_boot.service.practice;// ======== OrderServiceImpl.java ========
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.xiaoxu.web_boot.entity.practice.Item;
import org.xiaoxu.web_boot.entity.practice.Order;
import org.xiaoxu.web_boot.entity.practice.OrderSummary;
import org.xiaoxu.web_boot.enums.OrderEnums;
import org.xiaoxu.web_boot.exception.OrderException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Override
    public List<OrderSummary> processOrders(List<Order> request) {
        // 校验参数
        validateRequest(request);

        List<OrderSummary> result = Lists.newArrayList();
        Set<String> allProductNames = Sets.newHashSet();
        StringBuilder logBuilder = new StringBuilder();

        try {
            for (Order order : request) {
                double totalPrice = processProductTotal(order, allProductNames);
                double discountRate = getDiscountRate(order.getUser().getLevel());
                double finalPrice = Math.round(totalPrice * discountRate * 100.0) / 100.0;

                OrderSummary summary = new OrderSummary();
                summary.setOrderId(order.getOrderId());
                summary.setTotalPrice(totalPrice);
                summary.setDiscountRate(discountRate);
                summary.setFinalPrice(finalPrice);
                summary.setMessage("订单处理成功");
                result.add(summary);

                logBuilder.append(String.format("订单[%s] 金额=%.2f 折扣=%.2f 最终=%.2f\n",
                        order.getOrderId(), totalPrice, discountRate, finalPrice));
            }

            // 汇总统计
            List<Double> prices = result.stream().map(OrderSummary::getFinalPrice).collect(Collectors.toList());
            double maxPrice = Collections.max(prices);
            double minPrice = Collections.min(prices);
            logBuilder.append(String.format("最高订单金额: %.2f, 最低订单金额: %.2f\n", maxPrice, minPrice));

        } catch (OrderException e) {
            logBuilder.append("业务异常: ").append(e.getMessage()).append("\n");
        } catch (Exception e) {
            logBuilder.append("系统异常: ").append(e.getMessage()).append("\n");
            throw new RuntimeException("订单处理系统错误", e);
        } finally {
            System.out.println("===== 订单处理日志 =====");
            System.out.println(logBuilder);
        }

        return result;
    }

    private void validateRequest(List<Order> request) {
        if (CollectionUtils.isEmpty(request)) {
            throw new OrderException(OrderEnums.ORDER_PARAM_ERROR);
        }
    }

    private double processProductTotal(Order order, Set<String> allProductNames) {
        double total = 0;
        for (Item item : order.getItems()) {
            if (StringUtils.isBlank(item.getName())) {
                throw new OrderException(OrderEnums.ITEM_NAME_ERROR);
            }
            allProductNames.add(item.getName());
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    private double getDiscountRate(String level) {
        if (StringUtils.equalsIgnoreCase(level, "vip")) {
            return 0.8;
        } else if (StringUtils.equalsIgnoreCase(level, "svip")) {
            return 0.5;
        } else {
            return 1.0;
        }
    }
}
