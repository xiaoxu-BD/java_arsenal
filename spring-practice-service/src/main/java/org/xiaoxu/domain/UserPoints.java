package org.xiaoxu.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("t_user_points")
public class UserPoints {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer totalPoints;
    private Integer version;
    private LocalDateTime updateTime;




/*
    public UserPoints userCreate(){
        UserPoints userPoints = new UserPoints();
        userPoints.setVersion(0);
        userPoints.setUpdateTime();


    }*/
}
