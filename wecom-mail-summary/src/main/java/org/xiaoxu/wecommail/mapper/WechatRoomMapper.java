package org.xiaoxu.wecommail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.xiaoxu.wecommail.entity.WechatRoom;

public interface WechatRoomMapper extends BaseMapper<WechatRoom> {

    /** 全量镜像同步用:清空缓存表(显式 SQL,保证执行顺序) */
    @Delete("DELETE FROM t_e_wechat_room")
    int deleteAll();
}
