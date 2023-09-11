package tv.game88.wallet.api.type;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum WalletPayMethodEnum {
    CREDIT_CARD,
    WECHAT_PAY,
    ALIPAY;

    public static List<String> getPayMethodTypes() {
        return Arrays.stream( WalletPayMethodEnum.values() ).map( Enum::name ).collect( Collectors.toList() );
    }
}
