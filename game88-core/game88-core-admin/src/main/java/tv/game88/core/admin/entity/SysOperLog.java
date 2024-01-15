package tv.game88.core.admin.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.common.vo.BaseEntity;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 操作日志记录表 sys_oper_log
 *
 * @author MengJun
 */
@EqualsAndHashCode( callSuper = true )
@Data
@TableName( "sys_oper_log" )
public class SysOperLog extends BaseEntity {
	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 日志主键
	 */
	@Excel( name = "操作序号" )
	@TableId( value = "oper_id", type = IdType.AUTO )
	private Long operId;

	/**
	 * 操作模块
	 */
	@Excel( name = "操作模块" )
	private String title;

	/**
	 * 业务类型（0其它 1新增 2修改 3删除）
	 */
	@Excel( name = "业务类型" )
	private Integer businessType;

	/**
	 * 业务类型数组
	 */
	private Integer[] businessTypes;

	/**
	 * 请求方法
	 */
	@Excel( name = "请求方法" )
	private String method;

	/**
	 * 请求方式
	 */
	@Excel( name = "请求方式" )
	private String requestMethod;

	/**
	 * 操作类别（0其它 1后台用户 2手机端用户）
	 */
	@Excel( name = "操作类别" )
	private Integer operatorType;

	/**
	 * 操作人员
	 */
	@Excel( name = "操作人员" )
	private String operName;

	/**
	 * 部门名称
	 */
	@Excel( name = "部门名称" )
	private String deptName;

	/**
	 * 请求url
	 */
	@Excel( name = "请求地址" )
	private String operUrl;

	/**
	 * 操作地址
	 */
	@Excel( name = "操作地址" )
	private String operIp;

	/**
	 * 操作地点
	 */
	@Excel( name = "操作地点" )
	private String operLocation;

	/**
	 * 请求参数
	 */
	@Excel( name = "请求参数" )
	private String operParam;

	/**
	 * 返回参数
	 */
	@Excel( name = "返回参数" )
	private String jsonResult;

	/**
	 * 操作状态（0正常 1异常）
	 */
	@Excel( name = "状态" )
	private Integer status;

	/**
	 * 错误消息
	 */
	@Excel( name = "错误消息" )
	private String errorMsg;

	/**
	 * 操作时间
	 */
	@JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
	@Excel( name = "操作时间", width = 30, databaseFormat = "yyyy-MM-dd HH:mm:ss" )
	private LocalDateTime operTime;
}
