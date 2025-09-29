package org.xiaoxu.web_boot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xiaoxu.web_boot.entity.Address;
import org.xiaoxu.web_boot.entity.vo.AddressVO;

public interface AddressService extends IService<Address> {
    Object getFromRedis(String key);

    void register(Address address);

    AddressVO operateData(Long userId);
}
