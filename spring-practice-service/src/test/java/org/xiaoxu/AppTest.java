package org.xiaoxu;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xiaoxu.enums.UserStatusEnum;
import org.xiaoxu.mapper.UserMapper;
import org.xiaoxu.pojo.Users;

/**
 * Unit test for simple App.
 */
@Slf4j
@SpringBootTest
public class AppTest{
    @Autowired
    private UserMapper userMapper;
    @Test
    public void testHandler(){
        Users users = new Users();
        users.setId(2L);
        users.setStatus(UserStatusEnum.LOCKED);
        users.setNickName("xiaoxu1");
        users.setIdCard("549854121212312313");
        userMapper.insert(users);
    }

    @Test
    public void testSelect(){
        Users users = userMapper.selectById(2L);
        log.info("users:{}", users);
    }


}
