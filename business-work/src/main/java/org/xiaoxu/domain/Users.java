package org.xiaoxu.domain;

import java.util.Date;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Users {
    @NotNull(message = "不能为null")
    private Long id;

    @Size(max = 64,message = "最大长度要小于 64")
    private String name;

    private Date createTime;

    private Date modifyTime;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }
}