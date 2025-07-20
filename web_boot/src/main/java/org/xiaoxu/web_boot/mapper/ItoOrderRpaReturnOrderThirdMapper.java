package org.xiaoxu.web_boot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xiaoxu.web_boot.entity.ItoOrderRpaReturnOrderThird;

import java.util.List;

@Mapper
public interface ItoOrderRpaReturnOrderThirdMapper extends BaseMapper<ItoOrderRpaReturnOrderThird> {
    int deleteByPrimaryKey(Long id);

    int insert(ItoOrderRpaReturnOrderThird record);

    int insertSelective(ItoOrderRpaReturnOrderThird record);

    ItoOrderRpaReturnOrderThird selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ItoOrderRpaReturnOrderThird record);

    int updateByPrimaryKey(ItoOrderRpaReturnOrderThird record);

    List<ItoOrderRpaReturnOrderThird> getOnCondition();

    List<ItoOrderRpaReturnOrderThird> getByName(String id, String name);
}