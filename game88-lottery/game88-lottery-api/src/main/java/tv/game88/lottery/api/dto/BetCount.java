package tv.game88.lottery.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BetCount {
    private String     betinfo;
    private BigDecimal totalbet;
}
