package org.xiaoxu.wecommail.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.xiaoxu.wecommail.common.BusinessException;
import org.xiaoxu.wecommail.config.WecomMailProperties;
import org.xiaoxu.wecommail.entity.WechatRoom;
import org.xiaoxu.wecommail.mapper.WechatRoomMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 群列表数据源(接入节点一:配置 roomId 时的下拉数据)。
 *
 * <p>两种模式,由 wecom.room-list-api.url 决定:
 * <ul>
 *   <li><b>url 为空(当前)</b>:使用<b>后端写死的 Mock 群列表</b>(见 {@link #builtinMockRooms}),联调开箱即用;</li>
 *   <li><b>url 已配置</b>:调用其他开发组群列表 HTTP 接口。</li>
 * </ul>
 * 两种模式走同一条链路:同步 → 全量镜像落库(t_e_wechat_room) → 下拉从本地库读 → 附 configured 标记。
 * 上游不可用时回退本地缓存。
 *
 * <p><b>群列表只提供 roomId,不提供群名称</b>——群名称由管理员在新增群时人工确认填写
 * (t_e_wechat_room 不存群名,群名存在配置表里)。
 *
 * <p>HTTP 响应格式约定(roomId 字段名做了常见兼容):<pre>["ROOM_001"]</pre>
 * 或 <pre>{"code":0,"data":[{"roomId":"ROOM_001"}]}</pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatRoomClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final WechatRoomMapper roomMapper;
    private final WecomMailProperties props;

    private volatile LocalDateTime lastSyncTime;

    /** 当前是否为后端写死 Mock 模式(url 未配置) */
    public boolean isMockMode() {
        String url = props.getRoomListApi().getUrl();
        return url == null || url.isBlank();
    }

    /**
     * @param forceSync true 表示跳过 TTL 强制回源
     */
    public List<WechatRoom> listAllRooms(boolean forceSync) {
        WecomMailProperties.RoomListApi cfg = props.getRoomListApi();
        boolean stale = lastSyncTime == null
                || LocalDateTime.now().isAfter(lastSyncTime.plusMinutes(cfg.getCacheTtlMinutes()));
        if (!forceSync && !stale) {
            return listFromDb();
        }
        if (isMockMode()) {
            log.info("群列表使用后端写死的 Mock 数据(未配置 wecom.room-list-api.url)");
            syncToDb(builtinMockRooms());
        } else {
            List<WechatRoom> fetched;
            try {
                fetched = fetchRooms(cfg);
            } catch (Exception e) {
                List<WechatRoom> cached = listFromDb();
                if (!cached.isEmpty()) {
                    log.warn("群列表上游接口调用失败,沿用本地缓存 {} 条: {}", cached.size(), e.getMessage());
                    return cached;
                }
                throw new BusinessException("群列表接口调用失败且本地无缓存: " + e.getMessage());
            }
            syncToDb(fetched);
        }
        return listFromDb();
    }

    private List<WechatRoom> listFromDb() {
        return roomMapper.selectList(new LambdaQueryWrapper<WechatRoom>().orderByAsc(WechatRoom::getRoomId));
    }

    /**
     * 全量镜像落库:先清后插。MyBatis 语句按书写顺序执行,不存在 Hibernate
     * 先 INSERT 后 DELETE 的刷写顺序问题;各语句独立提交,中断最多留下一次不完整同步,下次自动修复。
     */
    private void syncToDb(List<WechatRoom> rooms) {
        roomMapper.deleteAll();
        LocalDateTime now = LocalDateTime.now();
        for (WechatRoom room : rooms) {
            room.setSyncTime(now);
            roomMapper.insert(room);
        }
        lastSyncTime = now;
        log.info("群列表已同步落库: {} 条", rooms.size());
    }

    /** 后端写死的联调群列表(仅 roomId):正式接口就绪后配置 url 即自动切换,无需改代码 */
    private static List<WechatRoom> builtinMockRooms() {
        return List.of(
                buildRoom("ROOM_001"),
                buildRoom("ROOM_002"),
                buildRoom("ROOM_003"),
                buildRoom("ROOM_004"),
                buildRoom("ROOM_005"));
    }

    private static WechatRoom buildRoom(String roomId) {
        WechatRoom room = new WechatRoom();
        room.setRoomId(roomId);
        return room;
    }

    private List<WechatRoom> fetchRooms(WecomMailProperties.RoomListApi cfg) throws Exception {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(cfg.getConnectTimeoutMs());
        factory.setReadTimeout(cfg.getReadTimeoutMs());
        String body = new RestTemplate(factory).getForObject(cfg.getUrl(), String.class);
        if (body == null || body.isBlank()) {
            throw new IllegalStateException("群列表接口返回空");
        }
        JsonNode root = MAPPER.readTree(body);
        JsonNode arr = root.isArray() ? root : root.path("data");
        if (!arr.isArray()) {
            throw new IllegalStateException("群列表响应格式不符合约定(需为数组或 {data:[...]}): "
                    + body.substring(0, Math.min(body.length(), 120)));
        }
        List<WechatRoom> rooms = new ArrayList<>();
        for (JsonNode node : arr) {
            String roomId = firstNonBlank(
                    node.path("roomId").asText(), node.path("roomid").asText(), node.path("room_id").asText());
            if (roomId.isBlank()) {
                continue;
            }
            WechatRoom room = new WechatRoom();
            room.setRoomId(roomId.trim());
            rooms.add(room);
        }
        return rooms;
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return "";
    }
}
