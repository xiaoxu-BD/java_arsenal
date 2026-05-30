package org.xiaoxu.pojo.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserRoleRequest {

        private Long userId;
        private List<Long> roleIds;
}
