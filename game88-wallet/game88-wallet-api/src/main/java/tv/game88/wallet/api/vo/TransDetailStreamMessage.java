package tv.game88.wallet.api.vo;

import lombok.Builder;
import lombok.Value;
import tv.game88.wallet.api.type.WalletTransEnum;

@Value
@Builder
public class TransDetailStreamMessage {
    String transDetailId;
    WalletTransEnum walletTransEnum;
}
