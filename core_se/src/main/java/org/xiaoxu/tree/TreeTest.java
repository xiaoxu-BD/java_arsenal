package org.xiaoxu.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TreeTest {


    public static void main(String[] args) {
        List<TreeNode> tree = getTree(getTestData());
        printTree(tree, "");
    }
    public static List<TreeNode> getTestData() {
        List<TreeNode> list = new ArrayList<>();
        
        // 根节点
        list.add(new TreeNode(1L, 0L, "根节点"));
        
        // 第一层
        list.add(new TreeNode(2L, 1L, "一级节点 A"));
        list.add(new TreeNode(3L, 1L, "一级节点 B"));
        
        // 第二层 (隶属于一级节点 A)
        list.add(new TreeNode(4L, 2L, "二级节点 A-1"));
        list.add(new TreeNode(5L, 2L, "二级节点 A-2"));
        
        // 第二层 (隶属于一级节点 B)
        list.add(new TreeNode(6L, 3L, "二级节点 B-1"));
        
        // 特殊情况：parentId 为 null 的根节点
        list.add(new TreeNode(7L, null, "孤立根节点"));

        return list;
    }






    public static void printTree(List<TreeNode> nodes, String prefix) {
        for (int i = 0; i < nodes.size(); i++) {
            TreeNode node = nodes.get(i);
            boolean isLast = (i == nodes.size() - 1);
            // 当前节点的连接符
            System.out.println(prefix + (isLast ? "└── " : "├── ") + node.getName());
            // 子节点的前缀：最后一个用空格，其他用竖线
            String childPrefix = prefix + (isLast ? "    " : "│   ");
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                printTree(node.getChildren(), childPrefix);
            }
        }
    }

    public static List<TreeNode> getTree(List<TreeNode> list){
        Map<Long, TreeNode> nodeMap = list.stream()
                .collect(Collectors.toMap(TreeNode::getId, node -> node));

        List<TreeNode> roots = new ArrayList<>();

        for (TreeNode node : list) {
            if (node.getParentId() == null || node.getParentId() == 0) {
                roots.add(node);
            } else {
                TreeNode parent = nodeMap.get(node.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(node);
                }
            }
        }

        return roots;
    }


}