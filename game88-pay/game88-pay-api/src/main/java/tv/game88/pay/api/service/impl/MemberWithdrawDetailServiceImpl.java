package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.*;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.config.cache.GenerateOrderCacheUtils;
import tv.game88.core.member.entity.MemberCard;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.member.mapper.MemberCardMapper;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.utils.TelegramBotMessage;
import tv.game88.pay.api.base.BasePayAgent;
import tv.game88.pay.api.base.PayAgentProcessorFactoryUtil;
import tv.game88.pay.api.constants.ConstantsPay;
import tv.game88.pay.api.dto.*;
import tv.game88.pay.api.entity.*;
import tv.game88.pay.api.mapper.*;
import tv.game88.pay.api.service.MemberWithdrawDetailService;
import tv.game88.pay.api.type.WithdrawRechargeType;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Log4j2
@Service
public class MemberWithdrawDetailServiceImpl extends ServiceImpl<MemberWithdrawDetailMapper, MemberWithdrawDetail> implements MemberWithdrawDetailService {
    @Resource
    private PayAgentLogMapper            payAgentLogMapper;
    @Resource
    private PayAgentChannelMapper        payAgentChannelMapper;
    @Resource
    private PayAgentPlatformMapper       payAgentPlatformMapper;
    @Resource
    private MemberInfoMapper             memberInfoMapper;
    @Resource
    private MemberMoneyManager           memberMoneyManager;
    @Resource
    private MemberCardMapper             memberCardMapper;
    @Resource
    private MemberRechargeBankMapper     memberRechargeBankMapper;
    @Resource
    private MemberRechargeUsdtMapper     memberRechargeUsdtMapper;
    @Resource
    private MemberRechargeOnlineMapper   memberRechargeOnlineMapper;
    @Resource
    private ConfigEnvCacheUtil           configEnvCacheUtil;
    @Resource
    private TelegramBotMessage           telegramBotMessage;
    @Resource
    private RedisUtils                   redisUtils;
    @Resource
    private PayAgentProcessorFactoryUtil payAgentProcessorFactoryUtil;
    @Resource
    private PasswordEncoder              passwordEncoder;

    @Override
    public RspMemberWithdrawDetailInfo getRspWithdrawDetail( String memberId ) {
        MemberInfo memberInfo = new QueryChainWrapper<>( memberInfoMapper ).eq( "id", memberId )
                                                                           .select( "code_will", "code_now", "account_now" )
                                                                           .one();
        return this.getRspWithdrawDetail( memberInfo.getCodeWill(), memberInfo.getCodeNow(), memberInfo.getAccountNow() );
    }

    private RspMemberWithdrawDetailInfo getRspWithdrawDetail( BigDecimal codeWill, BigDecimal codeNow, BigDecimal accountNow ) {
        //未打码金额 = 需求打码 - 累计有效打码
        BigDecimal noClean = codeWill.subtract( codeNow );
        if ( noClean.compareTo( BigDecimal.ZERO ) < 0 ) {
            noClean = BigDecimal.ZERO;
        }
        //可提现金额 = 账户余额 - 未打码金额
        BigDecimal canWithdrawMoney = accountNow.subtract( noClean );
        if ( canWithdrawMoney.compareTo( BigDecimal.ZERO ) < 0 ) {
            canWithdrawMoney = BigDecimal.ZERO;
        }
        RspMemberWithdrawDetailInfo rsp = new RspMemberWithdrawDetailInfo();
        rsp.setNeedBeat( noClean );
        rsp.setCanWithdrawMoney( canWithdrawMoney );
        rsp.setAccountNow( accountNow );
        return rsp;
    }

    @Override
    public List<MemberWithdrawDetail> selectMemberWithdrawDetailList( ReqMemberWithdrawDetail reqMemberWithdrawDetail ) {
        String[] selectDate = reqMemberWithdrawDetail.getSelectDate();
        if ( selectDate != null && selectDate.length > 0 ) {
            reqMemberWithdrawDetail.setSelectStartDate( selectDate[ 0 ] );
            reqMemberWithdrawDetail.setSelectEndDate( selectDate[ 1 ] );
        }
        List<MemberWithdrawDetail> withdrawDetails = this.baseMapper.selectMemberWithdrawDetailList( reqMemberWithdrawDetail );
        //查出会员状态是否为套利号
        if ( !CollectionUtils.isEmpty( withdrawDetails ) ) {
            List<String> memberIds = new ArrayList<>();
            //风控打码倍数
            String multipleCode = configEnvCacheUtil.getConf( "multiple_code" );
            for ( MemberWithdrawDetail me : withdrawDetails ) {
                //入款人姓名不为空，并且入款人不包含提现人，整条数据标红警告
                if ( StringUtils.isNotBlank( me.getBankUserName() ) && Strings.isNotBlank( me.getRechargeUserName() ) && !me
                        .getRechargeUserName().contains( me.getBankUserName() ) ) {
                    me.setRechargeUserNameStatus( 1 );//等于1,数据警告
                } else {
                    me.setRechargeUserNameStatus( 0 );
                }
                memberIds.add( me.getWithdrawId() );
                me.setMultipleCode( multipleCode );
            }
            List<MemberInfo> memberInfoList = memberInfoMapper.selectList( new QueryWrapper<MemberInfo>().in( "id", memberIds )
                                                                                                         .select( "id", "status"
                                                                                                                 ,
                                                                                                                 "register_time"
                                                                                                         ) );
            LocalDateTime date = LocalDateTime.now().minusHours( 48 );
            for ( MemberWithdrawDetail me : withdrawDetails ) {
                for ( MemberInfo st : memberInfoList ) {
                    if ( me.getWithdrawId().equals( st.getId() ) ) {
                        me.setMemberStatus( st.getStatus() );
                        if ( st.getRegisterTime().compareTo( date ) > 0 ) {
                            me.setRegisterColor( 1 );
                        }
                    }
                }
            }
        }
        return withdrawDetails;
    }

    @Override
    public List<MemberWithdrawDetail> selectMemberWithdrawDetailCount( ReqMemberWithdrawDetail reqMemberWithdrawDetail ) {
        if ( Objects.isNull( reqMemberWithdrawDetail.getSearchTime() ) ) {
            String[] searchTime = new String[] { LocalDateTimeUtils.format( LocalDateTimeUtils.getStartOfToday() ),
                    LocalDateTimeUtils.format( LocalDateTimeUtils.getEndOfToday() ) };
            reqMemberWithdrawDetail.setSearchTime( searchTime );
        }
        return this.baseMapper.countOpNameOrder( reqMemberWithdrawDetail );
    }

    @Override
    public List<RspMemberWithdrawDetailShunWei> selectMemberWithdrawDetailShunWeiList( ReqMemberWithdrawDetail req ) {
        return this.baseMapper.selectMemberWithdrawDetailShunWeiList( req.getIds() );
    }

    @Override
    public Map<String, Object> getTotal( ReqMemberWithdrawDetail req ) {
        String[] selectDate = req.getSelectDate();
        if ( selectDate != null && selectDate.length > 0 ) {
            req.setSelectStartDate( selectDate[ 0 ] );
            req.setSelectEndDate( selectDate[ 1 ] );
        }
        return this.baseMapper.getTotal( req );
    }

    @Override
    public RspBase<List<RspWithdrawReport>> withdrawReport( String id ) {
        if ( !redisUtils.lock( "WithdrawDetailReport" + id, 10 ) ) {
            return RspBase.businessError( "请勿重复查询" );
        }
        //取会员id最后一个字符
        String tableLast = id.substring( id.length() - 1 );

        RspMemberInfoWithdraw rspMemberInfo1  = this.baseMapper.selectMemberInfoWithdrawByIda( id, tableLast );
        RspMemberInfoWithdraw rspMemberInfo2  = this.baseMapper.selectMemberInfoWithdrawByIdb( id, tableLast );
        RspMemberInfoWithdraw rspMemberInfo3  = this.baseMapper.selectMemberInfoWithdrawByIdc( id, tableLast );
        RspMemberInfoWithdraw rspMemberInfo5  = this.baseMapper.selectMemberInfoWithdrawByIde( id, tableLast );
        RspMemberInfoWithdraw rspMemberInfo6  = this.baseMapper.selectMemberInfoWithdrawByIdf( id, tableLast );
        RspMemberInfoWithdraw rspMemberInfo7  = this.baseMapper.selectMemberInfoWithdrawByIdg( id, tableLast );
        RspMemberInfoWithdraw rspMemberInfo8  = this.baseMapper.selectMemberInfoWithdrawByIdh( id, tableLast );
        RspMemberInfoWithdraw rspMemberInfo9  = this.baseMapper.selectMemberInfoWithdrawByIdi( id, tableLast );
        RspMemberInfoWithdraw rspMemberInfo10 = this.baseMapper.selectMemberInfoWithdrawByIdj( id, tableLast );
        RspMemberInfoWithdraw rspMemberInfo11 = this.baseMapper.selectMemberInfoWithdrawByIdk( id, tableLast );
        //游戏投注详细
        List<RspMemberInfoWithdraw> rspMemberInfo12 = this.baseMapper.selectMemberInfoWithdrawByIdl( id, tableLast );
        RspMemberInfoWithdraw       rspMemberInfo13 = this.baseMapper.selectMemberInfoWithdrawByIdz( id, tableLast );

        List<RspWithdrawReport> withdrawReports = new LinkedList<>();
        RspWithdrawReport       withdrawReporta = new RspWithdrawReport();
        withdrawReporta.setClass_twoname( "禁言原因" );
        withdrawReporta.setT_value( rspMemberInfo1.getRemark() );
        withdrawReports.add( withdrawReporta );

        RspWithdrawReport withdrawRemark = new RspWithdrawReport();
        withdrawRemark.setClass_twoname( "会员备注" );
        withdrawRemark.setT_value( rspMemberInfo1.getRemark() );
        withdrawReports.add( withdrawRemark );

        RspWithdrawReport withdrawReportb = new RspWithdrawReport();
        withdrawReportb.setClass_twoname( "会员编号" );
        withdrawReportb.setT_value( rspMemberInfo1.getId() );
        withdrawReports.add( withdrawReportb );

        RspWithdrawReport withdrawReportc = new RspWithdrawReport();
        withdrawReportc.setClass_twoname( "用户类型" );
        if ( StringUtils.isNotBlank( rspMemberInfo1.getRegisterType() ) ) {
            if ( "0".equals( rspMemberInfo1.getRegisterType() ) ) {
                withdrawReportc.setT_value( "游客" );
            } else {
                withdrawReportc.setT_value( "会员" );
            }
        }
        withdrawReports.add( withdrawReportc );

        RspWithdrawReport withdrawReportd = new RspWithdrawReport();
        withdrawReportd.setClass_twoname( "会员VIP" );
        withdrawReportd.setT_value( rspMemberInfo1.getVip() );
        withdrawReports.add( withdrawReportd );

        RspWithdrawReport withdrawReportv = new RspWithdrawReport();
        withdrawReportv.setClass_twoname( "登录时间" );
        withdrawReportv.setT_value( rspMemberInfo1.getLoginTime() );
        withdrawReports.add( withdrawReportv );

        RspWithdrawReport withdrawReporte = new RspWithdrawReport();
        withdrawReporte.setClass_twoname( "会员注册时间" );
        withdrawReporte.setT_value( rspMemberInfo1.getRegTime() );
        withdrawReports.add( withdrawReporte );

        RspWithdrawReport withdrawReportf = new RspWithdrawReport();
        withdrawReportf.setClass_twoname( "会员积分" );
        withdrawReportf.setT_value( rspMemberInfo1.getAccountNow() );
        withdrawReports.add( withdrawReportf );

        RspWithdrawReport withdrawReportg = new RspWithdrawReport();
        withdrawReportg.setClass_twoname( "会员注单" );
        withdrawReportg.setT_value( rspMemberInfo1.getCodeTotal() );
        withdrawReports.add( withdrawReportg );

        RspWithdrawReport withdrawReporth = new RspWithdrawReport();
        withdrawReporth.setClass_twoname( "会员打码" );
        withdrawReporth.setT_value( rspMemberInfo1.getCodeNow() );
        withdrawReports.add( withdrawReporth );

        RspWithdrawReport withdrawReporti = new RspWithdrawReport();
        withdrawReporti.setClass_twoname( "登陆IP" );
        withdrawReporti.setT_value( rspMemberInfo1.getLoginIp() );
        withdrawReports.add( withdrawReporti );

        RspWithdrawReport withdrawReportk = new RspWithdrawReport();
        withdrawReportk.setClass_twoname( "线下充值金额" );
        withdrawReportk.setT_value( rspMemberInfo2.getRechargemoney() );
        withdrawReports.add( withdrawReportk );

        RspWithdrawReport withdrawReportz = new RspWithdrawReport();
        withdrawReportz.setClass_twoname( "USDT充值金额" );
        withdrawReportz.setT_value( rspMemberInfo13.getUsdtrechargemoney() );
        withdrawReports.add( withdrawReportz );

        RspWithdrawReport withdrawReportl = new RspWithdrawReport();
        withdrawReportl.setClass_twoname( "线上金额" );
        withdrawReportl.setT_value( rspMemberInfo3.getSubmoney() );
        withdrawReports.add( withdrawReportl );

        RspWithdrawReport withdrawReportn = new RspWithdrawReport();
        withdrawReportn.setClass_twoname( "手动增加金额" );
        withdrawReportn.setT_value( rspMemberInfo5.getRgIncome() );
        withdrawReports.add( withdrawReportn );

        RspWithdrawReport withdrawReporto = new RspWithdrawReport();
        withdrawReporto.setClass_twoname( "平台赠送金额" );
        withdrawReporto.setT_value( rspMemberInfo6.getZsIncome() );
        withdrawReports.add( withdrawReporto );

        RspWithdrawReport withdrawReportp = new RspWithdrawReport();
        withdrawReportp.setClass_twoname( "充值总的金额" );
        withdrawReportp.setT_value( rspMemberInfo7.getTotalincom() );
        withdrawReports.add( withdrawReportp );

        RspWithdrawReport withdrawReportq = new RspWithdrawReport();
        withdrawReportq.setClass_twoname( "会员提现次数" );
        withdrawReportq.setT_value( rspMemberInfo8.getWCount() );
        withdrawReports.add( withdrawReportq );

        RspWithdrawReport withdrawReportr = new RspWithdrawReport();
        withdrawReportr.setClass_twoname( "会员提现金额" );
        withdrawReportr.setT_value( rspMemberInfo9.getWSum() );
        withdrawReports.add( withdrawReportr );

        RspWithdrawReport withdrawReportu = new RspWithdrawReport();
        withdrawReportu.setClass_twoname( "彩票异常投注次数" );
        withdrawReportu.setT_value( rspMemberInfo10.getGcount() );
        withdrawReports.add( withdrawReportu );

        RspWithdrawReport withdrawReportt = new RspWithdrawReport();
        withdrawReportt.setClass_twoname( "彩票总投注笔数" );
        withdrawReportt.setT_value( rspMemberInfo11.getGtcount() );
        withdrawReports.add( withdrawReportt );

        //游戏
        if ( rspMemberInfo12 != null && rspMemberInfo12.size() != 0 ) {
            for ( RspMemberInfoWithdraw rs : rspMemberInfo12 ) {
                RspWithdrawReport withdrawReportTwo = new RspWithdrawReport();
                withdrawReportTwo.setClass_twoname( rs.getClassTwoname() );
                withdrawReportTwo.setT_value( "投注:" + rs.getTouZhu() + "盈利:" + rs.getYingLi() );
                withdrawReports.add( withdrawReportTwo );
            }
        }
        redisUtils.unLock( "WithdrawDetailReport" + id );
        return RspBase.ok( withdrawReports );
    }

    @Override
    public List<RspWithdrawReport> withdrawReportList() {
        return this.baseMapper.withdrawReportList();
    }

    @Override
    public RspBase<?> refused( ReqMemberWithdrawDetail req, String userName ) {
        MemberWithdrawDetail memberWithdrawLog = this.getById( req.getId() );
        if ( memberWithdrawLog == null ) {
            return RspBase.businessError( "订单不存在" );
        }
        if ( memberWithdrawLog.getStatus() == 2 ) {
            return RspBase.businessError( "订单重复处理" );
        }


        if ( StringUtils.isNotBlank( memberWithdrawLog.getOpName() ) && !userName.equals( memberWithdrawLog.getOpName() ) ) {
            return RspBase.businessError( "该订单只能由" + memberWithdrawLog.getOpName() + "处理" );
        }
        if ( !redisUtils.lock( "WithdrawDetailRefused" + memberWithdrawLog.getWithdrawId(), 5 ) ) {
            return RspBase.businessError( "请勿重复提交" );
        }
        if ( memberWithdrawLog.getStatus() < 2 || memberWithdrawLog.getStatus() == 5 || memberWithdrawLog.getStatus() == 7
                || memberWithdrawLog.getStatus() == 8 ) {
            MemberWithdrawDetail update = new MemberWithdrawDetail();
            update.setWithdrawOrderNo( req.getId() );
            update.setRemark( req.getRemark() );
            update.setStatus( 2 );//审核不通过
            update.setOpName( userName );
            update.setUpdateTime( LocalDateTime.now() );

            MemberWithdrawDetailService detailService = SpringUtils.getBean( MemberWithdrawDetailService.class );

            String mark = "操作人:" + userName + " ip:" + ServletUtil.getIp();
            detailService.refusedUpdateProcess( update, mark, memberWithdrawLog );
        } else {
            return RspBase.businessError(
                    "会员ID为" + memberWithdrawLog.getWithdrawId() + "该笔订单状态" + memberWithdrawLog.getStatus()
                            + "该状态下订单不能拒绝" );
        }

        redisUtils.unLock( "WithdrawDetailRefused" + memberWithdrawLog.getWithdrawId() );
        return RspBase.ok();
    }

    @Transactional( rollbackFor = Exception.class )
    public void refusedUpdateProcess( MemberWithdrawDetail update, String mark, MemberWithdrawDetail memberWithdrawDetail ) {
        int i = this.baseMapper.updateById( update );
        if ( i <= 0 ) {
            throw new BusinessException( "回退失败" );
        }
        BigDecimal withdrawMoney = memberWithdrawDetail.getWithdrawMoney();
        if ( Objects.equals( memberWithdrawDetail.getBankId(), ConstantsPay.VIPPAY_BANK_ID )
                && StringUtils.isNotBlank( memberWithdrawDetail.getRealBankAddress() )
                && StringUtils.isNumeric( memberWithdrawDetail.getRealBankAddress() ) ) {
            withdrawMoney = withdrawMoney.subtract( new BigDecimal( memberWithdrawDetail.getRealBankAddress() ) );
        }
        //回退提现金额
        String businessId = memberWithdrawDetail.getWithdrawOrderNo() + "bohui";
        memberMoneyManager.addMemberMoney( memberWithdrawDetail.getWithdrawId(), withdrawMoney, EnumMoney.BOHUI, BigDecimal.ONE
                , mark, businessId, businessId );
    }

    @Override
    public RspBase<?> refuseds( ReqMemberWithdrawDetail req, String userName ) {
        if ( !redisUtils.lock( "WithdrawDetailRefused" + userName, 5 ) ) {
            return RspBase.businessError( "请勿重复提交" );
        }
        List<MemberWithdrawDetail> withdrawLogList = this.baseMapper.selectBatchIds( req.getIds() );
        if ( withdrawLogList == null || withdrawLogList.size() == 0 ) {
            return RspBase.businessError( "订单已被处理,请刷新界面" );
        }
        MemberWithdrawDetailService detailService = SpringUtils.getBean( MemberWithdrawDetailService.class );
        String                      mark          = "操作人:" + userName + " ip:" + ServletUtil.getIp();
        for ( MemberWithdrawDetail withdrawDetail : withdrawLogList ) {
            if ( withdrawDetail == null ) {
                return RspBase.businessError( "订单不存在" );
            }
            if ( StringUtils.isNotBlank( withdrawDetail.getOpName() ) && !userName.equals( withdrawDetail.getOpName() ) ) {
                return RspBase.businessError(
                        "会员ID为" + withdrawDetail.getWithdrawId() + "该订单只能由" + withdrawDetail.getOpName() + "处理" );
            }
            if ( withdrawDetail.getStatus() == 2 ) {
                return RspBase.businessError( "会员ID为" + withdrawDetail.getWithdrawId() + "该笔订单重复处理" );
            }
            if ( req.getStatus() < 2 || req.getStatus() == 5 || req.getStatus() == 7 || req.getStatus() == 8 ) {
                MemberWithdrawDetail update = new MemberWithdrawDetail();
                update.setWithdrawOrderNo( req.getId() );
                update.setRemark( req.getRemark() );
                update.setStatus( 2 );//审核不通过
                update.setOpName( userName );
                update.setUpdateTime( LocalDateTime.now() );
                detailService.refusedUpdateProcess( update, mark, withdrawDetail );
            } else {
                return RspBase.businessError(
                        "会员ID为" + withdrawDetail.getWithdrawId() + "该笔订单状态" + withdrawDetail.getStatus()
                                + "该状态下订单不能拒绝" );
            }
        }

        redisUtils.unLock( "WithdrawDetailRefused" + userName );
        return RspBase.ok();
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public RspBase<?> back( ReqMemberWithdrawDetail req, String userName ) {
        MemberWithdrawDetail withdrawDetail = this.baseMapper.selectById( req.getId() );
        if ( withdrawDetail == null ) {
            return RspBase.businessError( "订单不存在" );
        }
        if ( withdrawDetail.getStatus() != 4 ) {
            return RspBase.businessError( "该订单状态不是代付中" );
        }
        PayAgentLog payAgentLog = payAgentLogMapper.selectById( withdrawDetail.getWithdrawOrderNo() );
        if ( payAgentLog != null ) {
            int i = payAgentLogMapper.deleteById( withdrawDetail.getWithdrawOrderNo() );
            if ( i < 1 ) {
                throw new BusinessException( "代付记录删除失败，请重试!" );
            }
        }

        MemberWithdrawDetail update = new MemberWithdrawDetail();
        update.setWithdrawOrderNo( withdrawDetail.getWithdrawOrderNo() );
        update.setRemark( "由" + userName + "操作回退" );
        update.setStatus( 1 );
        update.setOpName( userName );

        int i = this.baseMapper.updateById( update );
        if ( i < 1 ) {
            throw new BusinessException( "回退订单状态失败" );
        }
        return RspBase.ok();
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public RspBase<?> failBack( ReqMemberWithdrawDetail req, String userName ) {
        MemberWithdrawDetail withdrawDetail = this.baseMapper.selectById( req.getId() );
        if ( withdrawDetail == null ) {
            return RspBase.businessError( "订单不存在" );
        }
        if ( withdrawDetail.getStatus() != 5 ) {
            return RspBase.businessError( "该订单状态不是代付失败" );
        }
        PayAgentLog payAgentLog = payAgentLogMapper.selectById( withdrawDetail.getWithdrawOrderNo() );
        if ( payAgentLog != null ) {
            int i = payAgentLogMapper.deleteById( withdrawDetail.getWithdrawOrderNo() );
            if ( i < 1 ) {
                throw new BusinessException( "代付记录删除失败，请重试!" );
            }
        }

        MemberWithdrawDetail update = new MemberWithdrawDetail();
        update.setWithdrawOrderNo( withdrawDetail.getWithdrawOrderNo() );
        update.setRemark( "由" + userName + "操作回退" );
        update.setStatus( 1 );
        update.setOpName( userName );
        int i = this.baseMapper.updateById( update );
        if ( i < 1 ) {
            throw new BusinessException( "回退订单状态失败" );
        }
        return RspBase.ok();
    }

    @Override
    public RspBase<?> queryStatus( ReqMemberWithdrawDetail req ) {
        PayAgentLog payAgentLog = payAgentLogMapper.selectById( req.getId() );
        if ( payAgentLog == null ) {
            return RspBase.businessError( "代付订单不存在" );
        }
        PayAgentChannel payAgentChannel = payAgentChannelMapper.selectById( payAgentLog.getChannelId() );
        if ( payAgentChannel == null ) {
            return RspBase.businessError( "此代付通道不存在" );
        }

        PayAgentPlatform payAgentPlatform = payAgentPlatformMapper.selectById( payAgentChannel.getPlatformId() );
        if ( payAgentPlatform == null ) {
            log.warn( "代付平台未找到 - platformId:{}", payAgentChannel.getPlatformId() );
            return RspBase.businessError( "代付平台未找到" );
        }
        BasePayAgent basePayAgent = payAgentProcessorFactoryUtil.createPayProcessor( payAgentPlatform.getCode() );
        String       msg          = null;
        try {
            msg = basePayAgent.queryOrderPay( payAgentLog );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        PayAgentLog payAgentLogNew = payAgentLogMapper.selectById( req.getId() );
        String msgStatus = switch ( payAgentLogNew.getCallbackStatus() ) {
            case 0 -> "代付处理中";
            case 1 -> "代付成功";
            case 2 -> "代付失败";
            default -> "";
        };
        return RspBase.ok( msgStatus + ",查询返回结果:" + msg );
    }

    @Override
    public RspBase<?> lock( ReqMemberWithdrawDetail req, String userName ) {
        MemberWithdrawDetail withdrawDetail = this.baseMapper.selectById( req.getId() );
        if ( withdrawDetail == null ) {
            return RspBase.businessError( "订单不存在" );
        }
        if ( withdrawDetail.getStatus() == 1 ) {
            return RspBase.businessError( "该订单已被锁定,请刷新界面" );
        }
        if ( withdrawDetail.getStatus() == 2 ) {
            return RspBase.businessError( "该订单已被拒绝" );
        }
        if ( withdrawDetail.getStatus() != 5 && 1 < withdrawDetail.getStatus() ) {
            return RspBase.businessError( "审核流程非法" );
        }

        if ( StringUtils.isNotBlank( withdrawDetail.getOpName() ) && !userName.equals( withdrawDetail.getOpName() ) ) {
            return RspBase.businessError( "该订单只能由" + withdrawDetail.getOpName() + "处理" );
        }

        MemberWithdrawDetail update = new MemberWithdrawDetail();
        update.setWithdrawOrderNo( req.getId() );
        update.setRemark( req.getRemark() );
        update.setStatus( 1 );
        update.setOpName( userName );
        update.setUpdateTime( LocalDateTime.now() );
        int i = this.baseMapper.updateById( update );
        if ( i > 0 ) {
            return RspBase.ok();
        }

        return RspBase.businessError( "更新订单状态失败" );
    }

    @Override
    public RspBase<?> locks( ReqMemberWithdrawDetail req, String userName ) {
        List<MemberWithdrawDetail> withdrawDetailList = this.baseMapper.selectBatchIds( req.getIds() );
        for ( MemberWithdrawDetail withdrawDetail : withdrawDetailList ) {
            if ( withdrawDetail.getStatus() == 1 ) {
                return RspBase.businessError( withdrawDetail.getWithdrawOrderNo() + "该订单已被锁定,请刷新界面" );
            }
            if ( withdrawDetail.getStatus() == 2 ) {
                return RspBase.businessError( withdrawDetail.getWithdrawOrderNo() + "该订单已被拒绝" );
            }
            if ( withdrawDetail.getStatus() != 5 && 1 < withdrawDetail.getStatus() ) {
                return RspBase.businessError( withdrawDetail.getWithdrawOrderNo() + "审核流程非法" );
            }

            if ( StringUtils.isNotBlank( withdrawDetail.getOpName() ) && !userName.equals( withdrawDetail.getOpName() ) ) {
                return RspBase.businessError(
                        withdrawDetail.getWithdrawOrderNo() + "该订单只能由" + withdrawDetail.getOpName() + "处理" );
            }

            MemberWithdrawDetail update = new MemberWithdrawDetail();
            update.setWithdrawOrderNo( withdrawDetail.getWithdrawOrderNo() );
            update.setRemark( req.getRemark() );
            update.setStatus( 1 );
            update.setOpName( userName );
            update.setUpdateTime( LocalDateTime.now() );
            int i = this.baseMapper.updateById( update );
            if ( i <= 0 ) {
                return RspBase.businessError( withdrawDetail.getWithdrawOrderNo() + "更新订单状态失败" );
            }
        }
        return RspBase.ok( "批量锁定成功" );
    }

    @Override
    public RspBase<?> unlock( ReqMemberWithdrawDetail req, String userName, boolean contains ) {
        MemberWithdrawDetail withdrawDetail = this.baseMapper.selectById( req.getId() );
        if ( withdrawDetail == null ) {
            return RspBase.businessError( "订单不存在" );
        }
        if ( withdrawDetail.getStatus() != 1 && withdrawDetail.getStatus() != 5 ) {
            return RspBase.businessError( "订单已被处理,请刷新界面" );
        }

        if ( !contains ) {
            if ( StringUtils.isNotBlank( withdrawDetail.getOpName() ) && !userName.equals( withdrawDetail.getOpName() ) ) {
                return RspBase.businessError( "该订单只能由" + withdrawDetail.getOpName() + "处理" );
            }
        }
        if ( !redisUtils.lock( "WithdrawDetailUnlock" + withdrawDetail.getWithdrawId(), 5 ) ) {
            return RspBase.businessError( "请勿重复提交" );
        }

        MemberWithdrawDetail update = new MemberWithdrawDetail();
        update.setWithdrawOrderNo( req.getId() );
        update.setRemark( "取消锁定人：" + userName );
        update.setStatus( 0 );
        update.setOpName( "" );
        update.setUpdateTime( LocalDateTime.now() );
        int i = this.baseMapper.updateById( update );
        if ( i > 0 ) {
            return RspBase.ok();
        }
        redisUtils.unLock( "WithdrawDetailUnlock" + withdrawDetail.getWithdrawId() );
        return RspBase.businessError( "更新订单状态失败" );
    }

    @Override
    public RspBase<?> artificial( ReqMemberWithdrawDetail req, String userName ) {
        MemberWithdrawDetail withdrawDetail = this.baseMapper.selectById( req.getId() );
        if ( withdrawDetail == null ) {
            return RspBase.businessError( "订单不存在" );
        }
        if ( withdrawDetail.getStatus() == 2 ) {
            return RspBase.businessError( "该订单已被拒绝" );
        }
        if ( withdrawDetail.getStatus() == 3 ) {
            return RspBase.businessError( "该订单已被终审,请刷新界面" );
        }
        if ( withdrawDetail.getStatus() != 5 && 3 < withdrawDetail.getStatus() ) {
            return RspBase.businessError( "审核流程非法" );
        }
        if ( !redisUtils.lock( "WithdrawDetailArtificial" + withdrawDetail.getWithdrawId(), 5 ) ) {
            return RspBase.businessError( "请勿重复提交" );
        }

        if ( StringUtils.isNotBlank( withdrawDetail.getOpName() ) && !userName.equals( withdrawDetail.getOpName() ) ) {
            return RspBase.businessError( "该订单已被" + withdrawDetail.getOpName() + "锁定" );
        }
        //判断是代付成功还是出款成功
        int status = 3;
        if ( req.getPayAgentChannelId() != null ) {
            if ( withdrawDetail.getStatus() == 6 ) {
                return RspBase.businessError( "该订单已被终审,请刷新界面" );
            }
            //设定状态为代付成功
            status = 6;
            PayAgentChannel payAgentChannel = payAgentChannelMapper.selectById( req.getPayAgentChannelId() );
            req.setRemark( "人工代付:" + payAgentChannel.getName() );
        }
        MemberWithdrawDetail update = new MemberWithdrawDetail();
        update.setWithdrawOrderNo( req.getId() );
        update.setRemark( req.getRemark() );
        update.setStatus( status );
        update.setOpName( userName );
        update.setUpdateTime( LocalDateTime.now() );
        int i = this.baseMapper.updateById( update );
        redisUtils.unLock( "WithdrawDetailArtificial" + withdrawDetail.getWithdrawId() );
        if ( i > 0 ) {
            return RspBase.ok( "人工出款成功" );
        }
        return RspBase.businessError( "人工出款失败" );
    }

    @Override
    public RspBase<?> updateRemark( ReqMemberWithdrawDetail req, String userName ) {
        MemberWithdrawDetail update = new MemberWithdrawDetail();
        update.setWithdrawOrderNo( req.getId() );
        update.setRemark( req.getRemark() );
        int i = this.baseMapper.updateById( update );
        return i > 0 ? RspBase.ok() : RspBase.businessError( "更改备注失败" );
    }

    @Override
    public RspBase<?> abnormalWithdrawal( ReqMemberWithdrawDetail req, String userName ) {
        MemberWithdrawDetail withdrawDetail = this.baseMapper.selectById( req.getId() );
        if ( withdrawDetail == null ) {
            return RspBase.businessError( "订单不存在" );
        }
        if ( withdrawDetail.getStatus() == 2 ) {
            return RspBase.businessError( "该订单已被拒绝" );
        }
        if ( withdrawDetail.getStatus() == 3 ) {
            return RspBase.businessError( "该订单已被终审,请刷新界面" );
        }
        if ( withdrawDetail.getStatus() != 5 && 3 < withdrawDetail.getStatus() ) {
            return RspBase.businessError( "审核流程非法" );
        }
        if ( !redisUtils.lock( "WithdrawDetailAbnormalWithdrawal" + withdrawDetail.getWithdrawId(), 5 ) ) {
            return RspBase.businessError( "请勿重复提交" );
        }

        if ( StringUtils.isNotBlank( withdrawDetail.getOpName() ) && !userName.equals( withdrawDetail.getOpName() ) ) {
            return RspBase.businessError( "该订单已被" + withdrawDetail.getOpName() + "锁定" );
        }

        MemberWithdrawDetail update = new MemberWithdrawDetail();
        update.setWithdrawOrderNo( req.getId() );
        update.setRemark( req.getRemark() );
        update.setStatus( 7 );
        update.setOpName( userName );
        update.setUpdateTime( LocalDateTime.now() );
        int i = this.baseMapper.updateById( update );
        if ( i > 0 ) {
            return RspBase.ok();
        }
        redisUtils.unLock( "WithdrawDetailAbnormalWithdrawal" + withdrawDetail.getWithdrawId() );
        return RspBase.businessError( "更新订单状态失败" );
    }

    @Override
    public RspBase<?> manualWithdrawal( ReqMemberWithdrawDetail req, String userName ) {
        MemberWithdrawDetail withdrawDetail = this.baseMapper.selectById( req.getId() );
        if ( withdrawDetail == null ) {
            return RspBase.businessError( "订单不存在" );
        }
        if ( withdrawDetail.getStatus() == 2 ) {
            return RspBase.businessError( "该订单已被拒绝" );
        }
        if ( withdrawDetail.getStatus() == 3 ) {
            return RspBase.businessError( "该订单已被终审,请刷新界面" );
        }
        if ( withdrawDetail.getStatus() != 5 && 3 < withdrawDetail.getStatus() ) {
            return RspBase.businessError( "审核流程非法" );
        }
        if ( !redisUtils.lock( "WithdrawDetailManualWithdrawal" + withdrawDetail.getWithdrawId(), 5 ) ) {
            return RspBase.businessError( "请勿重复提交" );
        }

        if ( StringUtils.isNotBlank( withdrawDetail.getOpName() ) && !userName.equals( withdrawDetail.getOpName() ) ) {
            return RspBase.businessError( "该订单已被" + withdrawDetail.getOpName() + "锁定" );
        }

        MemberWithdrawDetail update = new MemberWithdrawDetail();
        update.setWithdrawOrderNo( req.getId() );
        update.setStatus( 8 );
        update.setOpName( userName );
        update.setUpdateTime( LocalDateTime.now() );
        // update.setRemark( req.getRemark() );
        int i = this.baseMapper.updateById( update );
        if ( i > 0 ) {
            return RspBase.ok();
        }
        redisUtils.unLock( "WithdrawDetailManualWithdrawal" + withdrawDetail.getWithdrawId() );
        return RspBase.businessError( "更新订单状态失败" );
    }

    @Override
    public RspBase<?> memberWithdrawPassIsOpen( String memberId ) {
        MemberInfo memberInfo = new QueryChainWrapper<>( memberInfoMapper ).eq( "id", memberId ).select( "id", "withdrawal_pass" )
                                                                           .one();
        if ( memberInfo == null ) {
            return RspBase.businessError( "会员不存在" );
        }
        return RspBase.ok( StringUtils.isNotBlank( memberInfo.getWithdrawalPass() ) );
    }

    @Override
    public RspBase<?> memberWithdrawPassSet( String memberId, ReqBoxPass boxPass ) {
        MemberInfo memberInfo = new QueryChainWrapper<>( memberInfoMapper ).eq( "id", memberId ).select( "id", "withdrawal_pass" )
                                                                           .one();
        if ( memberInfo == null ) {
            return RspBase.businessError( "会员不存在" );
        }
        if ( StringUtils.isNotBlank( memberInfo.getWithdrawalPass() ) ) {
            return RspBase.businessError( "提现已经设置过密码" );
        }
        MemberInfo update = new MemberInfo();
        update.setId( memberId );
        update.setWithdrawalPass( passwordEncoder.encode( boxPass.getBoxPass() ) );
        int i = memberInfoMapper.updateById( update );
        return i > 0 ? RspBase.ok() : RspBase.businessError( "设置提现密码异常，请稍后再试" );
    }

    @Override
    public RspBase<?> withdrawBank( String memberId, ReqMemberCardWithdraw req ) {
        MemberInfo memberInfo = memberInfoMapper.selectById( memberId );
        if ( memberInfo == null ) {
            return RspBase.businessError( "会员不存在" );
        }
        // 1正常 4套利号 6投诉号 7审查号 : 可以提现 ; 2测试号 3超管号 5稀有号 : 不可以提现
        if ( !Arrays.asList( 1, 4, 6, 7 ).contains( memberInfo.getStatus() ) ) {
            return RspBase.businessError( "会员状态异常，不允许提现" );
        }
        if ( StringUtils.isBlank( memberInfo.getWithdrawalPass() ) ) {
            return RspBase.businessError( "请设置提现密码!" );
        }
        if ( !passwordEncoder.matches( req.getWithdrawalPass(), memberInfo.getWithdrawalPass() ) ) {
            return RspBase.businessError( "提现密码错误，请联系客服!" );
        }
        if ( StringUtils.isBlank( memberInfo.getPhone() ) ) {
            return RspBase.businessError( "请绑定手机号后提现" );
        }
        if ( memberInfo.getWithdrawalStatus() != null && memberInfo.getWithdrawalStatus() ) {
            return RspBase.businessError( "出款通道维护,请联系客服!" );
        }
        BigDecimal minimumWithdrawalAmount = configEnvCacheUtil.getConfBd( "minimum_withdrawal_amount" );
        if ( req.getWithdrawMoney().compareTo( minimumWithdrawalAmount ) < 0 ) {
            return RspBase.businessError( "提现金额必须大于" + minimumWithdrawalAmount );
        }
        if ( memberInfo.getAccountNow().compareTo( req.getWithdrawMoney() ) < 0 ) {
            return RspBase.businessError( "账户余额不足" );
        }
        RspMemberWithdrawDetailInfo withdrawDetailInfo = this.getRspWithdrawDetail( memberInfo.getCodeWill(),
                memberInfo.getCodeNow(), memberInfo.getAccountNow() );
        if ( req.getWithdrawMoney().compareTo( withdrawDetailInfo.getCanWithdrawMoney() ) > 0 ) {
            return RspBase.businessError( "未完成打码，超出可提现金额限制" );
        }
        if ( req.getWithdrawMoney().remainder( BigDecimal.ONE ).compareTo( BigDecimal.ZERO ) != 0 ) {
            return RspBase.businessError( "提现金额必须整数" );
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.withHour( configEnvCacheUtil.getConfInt( "withdraw_limit_start_hour" ) )
                                     .withMinute( configEnvCacheUtil.getConfInt( "withdraw_limit_start_minute" ) ).withSecond( 0 )
                                     .withNano( 0 );
        LocalDateTime endTime = now.withHour( 23 ).withMinute( 59 ).withSecond( 59 ).withNano( 999999999 );

        BigDecimal withdrawLimitMoney = configEnvCacheUtil.getConfBd( "withdraw_limit_money" );
        if ( req.getWithdrawMoney().compareTo( withdrawLimitMoney ) < 0 && !startTime.isAfter( now )
                && !endTime.isBefore( now ) ) {
            return RspBase.businessError( configEnvCacheUtil.getConf( "withdraw_limit_msg" ) );
        }
        //每日提现次数
        int withdraw_times = configEnvCacheUtil.getConfInt( "withdraw_times_day" );
        if ( withdraw_times > 0 ) {
            long times = this.baseMapper.selectCount( new QueryWrapper<MemberWithdrawDetail>().eq( "withdraw_id", memberId )
                                                                                              .ge( "create_time",
                                                                                                      LocalDateTimeUtils.getStartOfToday() )
                                                                                              .in( "status", 3, 6 )
                                                                                              .eq( "withdraw_money",
                                                                                                      req.getWithdrawMoney() ) );
            if ( times >= withdraw_times ) {
                return RspBase.businessError( "今日提现次数超过限制，请您改日申请提现" );
            }
        }
        MemberCard memberCard = memberCardMapper.selectById( req.getMemberCardId() );
        if ( memberCard == null ) {
            return RspBase.businessError( "提现银行卡不存在" );
        }

        BigDecimal introvipWithdrawLimitMoney = configEnvCacheUtil.getConfBd( "introvip_withdraw_limit_money" );
        if ( StringUtils.isNumeric( memberCard.getBankAccount().trim() )
                && !Objects.equals( introvipWithdrawLimitMoney, BigDecimal.ZERO )
                && req.getWithdrawMoney().compareTo( introvipWithdrawLimitMoney ) >= 0 ) {
            return RspBase.businessError( configEnvCacheUtil.getConf( "introvip_withdraw_limit_msg" ) );
        }

        if ( !redisUtils.lock( "withdrawBank" + memberId, 5 ) ) {
            return RspBase.businessError( "处理中请稍后" );
        }
        String withdrawOrderNo = SpringUtils.getBean( MemberWithdrawDetailService.class )
                                            .withdrawBank( memberInfo, req.getWithdrawMoney(), memberCard );
        MemberWithdrawDetail update = new MemberWithdrawDetail();
        update.setWithdrawOrderNo( withdrawOrderNo );
        //出款金额汇总
        BigDecimal withdrawMoney = this.baseMapper.totalWithdrawMoney( memberId );
        //入款:银行卡入款/线上充值/USDT充值
        BigDecimal recharge = memberRechargeBankMapper.totalRechargeAll( memberId );
        if ( recharge.compareTo( BigDecimal.ZERO ) > 0 && withdrawMoney.compareTo( BigDecimal.ZERO ) > 0 ) {
            update.setRechargeWithdrawRate( recharge.divide( withdrawLimitMoney, 2, RoundingMode.HALF_UP ) );
        } else {
            update.setRechargeWithdrawRate( BigDecimal.ZERO );
        }
        int i = this.baseMapper.updateById( update );
        if ( i > 0 ) {
            // TODO send message to telegram ; ID: withdraw_log_telegram ; message: 您有新的提现订单,金额:{},请及时处理!
            telegramBotMessage.sendByChatId( String.format( "您有新的提现订单,金额:%s,请及时处理!", req.getWithdrawMoney() ),
                    configEnvCacheUtil.getConf( "recharge_log_telegram" ) );
            return RspBase.ok( "提现申请请求成功，请耐心等待" );
        }
        return RspBase.businessError( "提现申请请求失败" );
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public String withdrawBank( MemberInfo memberInfo, BigDecimal withdrawMoney, MemberCard memberCard ) {
        BigDecimal withdrawAward     = BigDecimal.ZERO;
        BigDecimal withdrawAwardRate = null;
        // 套利号无优惠
        if ( Objects.equals( memberCard.getBankId(), ConstantsPay.VIPPAY_BANK_ID ) && memberInfo.getStatus() != 4 ) {
            String vipPayWithdrawAwardRate = configEnvCacheUtil.getConf( "vippay_withdraw_award_rate" );
            if ( StringUtils.isNotBlank( vipPayWithdrawAwardRate ) ) {
                String[] newVipPayRates = vipPayWithdrawAwardRate.split( ";" );
                for ( String rates : newVipPayRates ) {
                    String[]   spit   = rates.split( "," );
                    BigDecimal amount = new BigDecimal( spit[ 0 ] );
                    if ( withdrawMoney.compareTo( amount ) >= 0 ) {
                        String[] spits = spit[ 1 ].split( "-" );

                        withdrawAwardRate = RandomUtils.randomDecimalWithMax( new BigDecimal( spits[ 0 ] ),
                                new BigDecimal( spits[ 1 ] ) );
                        withdrawAward     = withdrawMoney.multiply( withdrawAwardRate ).setScale( 0, RoundingMode.HALF_UP );
                    }

                }
            }
        }

        MemberWithdrawDetail memberWithdrawDetail = new MemberWithdrawDetail();
        memberWithdrawDetail.setWithdrawOrderNo( GenerateOrderCacheUtils.me.getOrderId( "TX", 3 ) );
        memberWithdrawDetail.setWithdrawId( memberInfo.getId() );
        memberWithdrawDetail.setWithdrawMoney( withdrawMoney.add( withdrawAward ) );
        if ( withdrawAward.compareTo( BigDecimal.ZERO ) > 0 ) {
            memberWithdrawDetail.setRemark(
                    "vipPay提现" + withdrawMoney + ",随机奖励" + withdrawAward + ",比例:" + withdrawAwardRate.toString() );
            memberWithdrawDetail.setRealBankAddress( withdrawAward.toString() );
        } else {
            memberWithdrawDetail.setRealBankAddress( memberCard.getRealBankAddress() );
        }
        memberWithdrawDetail.setBankId( memberCard.getBankId() );
        memberWithdrawDetail.setBankAccount( memberCard.getBankAccount() );
        memberWithdrawDetail.setBankAddress( memberCard.getBankAddress() );
        memberWithdrawDetail.setBankUserName( memberCard.getRealName() );
        memberWithdrawDetail.setStatus( 0 );
        memberWithdrawDetail.setCreateTime( LocalDateTime.now() );
        memberWithdrawDetail.setUpdateTime( memberWithdrawDetail.getCreateTime() );
        memberWithdrawDetail.setFirst(
                this.baseMapper.selectCount( new QueryWrapper<MemberWithdrawDetail>().eq( "withdraw_id", memberInfo.getId() )
                                                                                     .in( "status", 3, 6 ) ) <= 0 );

        memberWithdrawDetail.setBankRechargeNum( new QueryChainWrapper<>( memberRechargeBankMapper )
                .eq( "member_id", memberInfo.getId() ).eq( "status", 3 ).ge( "create_time", LocalDateTimeUtils.getStartOfToday() )
                .count() );
        List<MemberRechargeBank> memberRechargeBanks = new QueryChainWrapper<>( memberRechargeBankMapper )
                .eq( "member_id", memberInfo.getId() ).eq( "status", 3 ).ge( "create_time", LocalDateTime.now().minusMonths( 1 ) )
                .select( "recharge_real_name" ).list();
        List<String> rechargeRealNames = memberRechargeBanks.stream().map( MemberRechargeBank::getRechargeRealName ).distinct()
                                                            .toList();
        memberWithdrawDetail.setRechargeUserName( StringUtils.join( rechargeRealNames.toArray(), "," ) );
        BigDecimal code_total   = memberInfo.getCodeTotal();//累计有效投注
        BigDecimal code_account = memberInfo.getCodeNow();//打码账户
        if ( code_account != null && code_account.compareTo( BigDecimal.ZERO ) > 0 ) {
            memberWithdrawDetail.setRechargeBcodeRate( code_total.divide( code_account, 2, RoundingMode.HALF_UP ) );
        } else {
            memberWithdrawDetail.setRechargeBcodeRate( BigDecimal.ZERO );
        }
        this.baseMapper.insert( memberWithdrawDetail );
        memberMoneyManager.reduceMoney( memberInfo.getId(), withdrawMoney, EnumMoney.WITHDRAW,
                memberCard.getRealName() + memberCard.getBankAccount() );
        return memberWithdrawDetail.getWithdrawOrderNo();
    }

    @Override
    public List<RspWithdrawRechargeDetail> withdrawRechargeDetail( String memberId, WithdrawRechargeType type ) {
        List<RspWithdrawRechargeDetail> resultList = switch ( type ) {
            case withdraw -> this.baseMapper.selectRspDetail( memberId );
            case rechargeBank -> memberRechargeBankMapper.selectRspDetail( memberId );
            case rechargeUsdt -> memberRechargeUsdtMapper.selectRspDetail( memberId );
            case rechargeOnline -> memberRechargeOnlineMapper.selectRspDetail( memberId );
        };
        for ( RspWithdrawRechargeDetail detail : resultList ) {
            switch ( type ) {
            case withdraw -> this.setWithdrawColor( detail );
            case rechargeBank, rechargeUsdt -> this.setRechargeBankColor( detail );
            case rechargeOnline -> this.setRechargeOnlineColor( detail );
            }
        }
        return resultList;
    }

    private void setRechargeOnlineColor( RspWithdrawRechargeDetail detail ) {
        switch ( detail.getStatus() ) {
        case -1 -> {
            detail.setColor( "#FF2A4E" );
            detail.setRemark( "申请入款" );
        }
        case 0 -> {
            detail.setColor( "#FF0E0E" );
            detail.setRemark( "入款失败" );
        }
        case 1 -> {
            detail.setColor( "#12ED40" );
            detail.setRemark( "入款成功" );
        }
        }
    }

    private void setRechargeBankColor( RspWithdrawRechargeDetail detail ) {
        switch ( detail.getStatus() ) {
        case 0, 1 -> {
            detail.setColor( "#FF2A4E" );
            detail.setRemark( "申请入款" );
        }
        case 2 -> {
            detail.setColor( "#FF0E0E" );
            if ( StringUtils.isBlank( detail.getRemark() ) ) {
                detail.setRemark( "拒绝入款" );
            } else {
                detail.setRemark( "拒绝入款" + ":" + detail.getRemark() );
            }
        }
        case 3 -> {
            detail.setColor( "#12ED40" );
            detail.setRemark( "入款成功" );
        }
        case 4 -> {
            detail.setColor( "#FF0E0E" );
            if ( StringUtils.isBlank( detail.getRemark() ) ) {
                detail.setRemark( "入款失败" );
            } else {
                detail.setRemark( "入款失败" + ":" + detail.getRemark() );
            }
        }
        }
    }

    private void setWithdrawColor( RspWithdrawRechargeDetail detail ) {
        switch ( detail.getStatus() ) {
        case 0, 1, 4, 8 -> detail.setRemark( "提现中" );
        case 3, 6 -> detail.setRemark( "提现成功" );
        case 7 -> detail.setRemark( "提现成功，请联系客服" );
        case 2, 5 -> {
            detail.setColor( "#FF0E0E" );
            detail.setRemark( "提现失败" + (
                    detail.getStatus() == 2 && StringUtils.isNotBlank( detail.getRemark() ) && !detail.getRemark()
                                                                                                      .contains( "锁定" ) ?
                            ":" + detail.getRemark() : "" ) );
        }
        }
    }
}

