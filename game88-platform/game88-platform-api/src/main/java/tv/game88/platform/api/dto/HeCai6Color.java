package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class HeCai6Color {
    @Schema( title = "红" )
    List<String> reds  = Arrays.asList( "01", "02", "07", "08", "12", "13", "18", "19", "23", "24", "29", "30", "34", "35", "40"
            , "45", "46" );
    @Schema( title = "蓝" )
    List<String> blue  = Arrays.asList( "03", "04", "09", "10", "14", "15", "20", "25", "26", "31", "36", "37", "41", "42", "47"
            , "48" );
    @Schema( title = "绿" )
    List<String> green = Arrays.asList( "05", "06", "11", "16", "17", "21", "22", "27", "28", "32", "33", "38", "39", "43", "44"
            , "49" );
}
