package org.xiaoxu.exceldemo.service;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import org.springframework.stereotype.Service;
import org.xiaoxu.exceldemo.dto.UserImportRow;
import org.xiaoxu.exceldemo.entity.DemoUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** 造测试数据: 往 demo_user 批量插入随机用户, 给异步导出/导入练习喂料 */
@Service
public class DataGenService {

    private static final String[] SURNAMES = {"张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴"};
    private static final String[] GIVEN_NAMES = {"伟", "芳", "娜", "敏", "静", "磊", "军", "洋", "勇", "艳",
            "杰", "涛", "明", "超", "秀英", "国栋", "子涵", "欣怡", "浩然", "雨泽"};
    private static final int BATCH_SIZE = 1000;

    public long generate(int count) {
        if (count < 1 || count > 1_000_000) {
            throw new IllegalArgumentException("count 需在 [1, 1000000]");
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        // 手机号尾 8 位用 时间戳基数+序号, 保证同一次生成内不撞唯一键
        long phoneBase = System.currentTimeMillis() % 100_000_000L;

        long created = 0;
        List<DemoUser> buffer = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < count; i++) {
            DemoUser user = new DemoUser();
            user.setName(SURNAMES[random.nextInt(SURNAMES.length)]
                    + GIVEN_NAMES[random.nextInt(GIVEN_NAMES.length)]);
            user.setPhone(String.format("13%d%08d", i % 10, (phoneBase + i) % 100_000_000L));
            user.setEmail(i % 2 == 0 ? "user" + i + "@example.com" : null);
            user.setDeptName(UserImportRow.DEPARTMENTS.get(random.nextInt(UserImportRow.DEPARTMENTS.size())));
            user.setSalary(BigDecimal.valueOf(5000 + random.nextInt(45_000)));
            user.setHireDate(LocalDate.now().minusDays(random.nextInt(2000)));
            buffer.add(user);
            if (buffer.size() == BATCH_SIZE) {
                Db.saveBatch(buffer);
                created += buffer.size();
                buffer.clear();
            }
        }
        if (!buffer.isEmpty()) {
            Db.saveBatch(buffer);
            created += buffer.size();
        }
        return created;
    }
}
