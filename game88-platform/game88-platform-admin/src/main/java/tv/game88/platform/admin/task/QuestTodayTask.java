package tv.game88.platform.admin.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.quest.mapper.MemberQuestMapper;

import jakarta.annotation.Resource;

/**
 * 每日任务重置调度
 */
@Component
public class QuestTodayTask {
    @Resource
    private RedisUtils        redisUtil;
    @Resource
    private MemberQuestMapper memberQuestMapper;

    //每天凌晨0点执行
    @Scheduled( cron = "0 0 0 * * ?" )
    public void cleanDayTaskStatus() {
        if ( !redisUtil.lock( "QuestTodayTask", 100 ) ) {
            return;
        }
        //重置每日任务状态、打码量和有效时间
        memberQuestMapper.resetDayTaskStatus();
    }
}
