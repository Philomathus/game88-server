package tv.game88.core.member.dto;

import lombok.Data;

@Data
public class ReqSmallFeatures {
    private String phones;
    private String password;
    private String userIds;
    private String phonesByIds;
    private String memberIds;
    private String money;
    private Integer googleAuthCode;
}

