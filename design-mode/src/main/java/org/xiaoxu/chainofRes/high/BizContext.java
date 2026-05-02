package org.xiaoxu.chainofRes.high;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BizContext {

    private boolean stopped = false;

    private String stopReason;

    private final Map<String, Object> data = new HashMap<>();

    private final List<String> errors = new ArrayList<>();

    public void stop(String reason) {
        this.stopped = true;
        this.stopReason = reason;
    }

    public boolean isStopped() {
        return stopped;
    }

    public String getStopReason() {
        return stopReason;
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public <T> T get(String key, Class<T> clazz) {
        return clazz.cast(data.get(key));
    }

    public void addError(String error) {
        errors.add(error);
    }

    public List<String> getErrors() {
        return errors;
    }
}