package tv.game88.pay.api.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 线上充值信息对象 member_pay_jour
 *
 * @author mengJun
 * @date 2021-01-26
 */
@Data
public class RspRechargeBankReport {

    @Excel( name = "银行名称", orderNum = "1" )
    private String bankName;

    @Excel( name = "收款人", orderNum = "2" )
    private String bankUserName;

    @Excel( name = "充值金额", orderNum = "3" )
    private BigDecimal rechargeMoney;

    @JsonFormat( pattern = "yyyy-MM-dd" )
    @Excel( name = "时间", exportFormat = "yyyy-MM-dd", orderNum = "6" )
    private String updateTime;

    @Excel( name = "提现银行账号", orderNum = "5" )
    private String bankAccount;

    @Excel( name = "次数", orderNum = "4" )
    private Integer time;
}
