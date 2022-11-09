package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.platform.api.entity.MessageCommonProblem;
import tv.game88.platform.api.mapper.MessageCommonProblemMapper;
import tv.game88.platform.api.service.MessageCommonProblemService;

import java.util.List;

/**
 * 常用问题Service业务层处理
 *
 * @author MengJun
 */
@Service
public class MessageCommonProblemServiceImpl extends ServiceImpl<MessageCommonProblemMapper, MessageCommonProblem> implements MessageCommonProblemService {
    /**
     * 查询常用问题列表
     *
     * @param messageCommonProblem 常用问题
     *
     * @return 常用问题
     */
    @Override
    public List<MessageCommonProblem> selectMessageCommonProblemList( MessageCommonProblem messageCommonProblem ) {
        return this.baseMapper.selectMessageCommonProblemList( messageCommonProblem );
    }
}