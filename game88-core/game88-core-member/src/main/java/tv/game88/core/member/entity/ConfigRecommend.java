package tv.game88.core.member.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConfigRecommend {
    private String     id;
    private Integer    level;
    private String     name;
    private BigDecimal bill;
}