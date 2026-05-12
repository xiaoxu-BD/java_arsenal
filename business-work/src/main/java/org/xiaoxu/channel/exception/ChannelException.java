package org.xiaoxu.channel.exception;

public class ChannelException extends RuntimeException {

    private final String channelCode;
    private final String bizId;

    public ChannelException(String message) {
        super(message);
        this.channelCode = null;
        this.bizId = null;
    }

    public ChannelException(String channelCode, String bizId, String message) {
        super(message);
        this.channelCode = channelCode;
        this.bizId = bizId;
    }

    public ChannelException(String channelCode, String bizId, String message, Throwable cause) {
        super(message, cause);
        this.channelCode = channelCode;
        this.bizId = bizId;
    }

    public ChannelException(String message, Throwable cause) {
        super(message, cause);
        this.channelCode = null;
        this.bizId = null;
    }

    public String getChannelCode() { return channelCode; }
    public String getBizId() { return bizId; }
}
