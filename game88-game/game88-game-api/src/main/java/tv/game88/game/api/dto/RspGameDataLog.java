package tv.game88.game.api.dto;

import lombok.Data;

@Data
public class RspGameDataLog {
    private String  id;
    private String  game_id;
    private String  game_round;
    private String  account;
    private String  server_id;
    private String  kind_id;
    private String  table_id;
    private String  chair_id;
    private String  cell_score;
    private String  all_bet;
    private String  profit;
    private String  revenue;
    private String  game_start_time;
    private String  game_end_time;
    private Integer platform_id;
    private String  agent;
    private String  platform_type;
    private String  cx_agent;
}