package tv.game88.wallet.api.vo;

import lombok.Builder;
import lombok.Value;
import tv.game88.wallet.api.type.WalletTransEnum;

import java.io.Serializable;

@Value
@Builder
public class TransDetailStreamMessage implements Serializable {
    String transDetailId;
    Boolean isSeller;
    WalletTransEnum walletTransEnum;
}
