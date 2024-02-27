package tv.game88.wallet.api.dto;

import lombok.Builder;
import lombok.Value;
import tv.game88.wallet.api.type.WalletTransEnum;

import java.io.Serializable;

@Value
@Builder
public class SseStreamTransDetailMessage implements Serializable {
    String          transDetailId;
    Boolean         isSeller;
    WalletTransEnum walletTransEnum;
}
