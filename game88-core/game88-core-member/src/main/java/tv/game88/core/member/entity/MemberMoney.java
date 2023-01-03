package tv.game88.core.member.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberMoney {

    /** 会员id */
    @Excel(name = "会员id")
    private String id;

    /** 派送金额 */
    @Excel(name = "派送金额")
    private BigDecimal money;

    /** 打码倍数 */
    @Excel(name = "打码倍数")
    private BigDecimal beat;

    @TableField(exist = false)
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private String moneydes;

    @TableField(exist = false)
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private Integer googleAuthCode;




}
