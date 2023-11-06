package tv.game88.core.config.cache;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.config.entity.ConfigOss;
import tv.game88.core.config.mapper.ConfigOssMapper;

import jakarta.annotation.Resource;

/**
 * @author mengJun
 */
@Log4j2
@Component
public class ConfigOssCacheUtil {
	public static final String CONFIG_OSS_EFFECT = Constants.CONFIG_PREX + "oss:effect";

	@Resource
	private RedisUtils      redisUtil;
	@Resource
	private ConfigOssMapper configOssMapper;

	public void setEffect( ConfigOss configOss ) {
		redisUtil.unlink( CONFIG_OSS_EFFECT );
		redisUtil.strSet( CONFIG_OSS_EFFECT, JsonUtil.object2Json( configOss ) );
	}

	public ConfigOss getEffect() {
		this.exists();
		String value = redisUtil.strGet( CONFIG_OSS_EFFECT );
		return value == null ? null : JsonUtil.json2Object( value, ConfigOss.class );
	}

	private void exists() {
		if ( !redisUtil.exists( CONFIG_OSS_EFFECT ) ) {
			ConfigOss configOss = configOssMapper.selectConfigOssByEffect();
			if ( configOss != null ) {
				this.setEffect( configOss );
			}
		}
	}

	public void clear(){
		redisUtil.unlink( CONFIG_OSS_EFFECT );
	}
}
