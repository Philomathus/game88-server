package tv.game88.game.api.type;

import lombok.Getter;
import tv.game88.game.api.constants.ConstantsGame;
import tv.game88.game.api.dto.RspGameCategory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum EnumGameCategory {
    LOTTERY( "lottery", "自研彩票" ),
    KAIYUAN( ConstantsGame.KAI_YUAN, "开元棋牌" ),
    OG( ConstantsGame.OG, "OG电子" ),
    AG( ConstantsGame.AG, "AG电子" ),
    MG( ConstantsGame.MG, "MG电子" ),
    UPG( ConstantsGame.UPG, "UPG电子" ),
    SHABA( ConstantsGame.SHABA, "沙巴电子" ),
    ICG( ConstantsGame.ICG, "ICG电子" ),
    MEITIAN( ConstantsGame.MEITIAN, "美天棋牌" ),
    KAIXUAN( ConstantsGame.KAIXUAN, "凯旋棋牌" ),
    NEWWORLD( ConstantsGame.NEWWORLD, "新世界棋牌" ),
    BBIN( ConstantsGame.BBIN, "BBIN电子" ),
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
            case KAIYUAN -> List.of( 50 );
            case OG -> List.of( 2 );
            case AG -> List.of( 5 );
            case MG -> List.of( 6 );
            case UPG -> List.of( 7 );
            case SHABA -> List.of( 12 );
            case ICG -> List.of( 13 );
            case MEITIAN -> List.of( 14 );
            case KAIXUAN -> List.of( 51 );
            case NEWWORLD -> List.of( 17 );
            case BBIN -> List.of( 8, 9, 10, 11 );
        };
    }
}
