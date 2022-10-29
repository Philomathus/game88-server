package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对象 config_bank
 *
 * @author 77lm
 * @date 2021-10-14
 */
@Data
public class ConfigBank {
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
    private Boolean       effect = false;
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
    @JsonIgnore
    private Integer googleAuthCode;
}
