package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.common.page.PageDomain;
import tv.game88.wallet.api.type.EnumTransDetail;
import tv.game88.wallet.api.type.WalletTransEnum;

import java.util.List;

@EqualsAndHashCode( callSuper = true )
@Data
public class ReqTransDetailList extends PageDomain {
    @Schema( title = "买卖类型" )
    private EnumTransDetail       type;
    @Schema( title = "交易类型" )
    private List<WalletTransEnum> transType;
}
