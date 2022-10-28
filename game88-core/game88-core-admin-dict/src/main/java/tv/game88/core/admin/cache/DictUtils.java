package tv.game88.core.admin.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.admin.entity.SysDictData;
import tv.game88.core.admin.mapper.SysDictDataMapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * 字典工具类
 *
 * @author MengJun
 */
@Log4j2
@Component
public class DictUtils {

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys:dictCache";

    /**
     * 分隔符
     */
    public static final String SEPARATOR = ",";

    @Resource
    private SysDictDataMapper sysDictDataMapper;
    @Resource
    private RedisUtils        redisUtil;

    /**
     * 设置字典缓存
     *
     * @param key       参数键
     * @param dictDatas 字典数据列表
     */
    public void setDictCache( String key, List<SysDictData> dictDatas ) {
        redisUtil.hSet( SYS_DICT_KEY, key, JsonUtil.object2Json( dictDatas ) );
    }

    /**
     * 根据字典类型和字典标签获取字典值
     *
     * @param dictType  字典类型
     * @param dictLabel 字典标签
     *
     * @return 字典值
     */
    public String getDictValue( String dictType, String dictLabel ) {
        return getDictValue( dictType, dictLabel, SEPARATOR );
    }

    /**
     * 清空字典缓存
     */
    public void clearDictCache() {
        redisUtil.unlink( SYS_DICT_KEY );
    }

    /**
     * 获取字典缓存
     *
     * @param key 参数键
     *
     * @return dictDatas 字典数据列表
     */
    public List<SysDictData> getDictCache( String key ) {
        if ( !redisUtil.hHasKey( SYS_DICT_KEY, key ) ) {
            List<SysDictData> dictDataList = sysDictDataMapper.selectDictDataByType( key );
            if ( !CollectionUtils.isEmpty( dictDataList ) ) {
                setDictCache( key, dictDataList );
            }
        }
        String cacheObj = ( String ) redisUtil.hGet( SYS_DICT_KEY, key );
        if ( StringUtils.isNotNull( cacheObj ) ) {
            return JsonUtil.json2Array( cacheObj, new TypeReference<>() {} );
        }
        return null;
    }

    /**
     * 根据字典类型和字典值获取字典标签
     *
     * @param dictType  字典类型
     * @param dictValue 字典值
     *
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
     *
     * @return 字典标签
     */
    public String getDictLabel( String dictType, String dictValue, String separator ) {
        StringBuilder     propertyString = new StringBuilder();
        List<SysDictData> datas          = getDictCache( dictType );

        if ( StringUtils.containsAny( separator, dictValue ) && !CollectionUtils.isEmpty( datas ) ) {
            for ( SysDictData dict : datas ) {
                for ( String value : dictValue.split( separator ) ) {
                    if ( value.equals( dict.getDictValue() ) ) {
                        propertyString.append( dict.getDictLabel() ).append( separator );
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
     *
     * @return 字典值
     */
    public String getDictValue( String dictType, String dictLabel, String separator ) {
        StringBuilder     propertyString = new StringBuilder();
        List<SysDictData> datas          = getDictCache( dictType );

        if ( StringUtils.containsAny( separator, dictLabel ) && !CollectionUtils.isEmpty( datas ) ) {
            for ( SysDictData dict : datas ) {
                for ( String label : dictLabel.split( separator ) ) {
                    if ( label.equals( dict.getDictLabel() ) ) {
                        propertyString.append( dict.getDictValue() ).append( separator );
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
