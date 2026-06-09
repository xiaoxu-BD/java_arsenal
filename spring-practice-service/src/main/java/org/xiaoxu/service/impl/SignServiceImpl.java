package org.xiaoxu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaoxu.domain.SignIn;
import org.xiaoxu.domain.Users;
import org.xiaoxu.mapper.SignInMapper;
import org.xiaoxu.mapper.UsersMapper;
import org.xiaoxu.service.SignService;

import java.util.Date;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignServiceImpl implements SignService {

    private final StringRedisTemplate stringRedisTemplate;
    private final UsersMapper usersMapper;
    private final SignInMapper signInMapper;

    private static final String SIGN_KEY_PREFIX = "sign:";
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void persistSignIns() {
        LocalDate today = LocalDate.now();
        String monthStr = today.format(MONTH_FORMAT);
        int daysInMonth = today.lengthOfMonth(); // 当月天数：28/29/30/31

        // 1. 查询所有用户
        List<Users> users = usersMapper.selectList(null);
        if (users == null || users.isEmpty()) {
            return;
        }

        int count = 0;
        for (Users user : users) {
            String key = SIGN_KEY_PREFIX + user.getId() + ":" + monthStr;

            // 2. 遍历当月每一天，逐个检查bit位
            for (int day = 1; day <= daysInMonth; day++) {
                Boolean signed = stringRedisTemplate.opsForValue().getBit(key, day - 1);
                if (!Boolean.TRUE.equals(signed)) {
                    continue;
                }

                // 3. 检查 t_sign_in 是否已有记录（幂等）
                LocalDate signDate = today.withDayOfMonth(day);
                Date date = Date.from(signDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
                boolean exists = signInMapper.exists(new LambdaQueryWrapper<SignIn>()
                        .eq(SignIn::getUserId, user.getId())
                        .eq(SignIn::getSignDate, date));
                if (exists) {
                    continue;
                }

                // 4. 插入签到记录
                SignIn signIn = new SignIn();
                signIn.setUserId(user.getId());
                signIn.setSignDate(date);
                signIn.setCreateTime(new Date());
                signIn.setRepaird((byte) 0);
                signInMapper.insert(signIn);
                count++;
            }
        }

        if (count > 0) {
            log.info("签到记录持久化完成，本次新增{}条记录", count);
        }
    }
}
