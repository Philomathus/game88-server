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
 * 会员充值记录对象 member_recharge_bank
 *
 * @author mengJun
 */
@Data
public class MemberRechargeBank {
    @Excel( name = "订单号", orderNum = "1" )
    @TableId( type = IdType.INPUT )
    private String        rechargeOrderNo;
    @Excel( name = "会员编号", orderNum = "2" )
    private String        memberId;
    @Excel( name = "充值金额", orderNum = "3" )
    private BigDecimal    rechargeMoney;
    // 0已提交1初级审核通过2审核不通过3终极审核通过4拒绝
    // 订单状态
    private Integer       status;
    // payType的ID,用于区分银行卡/USDT..等其它虚拟币
    @Excel( name = "存款类型", orderNum = "5" )
    private Integer       rechargeType;
    @Excel( name = "存款人姓名", orderNum = "6" )
    private String        rechargeUserName;
    @Excel( name = "收款人", orderNum = "7" )
    private String        bankUserName;
    @Excel( name = "收款银行ID", orderNum = "8" )
    private String        bankId;
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
    @Excel( name = "订单状态", orderNum = "4" )
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

}
