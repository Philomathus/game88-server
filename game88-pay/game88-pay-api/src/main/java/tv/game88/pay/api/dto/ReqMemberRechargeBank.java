package tv.game88.pay.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ReqMemberRechargeBank {
    private String[] downLoadDate = new String[ 2 ];
    private String   id;
    private String   remark;

    private Integer status;
    private String  searchValue;

    private String bankUserName;
    private String rechargeUserName;
    private String opName;
    private String bankName;

    private String[] selectDate;
    private String   selectStartDate;

    private String selectEndDate;
    private String updateTime;

    @JsonIgnore
    private String priceMin;
    @JsonIgnore
    private String priceMax;
}
