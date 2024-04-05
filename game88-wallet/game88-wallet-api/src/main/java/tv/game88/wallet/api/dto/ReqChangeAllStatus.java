package tv.game88.wallet.api.dto;

import lombok.Data;
@Data
public class ReqChangeAllStatus {

    private String [] memberIds;
    private Integer   status;
    private Integer   googleAuthCode;
    private String    remarks;

}
