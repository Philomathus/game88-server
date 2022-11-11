package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.game.api.entity.GamePlatform;
import tv.game88.game.api.mapper.GamePlatformMapper;
import tv.game88.game.api.service.GamePlatformService;

import java.util.List;

/**
 * 游戏平台Service业务层处理
 *
 * @author MengJun
 */
@Service
public class GamePlatformServiceImpl extends ServiceImpl<GamePlatformMapper, GamePlatform> implements GamePlatformService {
    /**
     * 查询游戏平台列表
     *
     * @param gamePlatform 游戏平台
     *
     * @return 游戏平台
     */
    @Override
    public List<GamePlatform> selectGamePlatformList( GamePlatform gamePlatform ) {
        List<GamePlatform> gamePlatforms = this.baseMapper.selectGamePlatformList( gamePlatform );
        for ( GamePlatform platform : gamePlatforms ) {
            platform.setDes( null );
            platform.setMd5( null );
        }
        return gamePlatforms;
    }
}