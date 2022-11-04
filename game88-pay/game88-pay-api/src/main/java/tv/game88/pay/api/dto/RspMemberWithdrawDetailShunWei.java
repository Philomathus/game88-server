package tv.game88.pay.api.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

@Data
public class RspMemberWithdrawDetailShunWei {
    private static final long serialVersionUID = 1L;

    /**
     * 提现银行
     */
    @Excel( name = "银行名称", orderNum = "1" )
    private String bankName;

    /**
     * 收款人
     */
    @Excel( name = "持卡人姓名", orderNum = "2" )
    private String bankUserName;

    /**
     * 提现账号
     */
    @Excel( name = "银行卡号", orderNum = "3" )
    private String bankAccount;

    /**
     * 提现金额
     */
    @Excel( name = "提现金额", orderNum = "4" )
    private Integer withdrawMoney;

    /**
     * 订单号
     */
    @Excel( name = "订单号", orderNum = "5" )
    private String orderNo;

}
