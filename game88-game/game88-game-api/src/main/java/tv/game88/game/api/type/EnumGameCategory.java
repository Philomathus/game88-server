package tv.game88.game.api.type;

import lombok.Getter;
import tv.game88.game.api.dto.RspGameCategory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum EnumGameCategory {
    LOTTERY( "自研彩票" ),
    KAIYUAN( "开元棋牌" ),
    OG( "OG电子" ),
    AG( "AG电子" ),
    MG( "MG电子" ),
    UPG( "UPG电子" ),
    BBIN("BBIN电子"),
    SHABA("沙巴体育"),
    ICG("ICG电子"),
    MEITIAN("美天棋牌"),
    KAIXUAN("凯旋棋牌"),
    NEWWORLD("新世界棋牌"),
    ;

    private final String des;

    EnumGameCategory( String des ) {
        this.des = des;
    }

    public static List<RspGameCategory> getGameCategorys() {
        return Arrays.stream( EnumGameCategory.values() ).map( m -> {
            RspGameCategory gameCategory = new RspGameCategory();
            gameCategory.setName( m.name() );
            gameCategory.setDes( m.getDes() );
            return gameCategory;
        } ).collect( Collectors.toList() );
    }
}
