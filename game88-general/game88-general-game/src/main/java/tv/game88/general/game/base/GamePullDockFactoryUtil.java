package tv.game88.general.game.base;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import tv.game88.core.game.type.EnumGameCategory;

import jakarta.annotation.Resource;

@Component
public class GamePullDockFactoryUtil {
    @Resource
    private ApplicationContext context;

    public BaseGamePull createGamePullProcessor( EnumGameCategory enumGameCategory ) {
        return switch ( enumGameCategory ) {
            case UPG, MG -> ( BaseGamePull ) context.getBean( EnumGameCategory.MG.getType() + "GamePullProcessor" );
            case KAIXUAN_X, KAIYUAN, LEYOU -> ( BaseGamePull ) context.getBean(
                    EnumGameCategory.KAIYUAN.getType() + "GamePullProcessor" );
            default -> ( BaseGamePull ) context.getBean( enumGameCategory.getType() + "GamePullProcessor" );
        };
    }
}
