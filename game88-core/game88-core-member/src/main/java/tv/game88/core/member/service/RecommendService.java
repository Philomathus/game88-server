package tv.game88.core.member.service;

import tv.game88.core.member.entity.MemberInfo;

import java.math.BigDecimal;

public interface RecommendService {
    public void recommendProcess( MemberInfo memberInfo, BigDecimal rechargeMoney );
}