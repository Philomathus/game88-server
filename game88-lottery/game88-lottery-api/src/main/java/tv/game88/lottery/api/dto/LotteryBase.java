package tv.game88.lottery.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.lottery.api.utils.LotteryUtils;

@Data
public class LotteryBase {
    @Schema( title = "彩票ID" )
    private Integer id;
    @Schema( title = "彩票名称" )
    private String  name;
    @Schema( title = "封盘倒计时" )
    private Long    fdown;
    @Schema( title = "周期" )
    private Integer cycle;
    @Schema( title = "下注开始秒数" )
    private Integer betBeginSec;

    public long getFdown() {
        return cycle * 60 - 5;
    }

    /**
     * 0=时时彩
     * 1=11选5
     * 2=快三
     * 3=赛车
     * 4=六合彩
     * 获取彩票种类
     */
    public int getKind() {
        return LotteryUtils.getKindId( id );
    }
}
