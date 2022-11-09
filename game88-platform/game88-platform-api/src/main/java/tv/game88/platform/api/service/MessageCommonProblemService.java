package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.platform.api.entity.MessageCommonProblem;

import java.util.List;

/**
 * 常用问题Service接口
 *
 * @author MengJun
 */
public interface MessageCommonProblemService extends IService<MessageCommonProblem> {
    /**
     * 查询常用问题列表
     *
     * @param messageCommonProblem 常用问题
     *
     * @return 常用问题集合
     */
    public List<MessageCommonProblem> selectMessageCommonProblemList( MessageCommonProblem messageCommonProblem );
}