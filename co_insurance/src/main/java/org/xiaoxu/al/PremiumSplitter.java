package org.xiaoxu.al;

import org.xiaoxu.entity.CoParty;
import org.xiaoxu.exception.CoInsureRatioException;
import org.xiaoxu.exception.PremiumSplitException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class PremiumSplitter {
 
    private static final BigDecimal RATIO_TOLERANCE = new BigDecimal("0.000001");
 
    private PremiumSplitter() {}
 
    /**
     * 验证共保方比例之和 == 1（录单保存时调用）
     *
     * @param parties 共保方列表
     * @throws CoInsureRatioException 比例之和不等于1
     */
    public static void validateRatioSum(List<CoParty> parties) {
        if (parties == null || parties.isEmpty()) {
            throw new CoInsureRatioException("共保方列表不能为空");
        }
        BigDecimal sum = parties.stream()
            .map(CoParty::getShareRatio)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.subtract(BigDecimal.ONE).abs().compareTo(RATIO_TOLERANCE) > 0) {
            throw new CoInsureRatioException(
                String.format("共保比例之和必须等于1.000000，当前值：%s", sum.toPlainString()));
        }
    }
 
    /**
     * 按比例拆分金额（保费或保险金额均适用）
     *
     * @param total   总金额（保费或保险金额）
     * @param parties 已按 sortOrder 升序排序的共保方列表（最后一个承担尾差）
     * @return 各方分摊金额，顺序与 parties 一致，sum == total 严格保证
     */
    public static List<BigDecimal> split(BigDecimal total, List<CoParty> parties) {
        // 入参防御
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PremiumSplitException("拆分总金额必须大于0，当前值：" + total);
        }
        if (parties == null || parties.isEmpty()) {
            throw new PremiumSplitException("共保方列表不能为空");
        }
 
        List<BigDecimal> result = new ArrayList<>(parties.size());
        BigDecimal assigned = BigDecimal.ZERO;
 
        // 前 N-1 方：FLOOR 截断
        for (int i = 0; i < parties.size() - 1; i++) {
            BigDecimal ratio = parties.get(i).getShareRatio();
            BigDecimal share = total
                .multiply(ratio)
                .setScale(2, RoundingMode.FLOOR);  // 截断，不四舍五入
            result.add(share);
            assigned = assigned.add(share);
        }
 
        // 最后一方：承担尾差 = total - 已分配之和
        BigDecimal remainder = total.subtract(assigned);
        if (remainder.compareTo(BigDecimal.ZERO) < 0) {
            // 理论上 FLOOR 策略不会出现负数，若出现说明比例之和 > 1
            throw new PremiumSplitException(
                String.format("尾差为负（%s），请检查共保比例之和是否超过1", remainder));
        }
        result.add(remainder);
 
        // 强制校验：sum 必须严格等于 total
        verify(total, result);
        return result;
    }
 
    /**
     * 强制校验拆分结果之和 == 总金额
     * 拆分后必须调用此方法，不允许带尾差入库
     */
    public static void verify(BigDecimal total, List<BigDecimal> splits) {
        BigDecimal checkSum = splits.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (checkSum.compareTo(total) != 0) {
            throw new PremiumSplitException(
                String.format("保费拆分校验失败！总金额=%s，分配之和=%s，差值=%s",
                    total.toPlainString(),
                    checkSum.toPlainString(),
                    total.subtract(checkSum).toPlainString()));
        }
    }
 
    /**
     * 便捷方法：按 sortOrder 排序后拆分（外部无需手动排序）
     */
    public static List<BigDecimal> splitSorted(BigDecimal total, List<CoParty> parties) {
        List<CoParty> sorted = parties.stream()
            .sorted(Comparator.comparingInt(CoParty::getSortOrder))
            .collect(Collectors.toList());
        return split(total, sorted);
    }
}