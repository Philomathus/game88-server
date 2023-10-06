package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *   充值日志对象 Recharge_log
 *
 * @author Rajesh
 * @date 2023-05-20
 */

@Data
public class RechargeLog {
    /**
     * 会员ID memberId
     */
    @Excel( name = "会员ID" )
    private String memberId;

    /**
     * 会员ip member ip
     */
    @Excel( name = "会员ip" )
    private String ip;

    /**
     * 会员vip member vip
     */
    @Excel( name = "会员vip" )
    private String vip;

    /**
     * 会员卡 member card
     */
    @Excel( name = "会员卡" )
    private String card;

    /**
     * 创建日期 created time
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建日期" ,width = 30,
            exportFormat = "yyyy-MM-dd HH:mm:ss",
            importFormat = "yyyy-MM-dd HH:mm:ss",
            databaseFormat = "yyyy-MM-dd HH:mm:ss")
    private String createTime;

    @JsonIgnore
    private String[] selectDate;
    @JsonIgnore
    private String   selectStartDate;
    @JsonIgnore
    private String   selectEndDate;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("memberId", getMemberId())
                .append("ip", getIp())
                .append("card", getCard())
                .append("createTime", getCreateTime())
                .toString();
    }

}
