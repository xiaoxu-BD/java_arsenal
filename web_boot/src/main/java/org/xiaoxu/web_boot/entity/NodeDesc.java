package org.xiaoxu.web_boot.entity;

import lombok.Data;

import java.util.List;

/**
 * @className: NodeDesc
 * @author: xiaoxu
 * @date: 2025/7/6 16:44
 * @Version: 1.0
 * @description:
 */
@Data
public class NodeDesc {
    private int id;
    private String name;
    private String desc;

    private List<Object>  params;
}
