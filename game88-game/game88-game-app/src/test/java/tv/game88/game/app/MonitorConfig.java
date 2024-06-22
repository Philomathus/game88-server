package tv.game88.game.app;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class MonitorConfig {

    private Monitor      monitor;
    private List<Param>  params;
    private List<String> metrics;
    private boolean      detected;

    @Builder
    @Data
    public static class Param {
        private String field;
        private int    type;
        private String value;
    }

    @Builder
    @Data
    public static class Monitor {
        private String        name;
        private String        app;
        private String        host;
        private String        collector;
        private int           intervals;
        private int           status;
        private List<Integer> tags;
    }
}