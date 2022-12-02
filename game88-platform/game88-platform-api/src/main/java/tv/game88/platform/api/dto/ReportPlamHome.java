package tv.game88.platform.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReportPlamHome {
    private String repId;

    private String classOne;

    private String classOnename;

    private String classTwo;

    private String classTwoname;

    private String type;

    private BigDecimal tvalue;

    private String reporttime;

    private String reporhour;

}
