package tv.game88.admin.system.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import tv.game88.admin.system.entity.SysDictData;
import tv.game88.admin.system.mapper.SysDictDataMapper;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.StringUtils;
import lombok.extern.log4j.Log4j2;
import tv.game88.core.admin.constant.AdminConstants;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 字典工具类
 *
 * @author MengJun
 */
@Log4j2
@Component
public class DictUtils {

	/**
	 * 分隔符
	 */
	public static final String SEPARATOR = ",";

	@Resource
	private SysDictDataMapper   sysDictDataMapper;
	@Resource
	private StringRedisTemplate stringRedisTemplate;
	@Resource
	private RedisUtils redisUtil;

	/**
	 * 设置字典缓存
	 *
	 * @param key       参数键
	 * @param dictDatas 字典数据列表
	 */
	public void setDictCache( String key, List<SysDictData> dictDatas ) {
		redisUtil.strSet( getCacheKey( key ), JsonUtil.object2Json( dictDatas ) );
	}

	/**
	 * 根据字典类型和字典标签获取字典值
	 *
	 * @param dictType  字典类型
	 * @param dictLabel 字典标签
	 * @return 字典值
	 */
	public String getDictValue( String dictType, String dictLabel ) {
		return getDictValue( dictType, dictLabel, SEPARATOR );
	}

	/**
	 * 清空字典缓存
	 */
	@Async
	public void clearDictCache() {
		Set<String> keySet = stringRedisTemplate.execute( ( RedisCallback<Set<String>> ) connection -> {
			Set<String> binaryKeys = new HashSet<>();
			Cursor<byte[]> cursor = connection.scan( ScanOptions.scanOptions()
					.match(AdminConstants.SYS_DICT_KEY + "*").count(100).build());
			while (cursor.hasNext()) {
				binaryKeys.add(new String(cursor.next()));
			}
			return binaryKeys;
		} );
		redisUtil.unlink( keySet );
	}

	/**
	 * 设置cache key
	 *
	 * @param configKey 参数键
	 * @return 缓存键key
	 */
	public String getCacheKey( String configKey ) {
		return AdminConstants.SYS_DICT_KEY + configKey;
	}

	/**
	 * 获取字典缓存
	 *
	 * @param key 参数键
	 * @return dictDatas 字典数据列表
	 */
	public List<SysDictData> getDictCache( String key ) {
		if ( !redisUtil.exists( getCacheKey( key ) ) ) {
			List<SysDictData> dictDataList = sysDictDataMapper.selectDictDataByType( key );
			if ( !CollectionUtils.isEmpty( dictDataList ) ) {
				setDictCache( key, dictDataList );
			}
		}
		String cacheObj = redisUtil.strGet( getCacheKey( key ) );
		if ( StringUtils.isNotNull( cacheObj ) ) {
			return JsonUtil.json2Array( cacheObj, new TypeReference<List<SysDictData>>() {} );
		}
		return null;
	}

	/**
	 * 根据字典类型和字典值获取字典标签
	 *
	 * @param dictType  字典类型
	 * @param dictValue 字典值
	 * @return 字典标签
	 */
	public String getDictLabel( String dictType, String dictValue ) {
		return getDictLabel( dictType, dictValue, SEPARATOR );
	}

	/**
	 * 根据字典类型和字典值获取字典标签
	 *
	 * @param dictType  字典类型
	 * @param dictValue 字典值
	 * @param separator 分隔符
	 * @return 字典标签
	 */
	public String getDictLabel( String dictType, String dictValue, String separator ) {
		StringBuilder     propertyString = new StringBuilder();
		List<SysDictData> datas          = getDictCache( dictType );

		if ( StringUtils.containsAny( separator, dictValue ) && !CollectionUtils.isEmpty( datas ) ) {
			for ( SysDictData dict : datas ) {
				for ( String value : dictValue.split( separator ) ) {
					if ( value.equals( dict.getDictValue() ) ) {
						propertyString.append(dict.getDictLabel()).append(separator);
						break;
					}
				}
			}
		} else {
			for ( SysDictData dict : datas ) {
				if ( dictValue.equals( dict.getDictValue() ) ) {
					return dict.getDictLabel();
				}
			}
		}
		return StringUtils.stripEnd( propertyString.toString(), separator );
	}

	/**
	 * 根据字典类型和字典标签获取字典值
	 *
	 * @param dictType  字典类型
	 * @param dictLabel 字典标签
	 * @param separator 分隔符
	 * @return 字典值
	 */
	public String getDictValue( String dictType, String dictLabel, String separator ) {
		StringBuilder     propertyString = new StringBuilder();
		List<SysDictData> datas          = getDictCache( dictType );

		if ( StringUtils.containsAny( separator, dictLabel ) && !CollectionUtils.isEmpty( datas ) ) {
			for ( SysDictData dict : datas ) {
				for ( String label : dictLabel.split( separator ) ) {
					if ( label.equals( dict.getDictLabel() ) ) {
						propertyString.append(dict.getDictValue()).append(separator);
						break;
					}
				}
			}
		} else {
			for ( SysDictData dict : datas ) {
				if ( dictLabel.equals( dict.getDictLabel() ) ) {
					return dict.getDictValue();
				}
			}
		}
		return StringUtils.stripEnd( propertyString.toString(), separator );
	}
}
