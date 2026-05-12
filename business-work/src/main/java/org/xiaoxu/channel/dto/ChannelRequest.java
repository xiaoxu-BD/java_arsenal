package org.xiaoxu.channel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelRequest {

    private String channelCode;
    private String bizType;
    private String bizId;
    private String action;

    private String url;
    private String requestBody;

    private int maxRetry = 3;
}
