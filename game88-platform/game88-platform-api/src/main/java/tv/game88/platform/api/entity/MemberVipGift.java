package tv.game88.platform.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;

/**
 * 对象 member_vip_gift
 *
 * @author MengJun
 */
@TableName( "member_vip_gift" )
@Data
@EqualsAndHashCode( callSuper = false )
public class MemberVipGift {
    /**
     * 会员ID
     */
    @TableId( type = IdType.INPUT )
    private String memberId;

    /**
     * 晋级彩金领取vip
     */
    @Excel( name = "晋级彩金领取vip" )
    private Integer levelBonusVip;

    /**
     * 周俸禄领取时间
     */
    @Excel( name = "周俸禄领取时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime weekBonusTime;

    /**
     * 月俸禄领取时间
     */
    @Excel( name = "月俸禄领取时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime monthBonusTime;

    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
                .append( "memberId", getMemberId() )
                .append( "levelBonusVip", getLevelBonusVip() )
                .append( "weekBonusTime", getWeekBonusTime() )
                .append( "monthBonusTime", getMonthBonusTime() )
                .toString();
    }
}