package org.xiaoxu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.xiaoxu.pojo.OutboxMessageEntity;

import java.util.List;

@Mapper
public interface OutboxMapper extends BaseMapper<OutboxMessageEntity> {


    // 拉取一批未处理的消息，并且加「行锁」（关键！防止重复消费）
    List<OutboxMessageEntity> selectUnprocessedForUpdateSkipLocked(
            @Param("batchSize") int batchSize);

    // 处理完后批量标记为已处理
    int updateBatchProcessed(@Param("ids") List<Long> ids);
}
