package org.xiaoxu.tcmonitor;


import cn.hutool.core.date.StopWatch;


/**
 * @className: StopWatchD
 * @author: xiaoxu
 * @date: 2025/10/19 21:39
 * @Version: 1.0
 * @description:
 */
public class StopWatchD {
    public static void main(String[] args) throws InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();


        Thread.sleep(5000);


        stopWatch.stop();

//        System.out.println("耗时：" + stopWatch.getTime() + " ms"); // 输出毫秒
//        System.out.println("耗时（秒）：" + stopWatch.getTime(TimeUnit.SECONDS) + " s");
        System.out.println(stopWatch.prettyPrint());
    }
}
