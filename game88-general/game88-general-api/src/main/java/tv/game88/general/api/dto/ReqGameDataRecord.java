package tv.game88.general.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class ReqGameDataRecord {
    private String       agent;
    private String       account;
    private List<String> platformIds;
    private String       startTime;
    private String       endTime;
}
