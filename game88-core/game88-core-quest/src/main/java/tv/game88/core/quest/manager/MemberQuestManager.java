package tv.game88.core.quest.manager;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tv.game88.core.quest.entity.ActivityQuestInfo;
import tv.game88.core.quest.entity.MemberQuest;
import tv.game88.core.quest.mapper.MemberQuestMapper;

import javax.annotation.Resource;

@Service
@Log4j2
public class MemberQuestManager {
    @Resource
    private MemberQuestMapper memberQuestMapper;

    public void memberQuestProcess( String memberId, int add, ActivityQuestInfo confQuest ) {
        String memberQuestId = memberId.concat( "_" ).concat( confQuest.getId().toString() );
        MemberQuest memberQuest = memberQuestMapper.selectById( memberQuestId );
        if ( memberQuest == null ) {
            MemberQuest newMemberQuest = new MemberQuest();
            newMemberQuest.setMemberId( memberId );
            newMemberQuest.setQuestId( confQuest.getId() );
            newMemberQuest.setId( memberQuestId );
            newMemberQuest.setStatus( 0 );
            newMemberQuest.setCurNum( add );
            if ( newMemberQuest.getCurNum() >= confQuest.getTarget() ) {
                newMemberQuest.setCurNum( confQuest.getTarget() );
                newMemberQuest.setStatus( 1 );
            }
            newMemberQuest.setTaskMode( confQuest.getTaskMode() );
            memberQuestMapper.insert( newMemberQuest );
        } else if ( memberQuest.getStatus() == 0 ) {
            MemberQuest update = new MemberQuest();
            update.setCurNum( memberQuest.getCurNum() + add );
            if ( memberQuest.getCurNum() >= confQuest.getTarget() ) {
                update.setCurNum( confQuest.getTarget() );
                update.setStatus( 1 );
            }
            memberQuestMapper.updateById( update );
        }
    }
}
