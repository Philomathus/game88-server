package tv.game88.core.member.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 会员推广记录表对象 member_recommend
 *
 * @author mengJun
 */
@Data
public class MemberRecommend implements Serializable {
    private String     id;
    @Excel( name = "充值人ID" )
    private String     memberId;
    @Excel( name = "邀请码" )
    private String     code;
    @Excel( name = "推广人ID" )
    private String     inviterId;
    @Excel( name = "佣金" )
    private BigDecimal commission;
    @Excel( name = "状态" )
    private Integer    status;
    @Excel( name = "订单金额" )
    private BigDecimal orderMoney;
    @Excel( name = "推广等级" )
    private Integer    level;
    @Excel( name = "充值人账号" )
    private String     memberName;
    @Excel( name = "推广人账号" )
    private String     inviter;
    @Excel( name = "创建时间" )
    private Date       createTime;

    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
                .append( "id", getId() )
                .append( "memberId", getMemberId() )
                .append( "code", getCode() )
                .append( "inviterId", getInviterId() )
                .append( "commission", getCommission() )
                .append( "createTime", getCreateTime() )
                .append( "status", getStatus() )
                .append( "orderMoney", getOrderMoney() )
                .append( "level", getLevel() )
                .append( "memberName", getMemberName() )
                .append( "inviter", getInviter() )
                .toString();
    }
}