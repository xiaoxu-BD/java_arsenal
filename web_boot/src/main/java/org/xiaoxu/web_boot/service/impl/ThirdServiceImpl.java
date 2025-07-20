package org.xiaoxu.web_boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xiaoxu.web_boot.aop.DS;
import org.xiaoxu.web_boot.entity.ItoOrderRpaReturnOrderThird;
import org.xiaoxu.web_boot.mapper.ItoOrderRpaReturnOrderThirdMapper;
import org.xiaoxu.web_boot.service.ThirdService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @className: ThirdServiceImpl
 * @author: xiaoxu
 * @date: 2025/7/14 21:22
 * @Version: 1.0
 * @description:
 */
@Service
public class ThirdServiceImpl extends ServiceImpl<ItoOrderRpaReturnOrderThirdMapper,ItoOrderRpaReturnOrderThird>implements ThirdService {
    @Autowired
    private ItoOrderRpaReturnOrderThirdMapper thirdMapper;


    @DS("ds2")
    public List<ItoOrderRpaReturnOrderThird> getOnCondition(){
//        List<ItoOrderRpaReturnOrderThird> result  =   thirdMapper.getOnCondition();
        LambdaQueryWrapper<ItoOrderRpaReturnOrderThird> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(ItoOrderRpaReturnOrderThird::getCustomerNo)
                .ne(ItoOrderRpaReturnOrderThird::getCustomerNo, "");


        List<ItoOrderRpaReturnOrderThird> result = thirdMapper.selectList(wrapper);
        return result;
    }
    @DS("ds2")
    public List<String> getValue(List<ItoOrderRpaReturnOrderThird> thirdList){
        List<ItoOrderRpaReturnOrderThird> onCondition = this.getOnCondition();
        Map<String, String> cohMap = onCondition.stream()
//                .filter(entity -> StringUtils.isNotBlank(entity.getCohLineNo()) && StringUtils.isNotBlank(entity.getCohLineNo()))
                .collect(Collectors.toMap
                        (data -> data.getCohOrderId() + "_" + data.getCohLineNo(),
                                ItoOrderRpaReturnOrderThird::getCustomerNo,
                                (existingValue, newValue) -> existingValue  )
                );
        List<String> valueList  =     new ArrayList<>();
        for (Map.Entry<String, String> entry : cohMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            valueList.add(value);
            System.out.println("Key: " + key + ", Value: " + value);
        }
        return valueList;
    }

}
