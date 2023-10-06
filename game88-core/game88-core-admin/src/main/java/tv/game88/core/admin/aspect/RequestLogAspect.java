package tv.game88.core.admin.aspect;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.multipart.MultipartFile;
import tv.game88.common.utils.LocalDateTimeUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求日志方面
 *
 * @author dan
 * @version 1.0
 */
@Log4j2
//@Component
//@Aspect
public class RequestLogAspect {

    /**
     * 请求地址
     */
    @Pointcut("execution(* tv.game88..*.admin.controller..*(..))")
    public void requestServer() {
    }

    /**
     * 任务地址
     */
    @Pointcut("execution(* tv.game88..*.admin.task..*(..))")
    public void taskServer() {
    }

    /**
     * 请求切面
     *
     * @param proceedingJoinPoint 进行连接点
     * @return {@link Object}
     * @throws Throwable throwable
     * @author dan
     * @since 2020/11/17
     */
    @Around("requestServer()")
    public Object doRequestAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return runProceed(proceedingJoinPoint);
    }

    /**
     * 任务切面
     *
     * @param proceedingJoinPoint 进行连接点
     * @return {@link Object}
     * @throws Throwable throwable
     */
//    @Around("taskServer()")
    public Object doTaskAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        return runProceed(proceedingJoinPoint);
    }

    private Object runProceed(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        //结果
//        sb.append("Result    : ").append(result).append("\n");
        //打印
        String sb = "\n" + "----------------------------" + "请求时间    " + LocalDateTimeUtils.format(LocalDateTime.now())
                + "----------------------------\n" +
                //类名称
                "Class     : " + proceedingJoinPoint.getSignature().getDeclaringTypeName() + "\n" +
                //方法名称
                "Method    : " + proceedingJoinPoint.getSignature().getName() + "\n" +
                //请求成功
                "Result    : " + "成功" + "\n" +
                //所有的请求参数
                "Params    : " + getRequestParamsByProceedingJoinPoint(proceedingJoinPoint) + "\n" +
                //接口时间
                "request Time: " + (System.currentTimeMillis() - start) + "\n";
        System.out.println(sb);
        return result;
    }

    /**
     * 执行异常
     *
     * @param joinPoint 连接点
     * @param e         e
     * @return
     * @author dan
     * @since 2020/11/17
     */
    @AfterThrowing(pointcut = "requestServer()" , throwing = "e")
    public void doAfterThrow(JoinPoint joinPoint, RuntimeException e) {
        Signature signature = joinPoint.getSignature();
        //打印
        String sb = "\n" + "----------------------------" + "请求时间    " + LocalDateTimeUtils.format(LocalDateTime.now())
                + "----------------------------\n" +
                //类名称
                "Class     : " + signature.getDeclaringTypeName() + "\n" +
                //方法名称
                "Method    : " + signature.getName() + "\n" +
                //请求成功
                "Result    : " + "失败" + "\n" +
                //所有的请求参数
                "Params    : " + getRequestParamsByJoinPoint(joinPoint) + "\n" +
                //结果
                "Result    : " + getExceptionMsg(e) + "\n";
        System.out.println(sb);
//        log.info("Error Request Info      : {}", JSON.toJSONString(requestErrorInfo));
    }

    /**
     * 获取入参
     *
     * @param proceedingJoinPoint
     * @return
     */
    private Map<String, Object> getRequestParamsByProceedingJoinPoint(ProceedingJoinPoint proceedingJoinPoint) {
        //参数名
        String[] paramNames = ((MethodSignature) proceedingJoinPoint.getSignature()).getParameterNames();
        //参数值
        Object[] paramValues = proceedingJoinPoint.getArgs();

        return buildRequestParam(paramNames, paramValues);
    }

    private Map<String, Object> getRequestParamsByJoinPoint(JoinPoint joinPoint) {
        //参数名
        String[] paramNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        //参数值
        Object[] paramValues = joinPoint.getArgs();

        return buildRequestParam(paramNames, paramValues);
    }

    private Map<String, Object> buildRequestParam(String[] paramNames, Object[] paramValues) {
        Map<String, Object> requestParams = new HashMap<>();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                Object value = paramValues[i];

                //如果是文件对象
                if ( value instanceof MultipartFile file ) {
                    value = file.getOriginalFilename();  //获取文件名
                }

                requestParams.put(paramNames[i], value);
            }
        }
        return requestParams;
    }

    /**
     * 获取exception的字符串
     *
     * @param e e
     * @return {@link String }
     * @author dan
     * @since 2020/11/17
     */
    public static String getExceptionMsg(Exception e) {
        StringWriter sw = new StringWriter();
        try {
            e.printStackTrace(new PrintWriter(sw));
        } finally {
            try {
                sw.close();
            } catch (IOException e1) {
                log.error( e.getMessage(), e );
            }
        }
        return sw.getBuffer().toString().replaceAll("\\$" , "T");
    }

}
