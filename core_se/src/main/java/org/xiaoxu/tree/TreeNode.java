package org.xiaoxu.tree;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class TreeNode {
    private Long id;
    private Long parentId;
    private String name; // 用于直观展示
    private List<TreeNode> children;

    public TreeNode(Long id, Long parentId, String name) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
    }

}