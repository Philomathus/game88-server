package tv.game88.wallet.api.dto;

import lombok.Builder;
import tv.game88.wallet.api.type.WalletMessageEnum;

import java.time.LocalDateTime;

@Builder
public record RspWalletMessage(
        //ID type is string due to JS does not support 64bit long Integer.
        String id,
        String title,
        String content,
        String receiverUserId,
        Boolean isRead,
        WalletMessageEnum type,
        String createBy,
        LocalDateTime createTime
) {
}
