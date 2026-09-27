package org.xiaoxu.wecommail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.xiaoxu.wecommail.entity.EnterpriseWechatRecord;

import java.time.LocalDateTime;
import java.util.List;

public interface EnterpriseWechatRecordMapper extends BaseMapper<EnterpriseWechatRecord> {

    /**
     * 核心查询:该群"尚未成功发送"的消息 = 截至 now 已入库,且未被任何一封 SUCCESS 邮件包含。
     *
     * <p>语义:只要消息被任意 SUCCESS 记录包含过即视为已送达(不过滤 declare_info_id,
     * 群删除重建后旧 SUCCESS 仍然算数,避免重复发送)。窗口覆盖三类场景:
     * 常规当日消息 / 迟到消息(进次日邮件) / 失败追补(昨日 FAIL 的消息今日重发自动带上)。
     */
    @Select("""
            SELECT m.* FROM t_e_enterprise_wechat_record m
             WHERE m.room_id = #{roomId}
               AND m.msg_time <= #{now}
               AND NOT EXISTS (
                   SELECT 1 FROM t_e_wechat_mail_send_detail d
                   JOIN t_e_wechat_mail_send_record r ON r.id = d.send_record_id
                    WHERE d.message_id = m.id
                      AND r.send_status = 'SUCCESS'
               )
             ORDER BY m.msg_time ASC
            """)
    List<EnterpriseWechatRecord> findUnsentMessages(@Param("roomId") String roomId,
                                                    @Param("now") LocalDateTime now);
}
