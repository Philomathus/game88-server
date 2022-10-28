package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员充值记录对象 member_recharge_bank
 *
 * @author mengJun
 */
@Data
public class MemberRechargeBank {
    @Excel( name = "订单号", orderNum = "1" )
    @TableId( type = IdType.INPUT )
    private String        orderNo;
    @Excel( name = "会员编号", orderNum = "2" )
    private String        memberId;
    @Excel( name = "充值平台金额", orderNum = "3" )
    private BigDecimal    rechargeMoney;
    // 0已提交1初级审核通过2审核不通过3终极审核通过4拒绝
    @Excel( name = "订单状态", orderNum = "4" )
    private Integer       status;
    @Excel( name = "存款人姓名", orderNum = "6" )
    private String        rechargeUserName;
    @Excel( name = "收款人", orderNum = "7" )
    private String        bankUserName;
    @Excel( name = "收款银行", orderNum = "8" )
    private String        bankName;
    @Excel( name = "收款账号", orderNum = "9" )
    private String        bankAccount;
    @Excel( name = "开户地址", orderNum = "10" )
    private String        bankAddress;
    @Excel( name = "操作人", orderNum = "11" )
    private String        opName;
    @Excel( name = "备注", orderNum = "12" )
    private String        remark;
    @Excel( name = "是否首次", orderNum = "13" )
    private boolean       first = false;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss", orderNum = "14" )
    private LocalDateTime createTime;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "更新时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss", orderNum = "15" )
    private LocalDateTime updateTime;
    @Excel( name = "请求IP", orderNum = "16" )
    private String        ip;
    @Excel( name = "优惠比例", orderNum = "17" )
    private BigDecimal    discountBill;

    @TableField( exist = false )
    @JsonIgnore
    private String startDate;
    @TableField( exist = false )
    @JsonIgnore
    private String endDate;

    @TableField( exist = false )
    private Integer nameStatus;
    @TableField( exist = false )
    private String  statusDesc;

    public String getStatusDesc() {
        if ( status != null ) {
            switch ( status ) {
            case 0:
                return "未收款";
            case 1:
                return "初级审核通过";
            case 2:
                return "审核不通过";
            case 3:
                return "终极审核通过";
            case 4:
                return "入库失败";
            default:
            }
        }
        return "";
    }

    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE ).append( "orderId", getOrderNo() )
                                                                          .append( "memberId", getMemberId() )
                                                                          .append( "rechargeMoney", getRechargeMoney() )
                                                                          .append( "bankName", getBankName() )
                                                                          .append( "bankAccount", getBankAccount() )
                                                                          .append( "status", getStatus() )
                                                                          .append( "remark", getRemark() )
                                                                          .append( "opName", getOpName() )
                                                                          .append( "createTime", getCreateTime() )
                                                                          .append( "updateTime", getUpdateTime() )
                                                                          .append( "bankAddress", getBankAddress() )
                                                                          .append( "rechargeUserName", getRechargeUserName() )
                                                                          .append( "bankUserName", getBankUserName() )
                                                                          .append( "orderNo", getOrderNo() )
                                                                          .append( "discountBill", getDiscountBill() )
                                                                          .append( "first", isFirst() ).toString();
    }
}
