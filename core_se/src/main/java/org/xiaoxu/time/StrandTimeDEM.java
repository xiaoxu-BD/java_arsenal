package org.xiaoxu.time;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Date;


public class StrandTimeDEM {
    public static void main(String[] args) throws ParseException {
        LocalDateTime now =
                LocalDateTime.now();
        LocalDateTime time = now.minusDays(1);
        String formatted = DateUtil.format(time, "yyyyMM:dd:HH:ss");
        System.out.println(formatted);
    }
}
