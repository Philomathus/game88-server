package tv.game88.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspConfigBankList {
    @Schema( title = "主键" )
    private Long   id;
    @Schema( title = "银行名称" )
    private String bankName;
    @Schema( title = "银行图标" )
    private String bankIcon;
    @Schema( title = "排序" )
    private Long   sort;
}
