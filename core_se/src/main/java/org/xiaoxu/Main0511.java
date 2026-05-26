package org.xiaoxu;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class Main0511 {
    public static void main(String[] args) {

        String name = "11";

     /*   Map<String, String> allMap = getAllMap(name);

        System.out.println(allMap.get("getName"));*/
        char ch  = '泉';
        System.out.println(ch);



        try
        {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        }catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
//        NoSuchMethodException


    }







    public static Map<String,String> getAllMap(String name){
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < 2; i++) {

            if (StringUtils.isBlank(name)){
                break;
            }
            map.put("getName",name);

        }


        return map;




    }


}
