package org.xiaoxu.wecommail.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserDashboardVO {
    private UserInfo userInfo;
    private List<Order> orders;
    private Integer points;
}