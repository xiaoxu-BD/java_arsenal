package org.xiaoxu.wecommail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.xiaoxu.wecommail.common.SendStatus;
import org.xiaoxu.wecommail.entity.MailSendRecord;

import java.time.LocalDateTime;
import java.util.List;

public interface MailSendRecordMapper extends BaseMapper<MailSendRecord> {

    /**
     * CAS 认领:WAITING/FAIL → SENDING。
     * affected=0 表示已被其他线程/实例认领或状态不允许,调用方直接放弃——多实例并发的最终裁决。
     */
    @Update("UPDATE t_e_wechat_mail_send_record SET send_status = #{to}, update_time = NOW() " +
            "WHERE id = #{id} AND send_status IN ('WAITING', 'FAIL')")
    int casClaimToSending(@Param("id") Long id, @Param("to") SendStatus to);

    /** 成功回写(仅当前持有 SENDING 的线程能更新成功) */
    @Update("UPDATE t_e_wechat_mail_send_record SET send_status = #{success}, send_time = NOW(), update_time = NOW() " +
            "WHERE id = #{id} AND send_status = #{sending}")
    int markSuccess(@Param("id") Long id,
                    @Param("success") SendStatus success,
                    @Param("sending") SendStatus sending);

    /** 失败回写:置 FAIL + 失败原因 + 重试次数自增 */
    @Update("UPDATE t_e_wechat_mail_send_record SET send_status = #{fail}, error_message = #{errorMessage}, " +
            "retry_count = retry_count + 1, update_time = NOW() " +
            "WHERE id = #{id} AND send_status = #{sending}")
    int markFail(@Param("id") Long id,
                 @Param("fail") SendStatus fail,
                 @Param("sending") SendStatus sending,
                 @Param("errorMessage") String errorMessage);

    /** SENDING 超时兜底:实例宕机导致的卡死记录强制置 FAIL,允许后续重试 */
    @Update("UPDATE t_e_wechat_mail_send_record SET send_status = #{fail}, error_message = #{errorMessage}, " +
            "retry_count = retry_count + 1, update_time = NOW() " +
            "WHERE send_status = #{sending} AND update_time < #{cutoff}")
    int markStuckSendingFailed(@Param("sending") SendStatus sending,
                               @Param("fail") SendStatus fail,
                               @Param("errorMessage") String errorMessage,
                               @Param("cutoff") LocalDateTime cutoff);

    /** 认领后写快照:收件人/抄送/消息数(发送前配置可能被修改,以发送时刻为准) */
    @Update("UPDATE t_e_wechat_mail_send_record SET to_addresses = #{toAddresses}, cc_addresses = #{ccAddresses}, " +
            "message_count = #{messageCount}, update_time = NOW() WHERE id = #{id}")
    int updateSnapshot(@Param("id") Long id,
                       @Param("toAddresses") String toAddresses,
                       @Param("ccAddresses") String ccAddresses,
                       @Param("messageCount") int messageCount);

    /** 管理端列表:最新 limit 条 */
    @Select("SELECT * FROM t_e_wechat_mail_send_record ORDER BY create_time DESC, id DESC LIMIT #{limit}")
    List<MailSendRecord> findLatest(@Param("limit") int limit);
}
