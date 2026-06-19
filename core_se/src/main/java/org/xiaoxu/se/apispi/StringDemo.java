package org.xiaoxu.se.apispi;

import java.util.UUID;

public class StringDemo {
    public static void main(String[] args) {
        String replace = UUID.randomUUID().toString().replace("-", "");
        String replace2 = UUID.randomUUID().toString().replace("-", "");

        System.out.println(replace);
        System.out.println();
        System.out.println(replace2);

    }
}
