package tv.game88.wallet.api.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@TableName( value = "report_user_amount" )
@Data
public class ReportUserAmount {

    private Long userAmount;

    private Long userFrozenAmount;

    private Long upAmount;

    private Long upPeople;

    private Long downAmount;

    private Long downPeople;

    private Long memberBuy;

    private LocalDateTime reportTime;

    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    @TableField( exist = false )
    private String beginTime;

    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    @TableField( exist = false )
    private String endTime;
}
