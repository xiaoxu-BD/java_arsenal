package org.xiaoxu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaoxu.domain.PointStream;
import org.xiaoxu.domain.UserPoints;
import org.xiaoxu.enums.PointSteamEnum;
import org.xiaoxu.handler.BusinessException;
import org.xiaoxu.mapper.UserPointsMapper;
import org.xiaoxu.service.PointStreamService;
import org.xiaoxu.service.SignService;
import org.xiaoxu.service.UserPointService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @className: UserPointServiceImpl
 * @description: 用户积分服务，签到使用Redis Bitmap实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPointServiceImpl extends ServiceImpl<UserPointsMapper, UserPoints> implements UserPointService {

    private final PointStreamService pointStreamService;
    private final StringRedisTemplate stringRedisTemplate;
    private final SignService signService;

    private static final int SIGN_IN_POINTS = 10;
    private static final String SIGN_KEY_PREFIX = "sign:";
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean signIn(Long userId) {

        // 1. 用Redis Bitmap判断今日是否已签到
        String key = SIGN_KEY_PREFIX + userId + ":" + LocalDate.now().format(MONTH_FORMAT);
        long dayOfMonth = LocalDate.now().getDayOfMonth(); // 1~31，作为offset

        Boolean alreadySigned = stringRedisTemplate.opsForValue().getBit(key, dayOfMonth - 1);
        if (Boolean.TRUE.equals(alreadySigned)) {
            throw new BusinessException(1001, "今日已签到，不要重复签到");
        }

        // 2. 在Bitmap中标记今日已签到
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);

        // 3. 更新用户总积分（先查后改）
        UserPoints userPoints = getOne(new LambdaQueryWrapper<UserPoints>()
                .eq(UserPoints::getUserId, userId));
        if (userPoints == null) {
            // 首次签到，创建积分记录
            userPoints = new UserPoints();
            userPoints.setUserId(userId);
            userPoints.setTotalPoints(SIGN_IN_POINTS);
            userPoints.setVersion(0);
            userPoints.setUpdateTime(LocalDateTime.now());
            save(userPoints);
        } else {
            // 累加积分
            userPoints.setTotalPoints(userPoints.getTotalPoints() + SIGN_IN_POINTS);
            userPoints.setUpdateTime(LocalDateTime.now());
            updateById(userPoints);
        }

        // 4. 记录积分流水
        PointStream pointStream = new PointStream();
        pointStream.setUserId(userId);
        pointStream.setPoints(SIGN_IN_POINTS);
        pointStream.setType(PointSteamEnum.SIGN_IN.getDesc());
        pointStream.setRemark("每日签到奖励");
        pointStream.setCreateTime(LocalDateTime.now());
        pointStreamService.save(pointStream);

        log.info("用户{}签到成功，获得{}积分", userId, SIGN_IN_POINTS);
        return true;
    }

    /**
     * 查询当月签到记录
     */
    @Override
    public Boolean hasSignedToday(Long userId) {
        String key = SIGN_KEY_PREFIX + userId + ":" + LocalDate.now().format(MONTH_FORMAT);
        long dayOfMonth = LocalDate.now().getDayOfMonth();
        return stringRedisTemplate.opsForValue().getBit(key, dayOfMonth - 1);
    }







    // 签到
    public void sign(Long userId, Long habitId) {
        String key = String.format("habit:sign:%d:%d:%s",
                userId, habitId, YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM")));
        int offset = LocalDate.now().getDayOfMonth() - 1;
        stringRedisTemplate.opsForValue().setBit(key, offset, true);
    }

    // 查询某月签到情况
    public List<Boolean> getMonthSigns(Long userId, Long habitId, YearMonth month) {
        String key = String.format("habit:sign:%d:%d:%s",
                userId, habitId, month.format(DateTimeFormatter.ofPattern("yyyyMM")));
        List<Boolean> signs = new ArrayList<>();
        for (int i = 0; i < month.lengthOfMonth(); i++) {
            signs.add(stringRedisTemplate.opsForValue().getBit(key, i));
        }
        return signs;
    }

    // 计算当前连续签到天数
    public int getConsecutiveDays(Long userId, Long habitId) {
        // 从今天开始往前查，直到遇到未签到
        int consecutive = 0;
        LocalDate date = LocalDate.now();

        while (true) {
            String key = String.format("habit:sign:%d:%d:%s",
                    userId, habitId, YearMonth.from(date).format(DateTimeFormatter.ofPattern("yyyyMM")));
            int offset = date.getDayOfMonth() - 1;

            Boolean signed = stringRedisTemplate.opsForValue().getBit(key, offset);
            if (Boolean.TRUE.equals(signed)) {
                consecutive++;
                date = date.minusDays(1);
            } else {
                break;
            }
        }
        return consecutive;
    }
}
