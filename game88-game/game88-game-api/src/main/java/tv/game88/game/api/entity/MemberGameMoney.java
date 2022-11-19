package tv.game88.game.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberGameMoney {
    /**
     * 主键
     */
    @TableId( type = IdType.INPUT )
    private String        id;
    /**
     * 玩家ID
     */
    private String        memberId;
    /**
     * 游戏平台ID
     */
    private Long          platformId;
    /**
     * 状态
     */
    // -1=上分失败0=上分开始1=上分成功
    private Integer       status;
    /**
     * 上分金额
     */
    private BigDecimal    money;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 创建时间
     */
    private LocalDateTime updateTime;
    /**
     * 订单ID
     */
    private String        orderId;

}