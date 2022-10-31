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
 * USDT充值对象 member_recharge_usdt
 *
 * @author mengJun
 */
@Data
@NoArgsConstructor
public class MemberRechargeUsdt {
    @Excel( name = "订单号", orderNum = "1" )
    @TableId( type = IdType.INPUT )
    private String        rechargeOrderNo;
    @Excel( name = "会员编号" )
    private String        memberId;
    @Excel( name = "交易id" )
    private String        transactionId;
    @Excel( name = "充值金额" )
    private BigDecimal    rechargeMoney;
    @Excel( name = "渠道名称" )
    private String        channelName;
    @Excel( name = "链名称" )
    private String        chainName;
    @Excel( name = "充值地址" )
    private String        rechargeAddress;
    @Excel( name = "充值U数量" )
    private Long          rechargeNumber;
    @Excel( name = "充值汇率" )
    private BigDecimal    rechargeRate;
    @Excel( name = "状态(0锁定 1已提交 2拒绝 3通过)" )
    private Integer       status;
    @Excel( name = "优惠比例" )
    private BigDecimal    discountBill;
    @Excel( name = "操作人" )
    private String        opName;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "申请时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "审核时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime updateTime;
    @Excel( name = "备注" )
    private String        remark;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @TableField( exist = false )
    private Integer googleAuthCode;
}
