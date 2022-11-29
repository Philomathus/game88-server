package tv.game88.core.member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
    * 会员VIP等级配置
    */
@Data
@NoArgsConstructor
public class ConfigVip {
    /**
    * vip等级
    */
    @TableId(type = IdType.INPUT)
    private Integer level;

    /**
    * 晋级彩金
    */
    private BigDecimal levelBonus;

    /**
    * 周俸禄
    */
    private BigDecimal weekBonus;

    /**
    * 月俸禄
    */
    //private BigDecimal monthBonus;

    /**
    * 通道加速(1是0否)
    */
    private Boolean channel;

    /**
    * 专属客服(1是0否)
    */
    private Boolean client;

    /**
    * 需充值量
    */
    private BigDecimal bcode;
}