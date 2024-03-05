package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.wallet.api.constants.ConstantsWallet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RspSellOrderDetail2 {
    @Schema( title = "昵称" )
    private String  nikeName;
    @Schema( title = "等级" )
    private Integer level;
    @Schema( title = "头像" )
    private String  headImg;
    @Schema( title = "买单次数" )
    private Long    buyOrderNum;
    @Schema( title = "卖单次数" )
    private Long    sellOrderNum;

    @Schema( title = "挂单ID" )
    private String  transactionId;
    @Schema( title = "可购买总额(G币)" )
    private Long    amount;
    @Schema( title = "是否可拆分" )
    private Boolean canSplit;
    /**
     * 最低可购买金额
     */
    @Schema( title = "最小购买量" )
    private Long minBuyNum;

    @Schema( title = "30日成单数" )
    private Integer successNumMonth;
    @Schema( title = "30日成单率" )
    private String  successRateMonth;
    @Schema( title = "30日平均付款时间" )
    private String  receivedTimeMonth;
    @Schema( title = "30日平均放币时间" )
    private String  transferTimeMonth;
    @Schema( title = "卖家收款方式类型", description = "英文逗号,分割" )
    private String  payMethodTypes;

    @Schema( title = "信用等级" )
    private Integer    creditRating;

    @Schema( title = "买家收款方式" )
    private List<RspPayMethod2> rspPayMethodMap = new ArrayList<>();

    public String getHeadImg() {
        if ( StringUtils.isBlank( headImg ) ) {
            return ConstantsWallet.DEFAULT_HEAD_IMAGE_URL;
        }
        return ConfigDomainCacheUtil.me.getDomainOssValue() + headImg;
    }
}
