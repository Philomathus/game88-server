package tv.game88.game.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GameWashCodeLog {
    @TableId( type = IdType.INPUT )
    private String        washId;
    @Schema( title = "会员id" )
    private String        memberId;
    @Schema( title = "打码量" )
    private BigDecimal    codeAmount;
    @Schema( title = "洗码比例" )
    private BigDecimal    washCodeRate;
    @Schema( title = "洗码金额" )
    private BigDecimal    washCodeAmount;
    @Schema( title = "洗码时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime washCodeTime;
    @Schema( title = "游戏类型ID" )
    private Long          gameTypeId;
    @TableField( exist = false )
    @Schema( title = "打码比例" )
    private BigDecimal    beat;
}
