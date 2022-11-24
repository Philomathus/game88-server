package tv.game88.game.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对象 log_clean_code_info
 *
 * @author MengJun
 */
@TableName( "log_clean_code_info" )
@Data
@EqualsAndHashCode( callSuper = false )
public class LogCleanCodeInfo {

    /**
     * 游戏局号
     */
    @TableId( type = IdType.INPUT )
    private String id;

    /**
     * 会员ID
     */
    @Excel( name = "会员ID" )
    private String memberId;

    /**
     * 洗码ID
     */
    @Excel( name = "洗码ID" )
    private String cleanId;

    /**
     * 洗码量
     */
    @Excel( name = "洗码量" )
    private BigDecimal codeAmount;

    /**
     * 洗码比例
     */
    @Excel( name = "洗码比例" )
    private BigDecimal rateClean;

    /**
     * 洗码金额
     */
    @Excel( name = "洗码金额" )
    private BigDecimal cleanAmount;

    /**
     * 游戏代理
     */
    @Excel( name = "游戏代理" )
    private String agent;

    /**
     * 游戏
     */
    @Excel( name = "游戏" )
    private String name;

    /**
     * 洗码时间
     */
    @Excel( name = "洗码时间" )
    private LocalDateTime cleanTime;
}