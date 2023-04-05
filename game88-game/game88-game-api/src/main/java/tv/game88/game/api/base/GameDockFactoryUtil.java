package tv.game88.game.api.base;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import tv.game88.game.api.type.EnumGameCategory;

import javax.annotation.Resource;

@Component
public class GameDockFactoryUtil {
    @Resource
    private ApplicationContext context;

    public BaseGameDock createGameDockProcessor( EnumGameCategory enumGameCategory ) {
        return switch ( enumGameCategory ) {
            case UPG, MG -> ( BaseGameDock ) context.getBean( EnumGameCategory.MG.getType() + "GameProcessor" );
            case KAIXUAN, KAIXUAN_X, KAIYUAN, LEYOU -> ( BaseGameDock ) context.getBean(
                    EnumGameCategory.KAIYUAN.getType() + "GameProcessor" );
            default -> ( BaseGameDock ) context.getBean( enumGameCategory.getType() + "GameProcessor" );
        };
    }
}
