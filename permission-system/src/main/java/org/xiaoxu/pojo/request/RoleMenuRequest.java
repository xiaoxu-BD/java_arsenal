package org.xiaoxu.pojo.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class RoleMenuRequest {
    private Long roleId;
    private List<Long> menuIds;
}
