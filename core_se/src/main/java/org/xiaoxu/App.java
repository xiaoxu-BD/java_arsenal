package org.xiaoxu;


import lombok.extern.slf4j.Slf4j;
import org.xiaoxu.enums.Week;

/**
 * Hello world!
 *
 */
@Slf4j
public class App 
{
    public static void main( String[] args )
    {
        String value = "ZZzZZKrp194289102792071987280502$name";
        String[] split = value.split("\\$");
        System.out.println(split[0]);
        System.out.println(split[1]);
    }

}
