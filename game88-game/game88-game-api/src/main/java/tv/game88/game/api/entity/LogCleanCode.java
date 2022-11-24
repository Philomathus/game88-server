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
 * 会员洗码记录对象 log_clean_code
 *
 * @author MengJun
 */
@TableName( "log_clean_code" )
@Data
@EqualsAndHashCode( callSuper = false )
public class LogCleanCode {

    /**
     * 游戏局号
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 账号
     */
    @Excel( name = "账号" )
    private String memberId;

    /**
     * 洗码量
     */
    @Excel( name = "洗码量" )
    private BigDecimal codeAmount;

    /**
     * 洗码金额
     */
    @Excel( name = "洗码金额" )
    private BigDecimal cleanAmount;

    /**
     * 洗码时间
     */
    @Excel( name = "洗码时间" )
    private LocalDateTime cleanTime;
}