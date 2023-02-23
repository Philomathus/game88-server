package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.entity.LogMoney;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.entity.MemberMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.member.mapper.LogMoneyMapper;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.mapper.MemberMoneyMapper;
import tv.game88.platform.api.service.MemberMoneyService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * 派送彩金暂存表Service业务层处理
 *
 * @author Rajesh
 * @date 2022-12-23
 */
@Log4j2
@Service
public class MemberMoneyServiceImpl extends ServiceImpl<MemberMoneyMapper, MemberMoney> implements MemberMoneyService {

    @Resource
    private MemberMoneyMapper memberMoneyMapper;
    @Resource
    private MemberInfoMapper memberInfoMapper;
    @Resource
    private MemberMoneyManager memberMoneyManager;
    @Resource
    private LogMoneyMapper logMoneyMapper;
    @Resource
    private RedisUtils redisUtils;

    private static final String LOCK_KEY = "initStarSend";

    /**
     * 查询派送彩金暂存表列表
     *
     * @param memberMoney 派送彩金暂存表
     * @return 派送彩金暂存表
     */
    @Override
    public List<MemberMoney> selectAllMemberMoneyList(MemberMoney memberMoney) {
        return memberMoneyMapper.selectMemberMoneyList(memberMoney);
    }

    /**
     * 行为类型统计 count money service impl
     */
    @Override
    public BigDecimal countMoney() {
        return memberMoneyMapper.countMoney();
    }

    /**
     * 查询派送彩金暂存表列表 remove all data service impl
     */
    @Override
    public Integer handleClean() {
        return memberMoneyMapper.handleClean();
    }

    @Override
    public RspBase<?> starSend(MemberMoney memberMoney, String adminName) {
        String key = memberMoney.getMoneydes();
        if (!lockUnlock(key, true)) {
            throw new BusinessException("请勿重复提交");
        }
        List<MemberMoney> memberMoneyList = memberMoneyMapper.selectMemberMoneyList(new MemberMoney());

        log.info("STARSEND: List size - {}", memberMoneyList.size());
        if (memberMoneyList.size() > 0) {
            BigDecimal deliveryLimit = new BigDecimal(10000);
            memberMoneyList.forEach(memberData -> {
                if (memberData.getMoney().compareTo(deliveryLimit) >= 0) {
                    throw new BusinessException(String.format("会员%s派送金额超过一万, 派送金额:%s",
                            memberData.getId(), memberData.getMoney()));
                }
                MemberInfo memberInfo = getMemberInfo(memberData.getId(), key);
                log.info("STARSEND: Processing memberMoney: {}, memberInfo: {}",
                        memberData.toString(), memberInfo.toString());
                processMoney(memberData, memberInfo, adminName, key);
            });
        } else {
            lockUnlock(key, false);
            throw new BusinessException("请先上传有数据的excel");
        }
        memberInfoMapper.clearMemberMoney();
        lockUnlock(key, false);
        return RspBase.ok("success");
    }

    @Transactional(rollbackFor = Exception.class)
    void processMoney(MemberMoney memberMoney, MemberInfo memberInfo, String adminName, String key) {
        processLogMoney(memberMoney, memberInfo, adminName, key);
        processMemberInfo(memberMoney, memberInfo);
    }

    void processLogMoney(MemberMoney memberMoney, MemberInfo memberInfo, String adminName, String moneyDes) {
        String startOfToday = LocalDateTimeUtils.format(LocalDateTimeUtils.getStartOfToday());
        String today = LocalDateTimeUtils.format(LocalDate.now());
        String userId = memberMoney.getId();
        BigDecimal money = memberMoney.getMoney();
        String markOrder = "CJ" + today + memberMoney.getId() + "_" +
                money.setScale(0, RoundingMode.HALF_UP) + moneyDes;
        List<LogMoney> markList;

        if (money.compareTo(BigDecimal.ZERO) > 0) {
            markList = logMoneyMapper.findMark(userId, markOrder, money, null,
                    userId.substring(userId.length() - 1), startOfToday);
        } else {
            markList = logMoneyMapper.findMark(userId, markOrder, null, money.negate(),
                    userId.substring(userId.length() - 1), startOfToday);
        }
        if (markList.size() > 0) {
            lockUnlock(moneyDes, false);
            throw new BusinessException(
                    "派送失败.请查看此笔金额是否今日已经入款过.如否请输入其他入款备注." + "会员id:" + userId + "入款金额" + money
                            + "入款备注" + moneyDes);
        }
        memberMoneyManager.addMemberMoneyStarSend(memberMoney, memberInfo, markOrder, adminName, moneyDes);
    }

    void processMemberInfo(MemberMoney memberMoney, MemberInfo memberInfo) {
        int addMemberInfo = memberInfoMapper.addMoneySelect(memberMoney.getId(), memberMoney.getMoney(),
                null, memberInfo.getCodeWill());
        if (addMemberInfo <= 0) {
            throw new BusinessException("资金记入失败,请重试");
        }
    }

    private MemberInfo getMemberInfo(String id, String key) {
        MemberInfo memberInfo = memberInfoMapper.selectMemberInfoById(id);
        if (memberInfo == null) {
            lockUnlock(key, false);
            throw new BusinessException("会员id不存在:" + id);
        }
        return memberInfo;
    }

    private boolean lockUnlock(String key, boolean lock) {
        if (lock) {
            return redisUtils.lock(LOCK_KEY + key, 50);
        } else {
            return redisUtils.unLock(LOCK_KEY + key);
        }
    }
}
