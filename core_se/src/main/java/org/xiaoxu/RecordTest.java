package org.xiaoxu;

import org.xiaoxu.service.NES;

/**
 * @className: RecordTest
 * @author: xiaoxu
 * @date: 2025/8/12 20:52
 * @Version: 1.0
 * @description:
 */
public class RecordTest {

    public static void main(String[] args) {

        NES game = new NES("gaming", "The Legend of Zelda");

        // 访问字段
        System.out.println("Industry: " + game.industry());
        System.out.println("Name: " + game.name());

        // 使用 toString()
        System.out.println(game.toString()); // 输出: NES[industry=gaming, name=The Legend of Zelda]

        // 创建另一个相同内容的实例
        NES anotherGame = new NES("gaming", "The Legend of Zelda");

        // 比较两个实例
        System.out.println(game.equals(anotherGame)); // 输出: true
    }
}
