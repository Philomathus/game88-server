package tv.game88.wallet.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode( callSuper = true )
public class RspWalletRecordPay extends RspWalletRecord {
    private String payUrl;
}
