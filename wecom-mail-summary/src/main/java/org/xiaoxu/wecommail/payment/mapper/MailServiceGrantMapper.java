package org.xiaoxu.wecommail.payment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xiaoxu.wecommail.payment.entity.MailServiceGrant;

@Mapper
public interface MailServiceGrantMapper {

    int insert(MailServiceGrant grant);

    int sumServiceDays(@Param("userId") long userId);
}
