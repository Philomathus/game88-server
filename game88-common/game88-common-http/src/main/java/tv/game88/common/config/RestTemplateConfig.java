package tv.game88.common.config;

import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class RestTemplateConfig {
    private static int getMaxCpuCore() {
        return Runtime.getRuntime().availableProcessors();
    }


    @Bean( "restUploadTemplate" )
    @Lazy
    public RestTemplate uploadTemplate( RestTemplateBuilder builder ) throws Exception {
        RestTemplate restTemplate = builder.build();
        restTemplate.setRequestFactory( clientHttpUploadRequestFactory() );
        // 使用 utf-8 编码集的 conver 替换默认的 conver（默认的 string conver 的编码集为"ISO-8859-1"）
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        messageConverters.removeIf( converter -> converter instanceof StringHttpMessageConverter );
        messageConverters.add( new StringHttpMessageConverter( StandardCharsets.UTF_8 ) );
        //messageConverters.add( new StringHttpMessageConverter( Charset.forName("gbk")) );

        return restTemplate;
    }

    @Bean
    @Lazy
    public RestTemplate restTemplate( RestTemplateBuilder builder ) throws Exception {
        RestTemplate restTemplate = builder.build();
        restTemplate.setRequestFactory( clientHttpRequestFactory() );
        // 使用 utf-8 编码集的 conver 替换默认的 conver（默认的 string conver 的编码集为"ISO-8859-1"）
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        messageConverters.removeIf( converter -> converter instanceof StringHttpMessageConverter );
        messageConverters.add( new StringHttpMessageConverter( StandardCharsets.UTF_8 ) );
        //messageConverters.add( new StringHttpMessageConverter( Charset.forName("gbk")) );

        return restTemplate;
    }

    @Bean
    public HttpClientConnectionManager poolingConnectionManager() throws Exception {
        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
        poolingConnectionManager.setMaxTotal( 520 ); // 连接池最大连接数
        //poolingConnectionManager.setDefaultMaxPerRoute(100); // 每个主机的并发
        poolingConnectionManager.setDefaultMaxPerRoute( 2 * getMaxCpuCore() );// // 单路由的并发数
        return poolingConnectionManager;
    }

    @Bean
    public HttpClientBuilder httpClientBuilder() throws Exception {
        TrustStrategy acceptingTrustStrategy = ( x509Certificates, authType ) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial( null, acceptingTrustStrategy ).build();
        SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory( sslContext,
                new NoopHostnameVerifier() );

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setSSLSocketFactory( connectionSocketFactory );
        //设置HTTP连接管理器
        httpClientBuilder.setConnectionManager( poolingConnectionManager() );
        // 重试次数3次，并开启
        //httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(3,true));
        // 保持长链接配置，keep-alive
        httpClientBuilder.setKeepAliveStrategy( new DefaultConnectionKeepAliveStrategy() );

        return httpClientBuilder;
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() throws Exception {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient( httpClientBuilder().build() );
        clientHttpRequestFactory.setConnectTimeout( 6000 ); // 连接超时，毫秒
        clientHttpRequestFactory.setReadTimeout( 6000 ); // 读写超时，毫秒
        //clientHttpRequestFactory.setBufferRequestBody(false);//是否使用缓存流
        // 连接池不够用时候等待时间长度设置
        clientHttpRequestFactory.setConnectionRequestTimeout( 3000 );
        return clientHttpRequestFactory;
    }

    @Bean
    public ClientHttpRequestFactory clientHttpUploadRequestFactory() throws Exception {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient( httpClientBuilder().build() );
        clientHttpRequestFactory.setConnectTimeout( 600000 ); // 连接超时，毫秒
        clientHttpRequestFactory.setReadTimeout( 600000 ); // 读写超时，毫秒
        //clientHttpRequestFactory.setBufferRequestBody(false);//是否使用缓存流
        // 连接池不够用时候等待时间长度设置
        clientHttpRequestFactory.setConnectionRequestTimeout( 3000 );
        return clientHttpRequestFactory;
    }

}
 