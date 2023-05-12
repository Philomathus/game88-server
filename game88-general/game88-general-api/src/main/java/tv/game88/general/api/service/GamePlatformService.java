package tv.game88.general.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.general.api.entity.GamePlatform;

import java.util.List;

/**
 * 游戏平台Service接口
 *
 * @author MengJun
 */
public interface GamePlatformService extends IService<GamePlatform> {
    /**
     * 查询游戏平台列表
     *
     * @param gamePlatform 游戏平台
     *
     * @return 游戏平台集合
     */
    public List<GamePlatform> selectGamePlatformList( GamePlatform gamePlatform );
}