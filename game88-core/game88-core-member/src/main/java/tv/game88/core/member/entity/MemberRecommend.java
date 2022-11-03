package tv.game88.core.member.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员推广记录表对象 member_recommend
 *
 * @author mengJun
 */
@Data
public class MemberRecommend implements Serializable {
    private String        id;
    @Excel( name = "充值人ID" )
    private String        memberId;
    @Excel( name = "邀请码" )
    private String        code;
    @Excel( name = "推广人ID" )
    private String        inviterId;
    @Excel( name = "佣金" )
    private BigDecimal    commission;
    @Excel( name = "状态" )
    private Integer       status;
    @Excel( name = "订单金额" )
    private BigDecimal    orderMoney;
    @Excel( name = "推广等级" )
    private Integer       level;
    @Excel( name = "充值人账号" )
    private String        memberName;
    @Excel( name = "推广人账号" )
    private String        inviter;
    @Excel( name = "创建时间" )
    private LocalDateTime createTime;
}