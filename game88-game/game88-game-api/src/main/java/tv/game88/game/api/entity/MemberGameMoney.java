package tv.game88.game.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class MemberGameMoney {
    /**
     * 订单ID
     */
    @TableId( type = IdType.INPUT )
    private String        orderId;
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
    // 0=上分开始1=上分失败2=上分成功3下分失败4下分成功
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


    @TableField(exist = false)
    private Map<String,Object> params = new HashMap<>();

    public Map<String, Object> getParams(){
        if(params ==null){
            return new HashMap<>();
        }
        return params;
    }
}