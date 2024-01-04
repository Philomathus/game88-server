package tv.game88.general.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.general.api.entity.GamePlatform;

import java.util.List;

/**
 * 游戏平台Mapper接口
 *
 * @author MengJun
 */
public interface GamePlatformMapper extends BaseMapper<GamePlatform> {

    /**
     * 查询游戏平台列表
     *
     * @param gamePlatform 游戏平台
     *
     * @return 游戏平台集合
     */
    public List<GamePlatform> selectGamePlatformList( GamePlatform gamePlatform );

    List<GamePlatform> selectGamePlatformAndVersionList();
}