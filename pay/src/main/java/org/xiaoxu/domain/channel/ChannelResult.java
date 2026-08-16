package org.xiaoxu.domain.channel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// channel/ChannelResult.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChannelResult {

    private boolean success;
    private String  transactionId;
    private String  errorMessage;

    public static ChannelResult ok(String txnId) {
        return new ChannelResult(true, txnId, null);
    }

    public static ChannelResult fail(String msg) {
        return new ChannelResult(false, null, msg);
    }
}