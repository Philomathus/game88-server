package tv.game88.game.api.base;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import tv.game88.game.api.type.EnumGameCategory;

import javax.annotation.Resource;

@Component
public class GameButtFactoryUtil {
    @Resource
    private ApplicationContext context;

    public BaseGameButt createGameButtProcessor( EnumGameCategory enumGameCategory ) {
        return switch ( enumGameCategory ) {
            case UPG, MG -> ( BaseGameButt ) context.getBean( EnumGameCategory.MG.getType() + "GameProcessor" );
            case KAIXUAN, KAIXUAN_X, KAIYUAN, LEYOU -> ( BaseGameButt ) context.getBean(
                    EnumGameCategory.KAIYUAN.getType() + "GameProcessor" );
            default -> ( BaseGameButt ) context.getBean( enumGameCategory.getType() + "GameProcessor" );
        };
    }
}
