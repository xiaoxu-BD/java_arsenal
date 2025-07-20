package org.xiaoxu.web_boot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xiaoxu.web_boot.aop.DS;
import org.xiaoxu.web_boot.common.PageResult;
import org.xiaoxu.web_boot.entity.Person;
import org.xiaoxu.web_boot.mapper.PersonMapper;
import org.xiaoxu.web_boot.service.PersonService;

import java.util.List;

/**
 * @className: PersonServiceImpl
 * @author: xiaoxu
 * @date: 2025/7/20 15:17
 * @Version: 1.0
 * @description:
 */
@Service
public class PersonServiceImpl extends ServiceImpl<PersonMapper, Person> implements PersonService {

    @Autowired
    private PersonMapper personMapper;

    @DS("ds1")
    @Override
    public Person getPersonInfo(Long id) {
        Person person =   personMapper.getPersonInfo(id);
        return person;
    }

    @DS("ds1")
    @Override
    public void updateBatch() {
        List<Person> personList = personMapper.selectList(new LambdaQueryWrapper<Person>().isNotNull(Person::getId));
        List<Long> idList = personList.stream().map(Person::getId).toList();
        personMapper.updateBatch(idList);
    }

    @Override
    public PageResult<Person> getPersonPage(int pageNo, int pageSize) {
        Page<Person> page = new Page<>(pageNo, pageSize);


        Page<Person> personPage = personMapper.selectPage(page, new LambdaQueryWrapper<Person>().isNotNull(Person::getId));
        if (personPage != null) {
          return   PageResult.of(personPage);
        }
        return null;
    }
}
