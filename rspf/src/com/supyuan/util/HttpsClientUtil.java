package com.supyuan.util;

import java.util.ArrayList;  
import java.util.Iterator;  
import java.util.List;  
import java.util.Map;  
import java.util.Map.Entry;  
import org.apache.http.HttpEntity;  
import org.apache.http.HttpResponse;  
import org.apache.http.NameValuePair;  
import org.apache.http.client.HttpClient;  
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;  
import org.apache.http.util.EntityUtils;  
/* 
 * 利用HttpClient进行post请求的工具类 
 */  
public class HttpsClientUtil {  
    
	/**
	 * 发送数据通过后缀
	 * @param url
	 * @param map
	 * @param charset
	 * @return
	 */
	public static String doPost(String url,Map<String,String> map,String charset)
	{  
        HttpClient httpClient = null;  
        HttpPost httpPost = null;  
        String result = null;  
        try{  
            httpClient = new SSLClient();  
            httpPost = new HttpPost(url);  
            //设置参数  
            List<NameValuePair> list = new ArrayList<NameValuePair>();  
            Iterator iterator = map.entrySet().iterator();  
            while(iterator.hasNext()){  
                Entry<String,String> elem = (Entry<String, String>) iterator.next();  
                list.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));  
            }  
            if(list.size() > 0){  
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,charset);  
                httpPost.setEntity(entity);  
            }  
            HttpResponse response = httpClient.execute(httpPost);  
            if(response != null){  
                HttpEntity resEntity = response.getEntity();  
                if(resEntity != null){  
                    result = EntityUtils.toString(resEntity,charset);  
                }  
            }  
        }catch(Exception ex){  
            ex.printStackTrace();  
        }  
        return result;  
    } 
	
	/**
	 * 发送数据通过xml
	 * @param apiUrl
	 * @param xml
	 * @return
	 */
	public static String doPost(String apiUrl,String xml)
	{  
        HttpClient httpClient = null;  
        HttpPost httpPost = null;  
        String result = null;  
        try{  
            httpClient = new SSLClient();  
            httpPost = new HttpPost(apiUrl);
            StringEntity stringEntity = new StringEntity(xml.toString(), "UTF-8");// 解决中文乱码问题
			stringEntity.setContentEncoding("UTF-8");
			stringEntity.setContentType("application/xml");
			httpPost.setEntity(stringEntity);
			HttpResponse response = httpClient.execute(httpPost);  
            if(response != null)
            {  
                HttpEntity resEntity = response.getEntity();  
                if(resEntity != null)
                {  
                    result = EntityUtils.toString(resEntity, "UTF-8");  
                }
            }
        }catch(Exception ex){  
            System.out.println("发送失败："+ex.getMessage());
        }
        return result;  
    }
}  