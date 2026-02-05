package org.xiaoxu.debug;

import org.xiaoxu.domain.Employee;
import org.xiaoxu.domain.User;

import java.util.ArrayList;
import java.util.List;

/**
 * @className: Main113
 * @author: xiaoxu
 * @date: 2026/1/13 20:43
 * @Version: 1.0
 * @description:
 */
public class Main113 {
    public static void main(String[] args) {

        List<User> userList = new ArrayList<>(6);
        userList.add(new User(1, "Tom", 19));
        userList.add(new User(2, "Giles", 25));
        userList.add(new User(3, "Alex", 69));
        userList.add(new User(4, "Ryan", 21));
        userList.add(new User(5, "DongGe", 19));
        userList.add(new User(6, "RUI", 21));

        userList.forEach(user -> {
            String name = user.getName();
            int futureAge = user.getAge() + 10;
            System.out.println(name + " 10年后" + futureAge + "岁");
        });
    }

}

