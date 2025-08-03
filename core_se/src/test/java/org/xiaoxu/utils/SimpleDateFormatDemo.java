package org.xiaoxu.utils;

import junit.framework.TestCase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @className: SimpleDateFormatDemo
 * @author: xiaoxu
 * @date: 2025/8/1 7:10
 * @Version: 1.0
 * @description:
 */
public class SimpleDateFormatDemo extends TestCase {


    public void testSimpleDateFormat(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = sdf.format(new Date());
        System.out.println(format);
    }
    public void testSimpleDateFormat2(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        String format = sdf.format(new Date());
        System.out.println(format);
    }
}
