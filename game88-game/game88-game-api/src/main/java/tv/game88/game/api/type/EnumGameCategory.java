package tv.game88.game.api.type;

import lombok.Getter;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.RspGameCategory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum EnumGameCategory {
    LOTTERY( "lottery", "彩票游戏" ),
    KAIYUAN( ConstantsGame.KAI_YUAN, "开元棋牌" ),
    OG( ConstantsGame.OG, "OG电子" ),
    AG( ConstantsGame.AG, "AG电子" ),
    MG( ConstantsGame.MG, "MG电子" ),
    UPG( ConstantsGame.UPG, "UPG电子" ),
    SHABA( ConstantsGame.SHABA, "沙巴电子" ),
    ICG( ConstantsGame.ICG, "ICG电子" ),
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
            case SHABA -> List.of( 12 );
            case ICG -> List.of( 13 );
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
        };
    }

    public static EnumGameCategory getEnumByDataRemote( int platformId ) {
        return switch ( platformId ) {
            case 2 -> OG;
            case 5 -> AG;
            case 6 -> MG;
            case 7 -> UPG;
            case 12 -> SHABA;
            case 13 -> ICG;
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
            default -> null;
        };
    }
}
