package tv.game88.wallet.api.dto;

import lombok.Builder;
import lombok.Value;
import tv.game88.wallet.api.type.WalletMessageEnum;

import java.io.Serializable;

@Value
@Builder
public class SseStreamNotificationMessage implements Serializable {
    Long              messageId;
    String            title;
    WalletMessageEnum type;
}
