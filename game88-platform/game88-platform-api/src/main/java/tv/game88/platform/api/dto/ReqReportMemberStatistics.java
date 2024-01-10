package tv.game88.platform.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class ReqReportMemberStatistics {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String inclusive_date;
    private String channelCode;
}
