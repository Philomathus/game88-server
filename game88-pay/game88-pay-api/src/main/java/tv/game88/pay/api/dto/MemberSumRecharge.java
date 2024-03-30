package tv.game88.pay.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberSumRecharge {
    private String     memberId;
    private BigDecimal money;
}
