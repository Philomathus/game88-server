package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员提现信息对象 member_withdraw_detail
 *
 * @author mengJun
 */
@Data
@NoArgsConstructor
public class MemberWithdrawDetail {
    @TableId( type = IdType.INPUT )
    @Excel( name = "提现订单号", orderNum = "1" )
    private String     withdrawOrderNo;
    @Excel( name = "会员编号", orderNum = "2" )
    private String     memberId;
    @Excel( name = "提现金额", orderNum = "3" )
    private BigDecimal withdrawMoney;
    @Excel( name = "银行ID", orderNum = "4" )
    private Long       bankId;
    @Excel( name = "提现账号", orderNum = "5" )
    private String     bankAccount;
    @Excel( name = "开户地", orderNum = "6" )
    private String     bankAddress;
    @Excel( name = "开户银行真实姓名", orderNum = "7" )
    private String     bankUserName;
    //0申请中 1锁定 2审核不通过 3人工入款成功 4代付中 5代付失败 6代付成功
    private Integer    status;
    @Excel( name = "会员状态", orderNum = "9" )
    private Integer    memberStatus;
    @Excel( name = "操作人", orderNum = "10" )
    private String     opName;
    @Excel( name = "备注", orderNum = "11" )
    private String     remark;
    @Excel( name = "是否首次", orderNum = "12" )
    private Boolean    first = false;
    @Excel( name = "入款出款比", orderNum = "13" )
    private BigDecimal rechargeWithdrawRate;
    @Excel( name = "公司入款成功次数", orderNum = "14" )
    private Integer    bankRechargeNum;
    @Excel( name = "真实银行卡地址(省/市)", orderNum = "15" )
    private String     realBankAddress;
    @Excel( name = "公司入款人姓名", orderNum = "16" )
    private String     rechargeUserName;
    @Excel( name = "投注打码比例", orderNum = "17" )
    private BigDecimal rechargeBcodeRate;

    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "申请时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss", orderNum = "18" )
    private LocalDateTime createTime;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "审核时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss", orderNum = "19" )
    private LocalDateTime updateTime;

    @TableField( exist = false )
    @Excel( name = "订单状态", orderNum = "7" )
    private String statusDes;

    @TableField( exist = false )
    @JsonIgnore
    private String  statusName;
    //公司入款姓名与提现姓名状态
    @TableField( exist = false )
    @JsonIgnore
    private Integer rechargeUserNameStatus;
    @TableField( exist = false )
    @JsonIgnore
    private String  payAgentOrderNo;

    @TableField( exist = false )
    @JsonIgnore
    private String[] searchTime;
    @TableField( exist = false )
    @JsonIgnore
    private String   startTime;
    @TableField( exist = false )
    @JsonIgnore
    private String   endTime;

    public String getStatusDes() {
        if ( status != null ) {
            return switch ( status ) {
                case 0 -> "入账中";
                case 1 -> "初级审核通过";
                case 2 -> "审核不通过";
                case 3 -> "终极审核通过";
                case 4 -> "代付中";
                case 5 -> "代付失败";
                case 6 -> "代付成功";
                case 7 -> "出款异常";
                case 8 -> "人工代付中";
            };
        }
        return "";
    }

    public String getStartTime() {
        if ( searchTime != null && searchTime.length > 0 ) {
            return searchTime[ 0 ];
        }
        return null;
    }

    public String getEndTime() {
        if ( searchTime != null && searchTime.length > 0 ) {
            return searchTime[ 1 ];
        }
        return null;
    }

}
