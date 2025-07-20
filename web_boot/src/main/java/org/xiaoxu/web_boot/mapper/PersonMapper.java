package org.xiaoxu.web_boot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.xiaoxu.web_boot.entity.Person;

import java.util.List;

public interface PersonMapper extends BaseMapper<Person> {
    Person getPersonInfo(Long id);

    void updateBatch(List<Long> list);
}
