package tv.game88.pay.api.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import tv.game88.common.utils.JsonUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Log4j2
public class BankAddressUtil {
	public static String calcAuthorization( String source, String secretId, String secretKey, String datetime )
			throws NoSuchAlgorithmException, InvalidKeyException {
		String signStr = "x-date: " + datetime + "\n" + "x-source: " + source;
		Mac    mac     = Mac.getInstance( "HmacSHA1" );
		Key    sKey    = new SecretKeySpec( secretKey.getBytes( StandardCharsets.UTF_8 ), mac.getAlgorithm() );
		mac.init( sKey );
		byte[] hash = mac.doFinal( signStr.getBytes( StandardCharsets.UTF_8 ) );
		String sig  = Base64.encodeBase64String( hash );
		return "hmac id=\"" + secretId + "\", algorithm=\"hmac-sha1\", headers=\"x-date x-source\", signature=\"" + sig + "\"";
	}

	public static String urlencode( Map<?, ?> map ) {
		StringBuilder sb = new StringBuilder();
		for ( Map.Entry<?, ?> entry : map.entrySet() ) {
			if ( !sb.isEmpty() ) {
				sb.append( "&" );
			}
			sb.append( String.format( "%s=%s",
					URLEncoder.encode( entry.getKey().toString(), StandardCharsets.UTF_8 ),
					URLEncoder.encode( entry.getValue().toString(), StandardCharsets.UTF_8 )
			) );
		}
		return sb.toString();
	}

	public static String getBankAddress( String bankCard, String secretId, String secretKey, String url )
			throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
		String           source = "market";
		Calendar         cd     = Calendar.getInstance();
		SimpleDateFormat sdf    = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US );
		sdf.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
		String datetime = sdf.format( cd.getTime() );
		// 签名
		String auth = calcAuthorization( source, secretId, secretKey, datetime );
		// 请求方法
		String method = "GET";
		// 请求头
		Map<String, String> headers = new HashMap<>();
		headers.put( "X-Source", source );
		headers.put( "X-Date", datetime );
		headers.put( "Authorization", auth );

		// 查询参数
		Map<String, String> queryParams = new HashMap<>();
		queryParams.put( "bankcard", bankCard.trim().replaceAll( " ","" )
				.replaceAll( "\"","" ) );
		// body参数
		Map<String, String> bodyParams = new HashMap<>();
		if ( !queryParams.isEmpty() ) {
			url += "?" + urlencode( queryParams );
		}

		BufferedReader in = null;
		try {
			URL               realUrl = new URL( url );
			HttpURLConnection conn    = ( HttpURLConnection ) realUrl.openConnection();
			conn.setConnectTimeout( 5000 );
			conn.setReadTimeout( 5000 );
			conn.setRequestMethod( method );
			// request headers
			for ( Map.Entry<String, String> entry : headers.entrySet() ) {
				conn.setRequestProperty( entry.getKey(), entry.getValue() );
			}
			// request body
			Map<String, Boolean> methods = new HashMap<>();
			methods.put( "POST", true );
			methods.put( "PUT", true );
			methods.put( "PATCH", true );
			Boolean hasBody = methods.get( method );
			if ( hasBody != null ) {
				conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
				conn.setDoOutput( true );
				DataOutputStream out = new DataOutputStream( conn.getOutputStream() );
				out.writeBytes( urlencode( bodyParams ) );
				out.flush();
				out.close();
			}
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
			String        line;
			StringBuilder result = new StringBuilder();
			while ( ( line = in.readLine() ) != null ) {
				result.append( line );
			}
			Map mapResult = JsonUtil.json2Map( result.toString() );
			log.warn( "调用银行卡归属地址查询返回参数:" + mapResult );
			if ( mapResult != null ) {
				boolean success = Boolean.parseBoolean( mapResult.get( "success" ).toString() ) ;
				String  code    = mapResult.get( "code" ).toString();
				if ( success && "200".equals( code ) ) {
					Map    dataMap  = ( Map ) mapResult.get( "data" );
					String province = ( String ) dataMap.get( "province" );
					String city     = ( String ) dataMap.get( "city" );
					if ( province.contains( "上海" ) && ( city == null || city.isEmpty() ) ) {
						city = "上海";
					}
					if ( province.contains( "北京" ) && ( city == null || city.isEmpty() ) ) {
						city = "北京";
					}
					if ( province.contains( "天津" ) && ( city == null || city.isEmpty() ) ) {
						city = "天津";
					}
					if ( province.contains( "重庆" ) && ( city == null || city.isEmpty() ) ) {
						city = "重庆";
					}
					if ( province.contains( "广西" ) && ( city == null || city.isEmpty() ) ) {
						city = "南宁";
					}
					if ( province.contains( "云南" ) && ( city == null || city.isEmpty() ) ) {
						city = "昆明";
					}
					return province + "/" + city;
				}
			}
		} catch ( Exception e ) {
			log.error( e.getMessage(),e );
		} finally {
			try {
				if ( in != null ) {
					in.close();
				}
			} catch ( Exception e2 ) {
				log.error( e2.getMessage(),e2 );
			}
		}
		return "未知地区";
	}
}