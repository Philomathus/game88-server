package tv.game88.platform.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class RspImToken {
    private String       token;
    private List<String> imHostlist;
}
