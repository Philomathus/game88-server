package tv.game88.core.game.type;

import lombok.Getter;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.core.game.dto.RspGameCategory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum EnumGameCategory {
    //
    LOTTERY( "lottery", "彩票游戏" ),
    KAIYUAN( ConstantsGame.KAI_YUAN, "开元棋牌" ),
    OG( ConstantsGame.OG, "OG电子" ),
    AG( ConstantsGame.AG, "AG电子" ),
    MG( ConstantsGame.MG, "MG电子" ),
    UPG( ConstantsGame.UPG, "UPG电子" ),
    MEITIAN( ConstantsGame.MEITIAN, "美天棋牌" ),
    KAIXUAN( ConstantsGame.KAIXUAN, "凯旋棋牌" ),
    KAIXUAN_X( ConstantsGame.KAIXUAN_X, "凯旋棋牌X" ),
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
    AT( ConstantsGame.AT, "AT电子" ),
    RICH88( ConstantsGame.RICH88, "Rich88电子" ),
    PG_SOFT( ConstantsGame.PG_SOFT, "PG电子" ),
    ;

    private final String type;
    private final String des;

    EnumGameCategory( String type, String des ) {
        this.type = type;
        this.des  = des;
    }

    public static List<RspGameCategory> getGameCategorys() {
        return Arrays.stream( EnumGameCategory.values() ).map( m -> {
            RspGameCategory gameCategory = new RspGameCategory();
            gameCategory.setName( m.name() );
            gameCategory.setDes( m.getDes() );
            return gameCategory;
        } ).collect( Collectors.toList() );
    }

    public static EnumGameCategory getGameCategoryByType( String type ) {
        for ( EnumGameCategory value : EnumGameCategory.values() ) {
            if ( value.getType().equals( type ) ) {
                return value;
            }
        }
        return null;
    }

    public static List<Integer> getDataRemoteByEnum( EnumGameCategory enumGameCategory ) {
        return switch ( enumGameCategory ) {
            case LOTTERY -> List.of();
            case OG -> List.of( 2 );
            case AG -> List.of( 5 );
            case MG -> List.of( 6 );
            case UPG -> List.of( 7 );
            case MEITIAN -> List.of( 14 );
            case KAIXUAN -> List.of( 15 );
            case NEWWORLD -> List.of( 17 );
            case BBIN -> List.of( 8, 9, 10, 11 );
            case KAIYUAN -> List.of( 50 );
            case KAIXUAN_X -> List.of( 51 );
            case LEYOU -> List.of( 52 );
            case GAMING_365 -> List.of( 53 );
            case BOLE -> List.of( 54 );
            case BAISHENG -> List.of( 55 );
            case BG -> List.of( 56 );
            case MEIBO -> List.of( 57 );
            case JDB -> List.of( 64 );
            case CQ9 -> List.of( 65 );
            case AT -> List.of( 66 );
            case FG -> List.of( 34 );
            case WALI -> List.of( 32 );
            case SGWIN -> List.of( 33 );
            case PG_SOFT -> List.of( 35 );
            case RICH88 -> List.of( 36 );
            case DATANG -> List.of( 27 );
            case HG -> List.of( 28 );
            case XINGYUN -> List.of( 29 );
        };
    }

    public static EnumGameCategory getEnumByDataRemote( int platformId ) {
        return switch ( platformId ) {
            case 2 -> OG;
            case 5 -> AG;
            case 6 -> MG;
            case 7 -> UPG;
            case 14 -> MEITIAN;
            case 15 -> KAIXUAN;
            case 17 -> NEWWORLD;
            case 8, 9, 10, 11 -> BBIN;
            case 50 -> KAIYUAN;
            case 51 -> KAIXUAN_X;
            case 52 -> LEYOU;
            case 53 -> GAMING_365;
            case 54 -> BOLE;
            case 55 -> BAISHENG;
            case 56 -> BG;
            case 57 -> MEIBO;
            case 64 -> JDB;
            case 65 -> CQ9;
            case 34 -> FG;
            case 32 -> WALI;
            case 33 -> SGWIN;
            case 35 -> PG_SOFT;
            case 36 -> RICH88;
            case 27 -> DATANG;
            case 28 -> HG;
            case 29 -> XINGYUN;
            default -> null;
        };
    }
}
