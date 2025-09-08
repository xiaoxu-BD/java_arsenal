package org.xiaoxu.web_boot.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.xiaoxu.web_boot.aop.SPLParams;
import org.xiaoxu.web_boot.common.request.RegisterParam;
import org.xiaoxu.web_boot.consts.UserConst;
import org.xiaoxu.web_boot.entity.User;
import org.xiaoxu.web_boot.entity.vo.UserVO;
import org.xiaoxu.web_boot.exception.CustomException;
import org.xiaoxu.web_boot.local.UserInfoThread;
import org.xiaoxu.web_boot.mapper.UserMapper;
import org.xiaoxu.web_boot.mapper.convert.UserVOConvert;
import org.xiaoxu.web_boot.service.entity.UserService;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @className: UserServiceImpl
 * @author: xiaoxu
 * @date: 2025/9/3 9:43
 * @Version: 1.0
 * @description:
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService, InitializingBean {
    private final UserMapper userMapper;

    private final RedisTemplate redisTemplate;

    private final RedissonClient redissonClient;

    private  RBloomFilter<String> bloomFilter;
    @Override
    public void addUser(RegisterParam register) {
        if(register.getId() == null){
            register.setId(IdUtil.getSnowflakeNextId());
        }
        User user = User.builder().setId(register.getId()).setIdCard(register.getIdCard()).setSex(register.getSex()).build();

        userMapper.insertUser(user);
        //先插入数据,防止插入数据库失败,filter中添加脏数据
        bloomFilter.add(user.getIdCard());
        //将用户信息存入ThreadLocal
        UserInfoThread.setUserInfoThread(user.getIdCard());
    }

    @Override
    public List<UserVO> getUser() {


        // 先查询Bloom Filter
//        if (!isExist(userId)) {
//            return null; // 一定不存在，直接返回
//        }
        List<User> userList = this.list();
        Assert.isTrue(userList != null && !userList.isEmpty(),"用户不存在");
        List<UserVO> userVOList = UserVOConvert.INSTANCE.toUserVOList(userList);
        redisTemplate.opsForValue().set(UserConst.USER_ID ,userVOList,3, TimeUnit.MINUTES);
        return userVOList;
    }

    @Override
    public void updateUser(UserVO userVO) {
        Assert.isTrue(userVO.getUserId() != null,"用户id不能为空");
        User user = this.getById(userVO.getUserId());
        //如果user已经存在才能更新使用更新插入的方法:
        userMapper.updateExistUser(userVO);
        //更新 也要加入到布隆过滤器
        bloomFilter.add(user.getIdCard());

    }

    @Override
    @SPLParams("'用户执行了查询操作方法=' + #methodName + '，身份证id=' + #idCard")
    public UserVO getByIdCard(String idCard) {
        // 先查询Bloom Filter
        boolean exist = isExist(idCard);
        if (!exist) {
            throw new CustomException(500, "用户不存在");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().eq(User::getIdCard, idCard);
        User user = userMapper.selectOne(wrapper);
        Assert.isTrue(user!= null,"用户不存在");
        UserVO userVO = UserVOConvert.INSTANCE.toUserVO(user);
        return userVO;

    }

    //是否存在
    public boolean isExist(String key){
        if (!bloomFilter.contains(key)){
            return false;
        }

        return dbQueryByKey(key);
    }

    private boolean dbQueryByKey(String key) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().eq(User::getIdCard, key);
        User one = this.getOne(wrapper);
        return one != null;
    }


    //初始化布隆过滤器
    @Override
    public void afterPropertiesSet() throws Exception {
        String bloomFilterKey = "user:bloom:filter:";
        bloomFilter = redissonClient.getBloomFilter(bloomFilterKey);
        bloomFilter.tryInit(10000,0.01);
    }
}
