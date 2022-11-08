package tv.game88.platform.api.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ActivityQuestInfo {
    /**
    * 系统编号
    */
    private Integer id;

    /**
    * 图标
    */
    private String icon;

    /**
    * 标题
    */
    private String title;

    /**
    * 任务类型id
    */
    private Integer typeId;

    /**
    * 目标任务量
    */
    private Integer target;

    /**
    * 完成后增加的资金
    */
    private BigDecimal reward;

    /**
    * 任务详情
    */
    private String detail;

    /**
    * 描述
    */
    private String content;

    /**
    * 所属游戏id
    */
    private Integer gameId;

    /**
    * 平台游戏类型
    */
    private String kindId;

    /**
    * 平台类型
    */
    private Integer platformId;

    /**
    * 任务模式（0一次性 1每日任务）默认为0
    */
    private Integer taskMode;

    /**
    * 是否激活 0停用 1激活
    */
    private Boolean effect;

    /**
    * 排序号
    */
    private Integer sort;
}