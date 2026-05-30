package org.xiaoxu.se.apispi;

public class FileLoggerSpi implements LoggerSpi {
    @Override
    public void log(String message) {
        System.out.println("[File] 写入日志文件: " + message);
    }

    @Override
    public String type() {
        return "file";
    }
}
