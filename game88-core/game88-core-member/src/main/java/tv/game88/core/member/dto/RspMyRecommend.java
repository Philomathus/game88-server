package tv.game88.core.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspMyRecommend {

    @Schema( title = "我的ID" )
    private String     memberCode;
    @Schema( title = "推荐人ID" )
    private String     inviterCode;
    @Schema( title = "今日佣金" )
    private BigDecimal todaySion;
    @Schema( title = "昨日佣金" )
    private BigDecimal yesterdaySion;
    @Schema( title = "历史佣金" )
    private BigDecimal historySion;
    @Schema( title = "可领取佣金" )
    private BigDecimal canSion;
    @Schema( title = "推广链接" )
    private String     url;
    @Schema( title = "推广图片" )
    private String     shareIcon;
    @Schema( title = "分享背景图片" )
    private String     shareBackground;

}
