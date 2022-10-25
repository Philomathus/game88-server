package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.AppVersionUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.enums.EnumDev;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.platform.api.dto.RspInit;
import tv.game88.platform.api.dto.RspManUpdateVersion;
import tv.game88.platform.api.service.MemberInfoService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service( "memberInfoService" )
public class MemberInfoServiceImpl extends ServiceImpl<MemberInfoMapper, MemberInfo> implements MemberInfoService {
    @Resource
    private RedisUtils         redisUtils;
    @Resource
    private ConfigEnvCacheUtil configEnvCacheUtil;

    @Override
    public RspInit getLoginInit( Integer dev, String version ) {
        RspInit res = new RspInit();
        List<Object> keys = new ArrayList<>( Arrays.asList( "customer_url", "customer_url2", "web_url", "163action_captchaId",
                "163action_Product_id", "star_pic" ) );

        if ( dev == EnumDev.IOS.getType() ) {
            keys.addAll( Arrays.asList( "ios_version", "ios_force_update", "ios_down_url", "ios_update_text" ) );
        } else {
            keys.addAll( Arrays.asList( "android_version", "android_force_update", "android_down_url", "android_update_text" ) );
        }
        List<String> valueList = configEnvCacheUtil.getConf( keys );
        res.setCustomerUrl( valueList.get( 0 ) );
        res.setCustomerUrl2( valueList.get( 1 ) );
        res.setWebUrl( valueList.get( 2 ) );
        res.setCaptchaId( valueList.get( 3 ) );
        res.setProductId( valueList.get( 4 ) );
        String starPic = valueList.get( 5 );
        res.setStarPic( starPic == null ? "" : starPic );

        String latestVersion = valueList.get( 6 );
        res.setLatestVersion( latestVersion );
        res.setHasNew( AppVersionUtils.hasNewVersion( version, latestVersion ) );

        res.setLatestFore( valueList.get( 7 ) );
        res.setDownUrl( valueList.get( 8 ) );
        res.setUpdateText( valueList.get( 9 ) );
        return res;
    }

    @Override
    public RspManUpdateVersion checkManUpdateVersion( Integer dev, String version ) {
        RspManUpdateVersion rsp  = new RspManUpdateVersion();
        List<Object>        keys = new ArrayList<>( 3 );
        if ( dev == EnumDev.IOS.getType() ) {
            keys.addAll( Arrays.asList( "ios_man_version", "ios_update_content", "ios_man_version_url" ) );
        } else {
            keys.addAll( Arrays.asList( "android_man_version", "android_update_content", "android_man_version_url" ) );
        }
        List<String> valueList = configEnvCacheUtil.getConf( keys );

        String manVersion = valueList.get( 0 );
        rsp.setManVersion( StringUtils.isBlank( manVersion ) ? "3.8.11.1" : manVersion );
        //更新内容
        rsp.setUpdateContent( valueList.get( 1 ) );
        if ( AppVersionUtils.hasNewVersion( version, rsp.getManVersion() ) ) {
            rsp.setDownUrl( valueList.get( 2 ) );
        }
        return rsp;
    }
}
