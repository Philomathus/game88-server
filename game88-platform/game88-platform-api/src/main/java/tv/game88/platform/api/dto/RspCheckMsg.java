package tv.game88.platform.api.dto;

import lombok.Data;

@Data
public class RspCheckMsg {
    private String  msg;
    private Boolean status = false;
    private Boolean flag   = true;
}
