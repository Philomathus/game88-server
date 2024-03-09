package tv.game88.wallet.api.dto;

import lombok.Getter;
import lombok.Setter;
import tv.game88.wallet.api.type.WalletMessageEnum;

import java.time.LocalDateTime;

@Getter
@Setter
public class RspWalletMessage {
    //ID type is string due to JS does not support 64bit long Integer.
    private String id;
    private String title;
    private String content;
    private String receiverUserId;
    private Boolean isRead;
    private LocalDateTime createTime;
    private String createBy;
    private WalletMessageEnum type;
}
