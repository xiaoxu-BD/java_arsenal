package org.xiaoxu.web_boot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xiaoxu.web_boot.common.PageResult;
import org.xiaoxu.web_boot.entity.Person;

import java.util.List;

public interface PersonService extends IService<Person> {

    /**
     * 获取人员信息
     * @param id   人员id
     * @return     人员信息 包含地址信息
     *
     * */
    Person getPersonInfo(Long id);

    void updateBatch();

    PageResult<Person> getPersonPage(int pageNo, int pageSize);


    /**
     * 动态sql IF
     */
    List<Person> getPersonByName(String name);
}
