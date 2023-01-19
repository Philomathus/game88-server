package tv.game88.game.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RspPGSoftGameData {
    Map<String, String> data;
    Map<String, String> error;
}
