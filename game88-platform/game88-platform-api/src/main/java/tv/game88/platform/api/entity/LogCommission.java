package tv.game88.platform.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LogCommission {
    @Schema( title = "系统编号" )
    @TableId(type = IdType.AUTO)
    private Long          id;
    @Schema( title = "会员ID" )
    private String        memberId;
    @Schema( title = "佣金" )
    private BigDecimal    commission;
    @Schema( title = "创建时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
}