package tv.game88.core.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.core.config.entity.ConfigEnvironment;

import java.util.List;

/**
 * 环境参数配置Mapper接口
 *
 * @author MengJun
 */
public interface ConfigEnvironmentMapper extends BaseMapper<ConfigEnvironment> {

	/**
	 * 查询环境参数配置列表
	 *
	 * @param configEnvironment 环境参数配置
	 * @return 环境参数配置集合
	 */
	public List<ConfigEnvironment> selectConfigEnvironmentList( ConfigEnvironment configEnvironment );

	public List<ConfigEnvironment> selectConfigEnvironmentTwo( ConfigEnvironment configEnvironment );

	public Integer getTitleIndex( @Param( "title" ) String title, @Param( "code" ) String code );

	public int checkType( String envTitle );

	public int checkCode( String envValue );

    public String getValue();


	public List<ConfigEnvironment> selectConfigRecommendPic( ConfigEnvironment configEnvironment );

}
