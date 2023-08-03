package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对象 config_bank
 *
 * @author mengJun
 */
@Data
@NoArgsConstructor
public class PayRechargeBank {
    @TableId( type = IdType.AUTO )
    private Long          id;
    @Excel( name = "银行ID" )
    private Long          bankId;
    @Excel( name = "银行账号" )
    private String        bankAccount;
    @Excel( name = "开户人姓名" )
    private String        accountName;
    @Excel( name = "开户地址" )
    private String        bankAddress;
    @Excel( name = "优惠比例" )
    private BigDecimal    discountBill;
    @Excel( name = "开放层级" )
    private Integer       openLevelMin;
    private Integer       openLevelMax;
    @Excel( name = "状态" )
    private Boolean       effect;
    @Excel( name = "充值限额" )
    private BigDecimal    rechargeLimitMin;
    private BigDecimal    rechargeLimitMax;
    @Excel( name = "省份限制" )
    private String        restProvince;
    @Excel( name = "备注信息" )
    private String        remark;
    @Excel( name = "创建人" )
    private String        createBy;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    @Excel( name = "最后更新人" )
    private String        updateBy;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "最后更新时间", width = 30, databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime updateTime;

    @TableField( exist = false )
    @Excel( name = "银行名称" )
    private String bankName;
    @TableField( exist = false )
    @Excel( name = "银行图标" )
    private String bankIcon;

    @TableField( exist = false )
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private Integer googleAuthCode;

    @Excel( name = "排序" )
    private Integer       sort;

    @Excel( name = "text2" )
    private String text2;
}
