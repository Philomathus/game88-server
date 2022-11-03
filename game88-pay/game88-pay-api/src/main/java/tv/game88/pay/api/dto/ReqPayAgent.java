package tv.game88.pay.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReqPayAgent {
    private Long         payAgentChannelId;
    private String       withdrawOrderNo;
    private List<String> withdrawOrderNos;
    private String       googleAuthCode;

    // 失败原因
    private String        failReason;
    private LocalDateTime currentTime;
}

