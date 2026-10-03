package org.xiaoxu.wecommail.demo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 教学用:内存 Mock 数据源,模拟 Mapper.selectPage 的返回(不触库)。
 */
@Slf4j
@Service
public class DemoUserService {

    /** 演示用用户 DTO */
    public record DemoUser(long id, String name, String dept) {
    }

    private static final List<DemoUser> MOCK_USERS = buildMockUsers(23);

    private static List<DemoUser> buildMockUsers(int count) {
        String[] names = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十"};
        String[] depts = {"货物申报部", "单证部", "运维部", "财务部", "风控部"};
        List<DemoUser> list = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            list.add(new DemoUser(i, names[(i - 1) % names.length] + i, depts[(i - 1) % depts.length]));
        }
        return List.copyOf(list);
    }

    /** 内存分页,等价于 userMapper.selectPage(new Page<>(page,pageSize), wrapper) */
    public Page<DemoUser> pageUsers(int page, int pageSize) {
        log.info("[demo] ② Service 内存分页: page={}, pageSize={}, 数据总量={}", page, pageSize, MOCK_USERS.size());
        Page<DemoUser> pager = new Page<>(page, Math.max(pageSize, 1));
        pager.setTotal(MOCK_USERS.size());
        int from = (int) Math.min((long) (page - 1) * pageSize, MOCK_USERS.size());
        int to = (int) Math.min((long) from + pageSize, MOCK_USERS.size());
        pager.setRecords(MOCK_USERS.subList(from, to));
        return pager;
    }
}
