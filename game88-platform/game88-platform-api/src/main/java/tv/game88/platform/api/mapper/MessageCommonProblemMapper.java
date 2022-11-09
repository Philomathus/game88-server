package tv.game88.platform.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.platform.api.entity.MessageCommonProblem;

import java.util.List;

/**
 * 常用问题Mapper接口
 *
 * @author MengJun
 */
public interface MessageCommonProblemMapper extends BaseMapper<MessageCommonProblem> {

    /**
     * 查询常用问题列表
     *
     * @param messageCommonProblem 常用问题
     *
     * @return 常用问题集合
     */
    public List<MessageCommonProblem> selectMessageCommonProblemList( MessageCommonProblem messageCommonProblem );
}