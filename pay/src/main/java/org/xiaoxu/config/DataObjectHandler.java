package org.xiaoxu.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// handler/DataObjectHandler.java
@Component
@Primary
public class DataObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        setFieldValByNameIfNull("createTime", now, metaObject);
        setFieldValByNameIfNull("updateTime", now, metaObject);
        setFieldValByNameIfNull("deleted", 0, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }

    private void setFieldValByNameIfNull(String fieldName, Object value, MetaObject metaObject) {
        Object existing = getFieldValByName(fieldName, metaObject);
        if (existing == null) {
            setFieldValByName(fieldName, value, metaObject);
        }
    }

    public Object getFieldValByName(String fieldName, MetaObject metaObject) {
        try {
            return metaObject.getValue(fieldName);
        } catch (Exception e) {
            return null;
        }
    }
}