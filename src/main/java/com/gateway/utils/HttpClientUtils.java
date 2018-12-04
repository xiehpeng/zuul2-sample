package com.gateway.utils;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

/**
 * 带连接池功能的Http工具类
 *
 * @author tangdu
 * @version $: HttpClientUtils.java, v 0.1 2018年08月30日 22:01 tangdu Exp $
 */
public class HttpClientUtils {

  final static   String UA                         = "RuiXing/99.81 Chrome/62.0.3202.94 Safari/537.36";
  private static int    MAX_THREAD_NUM             = 400;
  private static int    DEFAULT_PER_ROUTE          = 50;
  public static int    CONNECT_TIMEOUT            = 10000;
  public static int    CONNECTION_REQUEST_TIMEOUT = 10000;
  public static int    SOCKET_TIMEOUT             = 30000;

  private static PoolingHttpClientConnectionManager POOLINGHTTPCLIENTCONNECTIONMANAGER = null;

  static {
    POOLINGHTTPCLIENTCONNECTIONMANAGER = new PoolingHttpClientConnectionManager();
    POOLINGHTTPCLIENTCONNECTIONMANAGER.setMaxTotal(MAX_THREAD_NUM);
    POOLINGHTTPCLIENTCONNECTIONMANAGER.setDefaultMaxPerRoute(DEFAULT_PER_ROUTE);
    POOLINGHTTPCLIENTCONNECTIONMANAGER.setValidateAfterInactivity(50);
  }

  public static CloseableHttpClient httpClient() throws Exception {
    SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
      @Override
      public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        return true;
      }
    }).build();
    //自定义重试策略,3次重试
    DefaultHttpRequestRetryHandler defaultHttpRequestRetryHandler =new DefaultHttpRequestRetryHandler(3,false);

    RequestConfig requestConfig = RequestConfig.custom()
        .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
        .setConnectTimeout(CONNECT_TIMEOUT)
        .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
        .setSocketTimeout(SOCKET_TIMEOUT).build();
    CloseableHttpClient httpClient = HttpClients.custom()
//        .setConnectionManager(POOLINGHTTPCLIENTCONNECTIONMANAGER)
        .setDefaultRequestConfig(requestConfig)
        .setRetryHandler(defaultHttpRequestRetryHandler)
        .setUserAgent(UA)
        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
        .setSSLContext(sslContext).build();
    return httpClient;
  }

  public static String post(String url, List<BasicNameValuePair> paramsList) throws Exception {
    CloseableHttpClient httpClient =HttpClientUtils.httpClient();
    CloseableHttpResponse httpResponse =null;
    try {
      HttpPost httpPost   =new HttpPost(url);
      UrlEncodedFormEntity urlEncodedFormEntity     = new UrlEncodedFormEntity(paramsList,"UTF-8");
      httpPost.setEntity(urlEncodedFormEntity);
      httpResponse = httpClient.execute(httpPost);
      int stateCode=httpResponse.getStatusLine().getStatusCode();
      String content="";
      if (stateCode == HttpStatus.SC_OK) {
          content= EntityUtils.toString(httpResponse.getEntity());
      }
      if(StringUtils.isBlank(content)){
        throw new IllegalArgumentException("Http请求结果异常");
      }
      return content;
    } finally {
      if(httpResponse!=null){
        httpResponse.close();
      }
      if(httpClient!=null) {
        httpClient.close();
      }
    }
  }

  public static String get(String url) throws Exception{
    CloseableHttpClient httpClient =HttpClientUtils.httpClient();
    CloseableHttpResponse httpResponse =null;
    try{
      HttpGet httpGet = new HttpGet(url);
      httpResponse = httpClient.execute(httpGet);
      int stateCode=httpResponse.getStatusLine().getStatusCode();
      String content="";
      if (stateCode == HttpStatus.SC_OK) {
        content= EntityUtils.toString(httpResponse.getEntity());
      }
      if(StringUtils.isBlank(content)){
        throw new IllegalArgumentException("Http请求结果异常");
      }
      return content;
    }finally {
      if(httpResponse!=null){
        httpResponse.close();
      }
      if(httpClient!=null) {
        httpClient.close();
      }
    }
  }
}
