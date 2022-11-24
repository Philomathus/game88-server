package tv.game88.game.api.service;

import tv.game88.game.api.type.EnumGameCategory;

public interface GameDataService {
    void beatGameCodeAgent( String dTime, String start, String end, String account, EnumGameCategory gameCategory );
}
