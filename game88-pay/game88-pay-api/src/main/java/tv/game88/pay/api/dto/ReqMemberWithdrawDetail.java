package tv.game88.pay.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReqMemberWithdrawDetail {

    private String[]     downLoadDate = new String[ 2 ];
    private String       id;
    private List<String> ids;
    private String       memberId;
    private String       remark;
    private String       searchCardBlack;
    private String       cardBlack;
    private Long         payAgentChannelId;
    private Integer      status;
    private String       opName;
    private String       searchValue;

    private String   priceMin;
    private String   priceMax;
    private String[] selectDate;
    private String   selectStartDate;
    private String   selectEndDate;

    private String[] searchTime;
}
