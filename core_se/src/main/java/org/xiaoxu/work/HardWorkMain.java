package org.xiaoxu.work;

public class HardWorkMain {


    public static void main(String[] args) {
        System.out.println(testCache("", "bd"));
    }



    public static boolean testCache(String value,String type){
        HardData data = new HardData();
        if (type.equals("bd")){
            data.setBd("2");
            data.setBdEn(null);
        } else if (type.equals("bdEn")) {
            data.setBdEn("bdEn");
            data.setBd(null);
        }

        if ( !(Constant.TWO.equals(data.getBd()) || "bdEn".equals(data.getBdEn()))){
            return false;
        }

        return true;


    }
}
