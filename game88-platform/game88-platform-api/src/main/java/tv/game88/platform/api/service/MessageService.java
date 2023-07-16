package tv.game88.platform.api.service;

import tv.game88.platform.api.dto.MessageReceiverId;
import tv.game88.platform.api.dto.RspMessageCommonProblem;
import tv.game88.platform.api.dto.RspMessageHomeNotice;
import tv.game88.platform.api.dto.RspMessageOnSite;

import java.util.List;

public interface MessageService {
    List<RspMessageHomeNotice> getMessageHomeNotices();

    List<RspMessageCommonProblem> getMessageCommonProblems();

    List<RspMessageOnSite> getMessageOnSites( String userId );
}
