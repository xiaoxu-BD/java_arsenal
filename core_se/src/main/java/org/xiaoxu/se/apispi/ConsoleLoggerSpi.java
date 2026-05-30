package org.xiaoxu.se.apispi;

public class ConsoleLoggerSpi implements LoggerSpi {
    @Override
    public void log(String message) {
        System.out.println("[Console] " + message);
    }

    @Override
    public String type() {
        return "console";
    }
}
