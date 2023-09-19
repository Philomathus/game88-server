package tv.game88.general.api.service.impl;

import com.baomidou.dynamic.datasource.annotation.Master;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.api.mapper.GamePlatformMapper;
import tv.game88.general.api.service.GamePlatformService;

import java.util.List;

/**
 * 游戏平台Service业务层处理
 *
 * @author MengJun
 */
@Log4j2
@Service
@Master
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
            platform.setDes( "******" );
            platform.setMd5( "******" );
        }
        return gamePlatforms;
    }
}