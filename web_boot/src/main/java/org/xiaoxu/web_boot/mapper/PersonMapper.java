package org.xiaoxu.web_boot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.CacheNamespace;
import org.xiaoxu.web_boot.entity.Person;

import java.util.List;

//@CacheNamespace
public interface PersonMapper extends BaseMapper<Person> {
    Person getPersonInfo(Long id);

    void updateBatch(List<Long> list);


    /**
     * 动态sql IF
     *
     */
    List<Person> getPeronInfoByName(String name);

}
