package com.view26.ci.plugin.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.kohsuke.stapler.StaplerRequest;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import static org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

/**
 * @author trongle
 * @version 10/21/2015 2:09 PM trongle $
 * @since 1.0
 */
public class HttpClientUtils {
  public static Integer RETRY_MAX_COUNT = 3;
  public static Boolean RETRY_REQUEST_SEND_RETRY_ENABLED = false;
  private static final String AWS_METADATA_MAC_URL  ="http://169.254.169.254/latest/meta-data/mac";
  private static HttpClient CLIENT;

  private HttpClientUtils() {
  }

  private static HttpClient getClient() throws ClientRequestException {
    initClient();
    return CLIENT;
  }

  private static synchronized void initClient() throws ClientRequestException {
    if (null == CLIENT) {
      try {
        CLIENT = getHttpClient();
      } catch (Exception e) {
        throw new ClientRequestException(e.getMessage());
      }
    }
  }

  /**
   * Encode url
   *
   * @param url
   * @return
   */
  public static String encode(String url) {
    try {
      return URLEncoder.encode(url, "UTF-8");
    } catch (Exception e) {
      return url;
    }
  }

  /**
   * @param request
   * @return
   */
  public static String getServerUrl(StaplerRequest request) {
    Boolean isDefaultPort = request.getServerPort() == 443 || request.getServerPort() == 80;
    return String.format("%s://%s%s%s%s", request.getScheme(), request.getServerName(),
      isDefaultPort ? "" : ":", request.getServerPort(), request.getContextPath());
  }

  /**
   * get port from url
   *
   * @param url
   * @return
   */
  public static int getPort(String url) {
    URL uri = null;
    try {
      uri = new URL(url);
    } catch (Exception e) {
    }
    int port = 0;
    if (uri != null) {
      port = (uri.getPort() > 0 ? uri.getPort() : ("http".equalsIgnoreCase(uri.getProtocol()) ? 80 : 443));
    }
    return port;
  }

  /**
   * get mac address and port
   *
   * @return
   */
  public static String getMacAddress() throws Exception {
    NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());

    //if cannot get by localhost, we try to get first NetworkInterface
    if (null == network) {
      network = NetworkInterface.getByIndex(0);
    }

    byte[] mac;
    try {
      mac = network.getHardwareAddress();
    } catch (Exception e) {
      mac = new byte[0];
    }

    if (mac != null && mac.length <= 0) {
      //try to get mac address in AWS
      return HttpClientUtils.get(AWS_METADATA_MAC_URL, null).getBody();
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < mac.length; i++) {
      sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
    }
    return sb.toString();
  }

  /**
   * @param url
   * @param headers
   * @return
   * @throws ClientRequestException
   */
  public static ResponseEntity get(String url, Map<String, String> headers) throws ClientRequestException {
    HttpGet request = new HttpGet(url);
    addHeader(request, headers);
    return execute(request);
  }

  /**
   * @param url
   * @param headers
   * @param data
   * @return
   * @throws ClientRequestException
   */
  public static ResponseEntity post(String url, Map<String, String> headers, String data)
    throws ClientRequestException {
    return post(url, headers, data, ContentType.APPLICATION_JSON);
  }

  /**
   * @param url
   * @param headers
   * @param data
   * @param contentType
   * @return
   * @throws ClientRequestException
   */
  public static ResponseEntity post(String url, Map<String, String> headers, String data, ContentType contentType)
    throws ClientRequestException {
    HttpPost request = new HttpPost(url);
    addHeader(request, headers);
    if (!StringUtils.isEmpty(data))
      request.setEntity(new StringEntity(data, contentType));
    return execute(request);
  }

  /**
   * @param url
   * @param headers
   * @param data
   * @return
   * @throws ClientRequestException
   */
  public static ResponseEntity put(String url, Map<String, String> headers, String data) throws ClientRequestException {
    return put(url, headers, data, ContentType.APPLICATION_JSON);
  }

  /**
   * @param url
   * @param headers
   * @param data
   * @param contentType
   * @return
   * @throws ClientRequestException
   */
  public static ResponseEntity put(String url, Map<String, String> headers, String data, ContentType contentType)
    throws ClientRequestException {
    HttpPut request = new HttpPut(url);
    addHeader(request, headers);
    if (!StringUtils.isEmpty(data))
      request.setEntity(new StringEntity(data, contentType));
    return execute(request);
  }

  /**
   * @param url
   * @param headers
   * @return
   * @throws ClientRequestException
   */
  public static ResponseEntity delete(String url, Map<String, String> headers) throws ClientRequestException {
    return delete(url, headers, ContentType.APPLICATION_JSON);
  }

  /**
   * @param url
   * @param headers
   * @param contentType
   * @return
   * @throws ClientRequestException
   */
  public static ResponseEntity delete(String url, Map<String, String> headers, ContentType contentType)
    throws ClientRequestException {
    HttpDelete request = new HttpDelete(url);
    addHeader(request, headers);
    return execute(request);
  }

  private static ResponseEntity doExecute(HttpResponse response) throws Exception {
    if (null == response) {
      throw new Exception("response is null.");
    }
    HttpEntity entity = response.getEntity();
    BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));

    StringBuilder result = new StringBuilder();
    String line = "";
    while ((line = rd.readLine()) != null) {
      result.append(line);
    }
    return new ResponseEntity(result.toString(), response.getStatusLine().getStatusCode());
  }

  /**
   * Execute a request
   *
   * @param request
   * @return
   * @throws ClientRequestException
   */
  public static ResponseEntity execute(HttpUriRequest request) throws ClientRequestException {
    HttpClient client;
    try {
      client = getClient();
    } catch (Exception e) {
      throw new ClientRequestException("Cannot get HttpClient." + e.getMessage(), e);
    }
    HttpResponse response = null;
    ResponseEntity responseEntity;

    try {
      response = client.execute(request);
      responseEntity = doExecute(response);
    } catch (Exception e) {
      throw new ClientRequestException(e.getMessage(), e);
    } finally {
      if (null != response)
        org.apache.http.client.utils.HttpClientUtils.closeQuietly(response);
    }

    return responseEntity;
  }

  private static void addHeader(HttpRequestBase httpRequestBase, Map<String, String> headers) {
    if (headers != null) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        httpRequestBase.addHeader(entry.getKey(), entry.getValue());
      }
    }
  }

  public static HttpClient getHttpClient() throws Exception {
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
    SSLConnectionSocketFactory sslSocketFactory = getSslSocketFactory();
    httpClientBuilder.setSSLSocketFactory(sslSocketFactory)
      .setConnectionReuseStrategy(new NoConnectionReuseStrategy());
    httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(RETRY_MAX_COUNT, RETRY_REQUEST_SEND_RETRY_ENABLED) {
      @Override public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        if (executionCount > this.getRetryCount())
          return false;
        if (exception instanceof HttpHostConnectException)
          return true;
        return super.retryRequest(exception, executionCount, context);
      }
    });
    return httpClientBuilder.build();
  }

  private static SSLConnectionSocketFactory getSslSocketFactory()
    throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
    SSLContext sslContext = getSslContext();
    return new SSLConnectionSocketFactory(sslContext, ALLOW_ALL_HOSTNAME_VERIFIER);
  }

  private static SSLContext getSslContext() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
    SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    TrustStrategy trustStrategy = new TrustAllStrategy();
    sslContextBuilder.loadTrustMaterial(keyStore, trustStrategy);
    return sslContextBuilder.build();
  }

  /**
   * Trust all certificates.
   */
  public static class TrustAllStrategy implements TrustStrategy {

    @Override
    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
      return true;
    }
  }
}