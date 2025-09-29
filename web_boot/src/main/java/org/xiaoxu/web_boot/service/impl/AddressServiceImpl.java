package org.xiaoxu.web_boot.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.xiaoxu.web_boot.entity.Address;
import org.xiaoxu.web_boot.entity.vo.AddressVO;
import org.xiaoxu.web_boot.mapper.AddressMapper;
import org.xiaoxu.web_boot.service.AddressService;
import org.xiaoxu.web_boot.utils.RedisUtils;

/**
 * @className: AddressServiceImpl
 * @author: xiaoxu
 * @date: 2025/8/10 8:34
 * @Version: 1.0
 * @description:
 */
@Service
@Slf4j
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {
    @Autowired
    private RedisUtils redisUtils;

    private final String REDIS_ADDRESS_KEY = "address:";

    @Autowired
    private AddressMapper addressMapper;
    @Override
    public Object getFromRedis(String key) {

        ///拿着address 的主键去mysql中查询 如果有放入到redis中
        Address addressEntity = this.getById(key);
        if (ObjectUtil.isNotEmpty(addressEntity)) {
            redisUtils.set(REDIS_ADDRESS_KEY + key, addressEntity);
        }
        ///从redis中查询
//        Address addressInRedis = redisUtils.get(REDIS_ADDRESS_KEY + key, Address.class);
//        return address;
        return redisUtils.getExpire(REDIS_ADDRESS_KEY + key);

    }

    @Override
    public void register(Address address) {
        //redis里面那
        Object addressInRedis = redisUtils.get(REDIS_ADDRESS_KEY + address.getId());

        String jsonStr = JSONUtil.toJsonStr(addressInRedis);
        Address addressInRedisEntity = JSONUtil.toBean(jsonStr, Address.class);
        if (BeanUtil.isNotEmpty(addressInRedisEntity)) {
            log.info("addressInRedisEntity:{}",addressInRedisEntity);
            throw new RuntimeException("地址已存在,无需注册");
        }
        Address addressInMysql = new Address();
        addressInMysql.setId(address.getId());
        addressInMysql.setPersonId(address.getPersonId());
        addressInMysql.setProvince(address.getProvince());
        addressInMysql.setCity(address.getCity());
        addressInMysql.setDetail(address.getDetail());
        addressInMysql.setType(address.getType());
        //插入到mysql中
        this.save(addressInMysql);
        //插入到redis中
        redisUtils.set(REDIS_ADDRESS_KEY + address.getId(), addressInMysql);
    }

    @Override
    public AddressVO operateData(Long userId) {

        Assert.notNull(userId, "userId不能为空");

        AddressVO addressVO = redisUtils.get(REDIS_ADDRESS_KEY + userId, AddressVO.class);

        if (ObjectUtil.isEmpty(addressVO)) {
//            addressMapper.selectByPersonId(userId);
        }
        return null;
    }
}
