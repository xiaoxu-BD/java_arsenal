package org.xiaoxu.wecommail.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EnrichedDetail {
    private UserDetail detail;          // 原始详情
    private List<String> permissions;   // 补充的权限
}