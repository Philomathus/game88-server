package tv.game88.core.game.type;

import lombok.Getter;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.core.game.dto.RspGameCategory;

import java.util.Arrays;
import java.util.List;

@Getter
public enum EnumGameCategory {
    //
    LOTTERY( "lottery", "彩票游戏" ),
    KAIYUAN( ConstantsGame.KAI_YUAN, "开元棋牌" ),
    OG( ConstantsGame.OG, "OG电子" ),
    OG_NEW( ConstantsGame.OG_NEW, "新OG电子" ),
    AG( ConstantsGame.AG, "AG电子" ),
    MG( ConstantsGame.MG, "MG电子" ),
    UPG( ConstantsGame.UPG, "UPG电子" ),
    MEITIAN( ConstantsGame.MEITIAN, "美天棋牌" ),
    KAIXUAN_X( ConstantsGame.KAIXUAN_X, "凯旋棋牌" ),
    NEWWORLD( ConstantsGame.NEWWORLD, "新世界棋牌" ),
    BBIN( ConstantsGame.BBIN, "BBIN电子" ),
    LEYOU( ConstantsGame.LE_YOU, "乐游棋牌" ),
    GAMING_365( ConstantsGame.GAMING_365, "365棋牌" ),
    BOLE( ConstantsGame.BOLE, "博乐棋牌" ),
    BAISHENG( ConstantsGame.BAISHENG, "百胜棋牌" ),
    BG( ConstantsGame.BG, "BG棋牌" ),
    MEIBO( ConstantsGame.MEIBO, "美博棋牌" ),
    DATANG( ConstantsGame.DATANG, "大唐棋牌" ),
    HG( ConstantsGame.HG, "欢乐棋牌" ),
    XINGYUN( ConstantsGame.XINGYUN, "幸运棋牌" ),
    WALI( ConstantsGame.WALI, "瓦力棋牌" ),
    SGWIN( ConstantsGame.SGWIN, "双赢棋牌" ),
    FG( ConstantsGame.FG, "FG棋牌" ),
    JDB( ConstantsGame.JDB, "JDB电子" ),
    CQ9( ConstantsGame.CQ9, "CQ9电子" ),
    RICH88( ConstantsGame.RICH88, "Rich88电子" ),
    PG_SOFT( ConstantsGame.PG_SOFT, "PG电子" ),
    SHABA( ConstantsGame.SHABA, "沙巴体育" ),

    T1( ConstantsGame.T1, "T1" ),
    PP( ConstantsGame.PP, "pp" ),
    CG( ConstantsGame.CG, "cg" ),
    JILI( ConstantsGame.JILI, "jiLi" ),
    PG_NEW( ConstantsGame.PG_NEW, "PGNew" ),
    ;

    private final String type;
    private final String des;

    EnumGameCategory( String type, String des ) {
        this.type = type;
        this.des  = des;
    }

    public static List<RspGameCategory> getGameCategorys() {
        return Arrays.stream( EnumGameCategory.values() ).filter( m -> !Arrays.asList( T1, PP, CG, JILI, PG_NEW ).contains( m ) )
                .map( m -> {
                    RspGameCategory gameCategory = new RspGameCategory();
                    gameCategory.setName( m.name() );
                    gameCategory.setDes( m.getDes() );
                    return gameCategory;
                } ).toList();
    }

    public static EnumGameCategory getGameCategoryByType( String type ) {
        for ( EnumGameCategory value : EnumGameCategory.values() ) {
            if ( value.getType().equals( type ) ) {
                return value;
            }
        }
        return null;
    }

    public static List<RspGameCategory> getGameCategoryAll() {
        return Arrays.stream( EnumGameCategory.values() ).map( m -> {
            RspGameCategory gameCategory = new RspGameCategory();
            gameCategory.setName( m.name() );
            gameCategory.setDes( m.getDes() );
            return gameCategory;
        } ).toList();
    }
}
