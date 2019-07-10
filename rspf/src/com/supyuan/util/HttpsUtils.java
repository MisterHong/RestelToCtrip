package com.supyuan.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
 
public class HttpsUtils {
	private static PoolingHttpClientConnectionManager	connMgr;
	private static RequestConfig				requestConfig;
	private static final int				MAX_TIMEOUT	= 7000;
	
	static {
		// 设置连接池
		connMgr = new PoolingHttpClientConnectionManager();
		// 设置连接池大小
		connMgr.setMaxTotal(100);
		connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());
		// Validate connections after 1 sec of inactivity
		connMgr.setValidateAfterInactivity(1000);
		RequestConfig.Builder configBuilder = RequestConfig.custom();
		// 设置连接超时
		configBuilder.setConnectTimeout(MAX_TIMEOUT);
		// 设置读取超时
		configBuilder.setSocketTimeout(MAX_TIMEOUT);
		// 设置从连接池获取连接实例的超时
		configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
 
		requestConfig = configBuilder.build();
	}

	/**
	 * 发送 POST 请求，JSON形式
	 * 
	 * @param apiUrl
	 * @param String
	 *  String对象
	 * @return
	 */
	public static String doPost(String apiUrl, String content) {
		CloseableHttpClient httpClient = null;
		if (apiUrl.startsWith("https")) {
			httpClient = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory())
					.setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
		} else {
			httpClient = HttpClients.createDefault();
		}
		String httpStr = null;
		HttpPost httpPost = new HttpPost(apiUrl);
		CloseableHttpResponse response = null;
		try {
			httpPost.setConfig(requestConfig);
			StringEntity stringEntity = new StringEntity(content.toString(), "UTF-8");// 解决中文乱码问题
			stringEntity.setContentEncoding("UTF-8");
			stringEntity.setContentType("application/xml");
			httpPost.setEntity(stringEntity);
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			httpStr = EntityUtils.toString(entity, "UTF-8");
		} catch (Exception e) {
			System.err.println("发送失败："+e.getMessage());
		} finally {
			if (response != null) {
				try {
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}
		return httpStr;
	}
 
	/**
	 * 创建SSL安全连接
	 * 
	 * @return
	 */
	private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
		SSLConnectionSocketFactory sslsf = null;
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
 
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();
			sslsf = new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
 
				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			});
		} catch (GeneralSecurityException e) {
			System.out.println(e.getMessage());
		}
		return sslsf;
	}
	
	
	public static void main(String[] args) {
		String  url = "https://vendor-ctrip.fws.ctripqa.com/Hotel/OTAReceive/HotelStaticInfoService.asmx";
		String content = "<OTA_HotelDescriptiveContentNotifRQ Target=\"Production\" PrimaryLangID=\"en-us\" Version=\"1.0\" TimeStamp=2019-04-01T17:34:34Z xmlns=\"http://www.opentravel.org/OTA/2003/05\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">  <POS>    <Source>    <RequestorID ID=\"hangye_alex\" MessagePassword=\"hangye_alex_101019\" Type=\"1\">    <CompanyName Code=\"C\" CodeContext=\"1\" />    </RequestorID>    </Source>  </POS>  <HotelDescriptiveContents  HotelCode=067625>    <HotelDescriptiveContent>      <HotelInfo HotelStatus=\"Active\">        <Descriptions>          <MultimediaDescriptions>            <MultimediaDescription>              <TextItems>                <TextItem Language=\"en-us\" Title=CORAL AL KHOBAR HOTEL>                </TextItem>              </TextItems>            </MultimediaDescription>           </MultimediaDescriptions>         </Descriptions>         <CategoryCodes>           <GuestRoomInfo Quantity=131 />         </CategoryCodes>         <Position Longitude=50.186539 Latitude=26.286273 />      </HotelInfo>      <ContactInfos>         <ContactInfo>          <Addresses>            <Address Language=\"en-us\">            <CityName>AL KHOBAR</CityName>            </Address>          </Addresses>    </HotelDescriptiveContent>  </HotelDescriptiveContents></OTA_HotelDescriptiveContentNotifRQ>";
		String res = doPost(url,content);
		System.out.println(res);
	}
	
}