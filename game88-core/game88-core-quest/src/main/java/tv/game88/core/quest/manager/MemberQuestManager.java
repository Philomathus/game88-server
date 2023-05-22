package tv.game88.core.quest.manager;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tv.game88.core.quest.entity.ActivityQuestInfo;
import tv.game88.core.quest.entity.MemberQuest;
import tv.game88.core.quest.mapper.MemberQuestMapper;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Service
@Log4j2
public class MemberQuestManager {
    @Resource
    private MemberQuestMapper memberQuestMapper;

    public void memberQuestProcess( String memberId, BigDecimal add, ActivityQuestInfo confQuest ) {
        String      memberQuestId = memberId.concat( "_" ).concat( confQuest.getId().toString() );
        MemberQuest memberQuest   = memberQuestMapper.selectById( memberQuestId );

        BigDecimal targetNum = new BigDecimal( confQuest.getTarget() );
        if ( memberQuest == null ) {
            MemberQuest newMemberQuest = new MemberQuest();
            newMemberQuest.setMemberId( memberId );
            newMemberQuest.setQuestId( confQuest.getId() );
            newMemberQuest.setId( memberQuestId );
            newMemberQuest.setStatus( 0 );
            newMemberQuest.setCurNum( add );
            if ( newMemberQuest.getCurNum().compareTo( targetNum ) >= 0 ) {
                newMemberQuest.setCurNum( targetNum );
                newMemberQuest.setStatus( 1 );
            }
            newMemberQuest.setTaskMode( confQuest.getTaskMode() );
            memberQuestMapper.insert( newMemberQuest );
        } else if ( memberQuest.getStatus() == 0 ) {
            MemberQuest update = new MemberQuest();
            update.setId( memberQuestId );
            update.setCurNum( memberQuest.getCurNum().add( add ) );
            if ( update.getCurNum().compareTo( targetNum ) >= 0 ) {
                update.setCurNum( targetNum );
                update.setStatus( 1 );
            }
            memberQuestMapper.updateById( update );
        }
    }
}
