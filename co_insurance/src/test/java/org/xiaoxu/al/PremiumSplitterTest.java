package org.xiaoxu.al;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.xiaoxu.entity.CoParty;
import org.xiaoxu.exception.CoInsureRatioException;
import org.xiaoxu.exception.PremiumSplitException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("PremiumSplitter 共保拆分逻辑测试")
class PremiumSplitterTest {

    @Nested
    @DisplayName("validateRatioSum")
    class ValidateRatioSumTests {

        @Test
        @DisplayName("比例和为1时应通过")
        void shouldPassWhenRatioSumEqualsOne() {
            List<CoParty> parties = List.of(
                mockParty("0.333333", 1),
                mockParty("0.333333", 2),
                mockParty("0.333334", 3)
            );

            PremiumSplitter.validateRatioSum(parties);
        }

        @Test
        @DisplayName("比例和在容差外时应抛异常")
        void shouldThrowWhenRatioSumOutOfTolerance() {
            List<CoParty> parties = List.of(
                mockParty("0.50", 1),
                mockParty("0.40", 2)
            );

            CoInsureRatioException ex = assertThrows(
                CoInsureRatioException.class,
                () -> PremiumSplitter.validateRatioSum(parties)
            );

            assertTrue(ex.getMessage().contains("共保比例之和必须等于1.000000"));
        }

        @Test
        @DisplayName("空列表时应抛异常")
        void shouldThrowWhenPartiesEmpty() {
            CoInsureRatioException ex = assertThrows(
                CoInsureRatioException.class,
                () -> PremiumSplitter.validateRatioSum(List.of())
            );

            assertEquals("共保方列表不能为空", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("split")
    class SplitTests {

        @Test
        @DisplayName("应按FLOOR拆分并由最后一方承担尾差")
        void shouldSplitWithFloorAndLastPartyTakeRemainder() {
            BigDecimal total = new BigDecimal("100.00");
            List<CoParty> parties = List.of(
                mockParty("0.3333", 1),
                mockParty("0.3333", 2),
                mockParty("0.3334", 3)
            );

            List<BigDecimal> result = PremiumSplitter.split(total, parties);

            assertEquals(new BigDecimal("33.33"), result.get(0));
            assertEquals(new BigDecimal("33.33"), result.get(1));
            assertEquals(new BigDecimal("33.34"), result.get(2));
            assertEquals(total, result.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        }

        @Test
        @DisplayName("总金额小于等于0时应抛异常")
        void shouldThrowWhenTotalInvalid() {
            List<CoParty> parties = List.of(mockParty("1.0", 1));

            PremiumSplitException ex = assertThrows(
                PremiumSplitException.class,
                () -> PremiumSplitter.split(BigDecimal.ZERO, parties)
            );

            assertTrue(ex.getMessage().contains("拆分总金额必须大于0"));
        }

        @Test
        @DisplayName("共保方为空时应抛异常")
        void shouldThrowWhenPartiesEmpty() {
            PremiumSplitException ex = assertThrows(
                PremiumSplitException.class,
                () -> PremiumSplitter.split(new BigDecimal("100.00"), List.of())
            );

            assertEquals("共保方列表不能为空", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("verify 与 splitSorted")
    class VerifyAndSplitSortedTests {

        @Test
        @DisplayName("verify在金额不一致时应抛异常")
        void shouldThrowWhenVerifyMismatch() {
            PremiumSplitException ex = assertThrows(
                PremiumSplitException.class,
                () -> PremiumSplitter.verify(
                    new BigDecimal("100.00"),
                    List.of(new BigDecimal("33.00"), new BigDecimal("33.00"), new BigDecimal("33.00"))
                )
            );

            assertTrue(ex.getMessage().contains("保费拆分校验失败"));
        }

        @Test
        @DisplayName("splitSorted应先按sortOrder排序后拆分")
        void shouldSortByOrderBeforeSplit() {
            CoParty third = mockParty("0.20", 3);
            CoParty first = mockParty("0.30", 1);
            CoParty second = mockParty("0.50", 2);

            List<BigDecimal> result = PremiumSplitter.splitSorted(
                new BigDecimal("10.01"),
                List.of(third, first, second)
            );

            assertEquals(List.of(
                new BigDecimal("3.00"),
                new BigDecimal("5.00"),
                new BigDecimal("2.01")
            ), result);
        }
    }

    private static CoParty mockParty(String ratio, int sortOrder) {
        CoParty party = mock(CoParty.class);
        when(party.getShareRatio()).thenReturn(new BigDecimal(ratio));
        when(party.getSortOrder()).thenReturn(sortOrder);
        return party;
    }
}
