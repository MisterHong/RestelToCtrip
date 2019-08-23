package com.supyuan.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.supyuan.system.hotel.HotelModel;
import com.supyuan.system.hotel.Lin;
import com.supyuan.system.hotel.Reg;
import com.supyuan.system.hotel.RomeStay;
import com.supyuan.system.hotel.RoomInfo;
import com.supyuan.system.hotel.RoomType;
import com.supyuan.system.hotel.SubType;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class HttpUtils {

	private static final Logger log = Logger.getLogger(HttpUtils.class);
	
	@SuppressWarnings("unused")
	private static class TrustAnyTrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }
 
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }
 
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }
	}

	private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
	}

	/**
	 * Restel xml请求
	 * @param xml_info
	 * @return
	 * @throws Exception
	 */
	public static String HttpClientPost(String xml_info) throws Exception {
		//获取配置参数
		String api_url = Config.getStr("api_url");
		String codigousu = Config.getStr("codigousu");
		String clausu = Config.getStr("clausu");
		String afiliacio = Config.getStr("afiliacio");
		String secacc = Config.getStr("secacc");
		// 获取默认的请求客户端
		CloseableHttpClient client = HttpClients.createDefault();
		// 通过HttpPost来发送post请求
		HttpPost httpPost = new HttpPost(api_url);
		httpPost.addHeader("Connection", "Keep-Alive");
		// 构造list集合 post 数据
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		BasicNameValuePair basicNameValuePairCodu = new BasicNameValuePair("codigousu", codigousu);
		BasicNameValuePair basicNameValuePairClsu = new BasicNameValuePair("clausu", clausu);
		BasicNameValuePair basicNameValuePairAfi = new BasicNameValuePair("afiliacio", afiliacio);
		BasicNameValuePair basicNameValuePairSec = new BasicNameValuePair("secacc", secacc);
		BasicNameValuePair basicNameValuePairPram = new BasicNameValuePair("xml", xml_info);
		params.add(basicNameValuePairCodu);
		params.add(basicNameValuePairClsu);
		params.add(basicNameValuePairAfi);
		params.add(basicNameValuePairSec);
		params.add(basicNameValuePairPram);
		// 我们发现Entity是一个接口，所以只能找实现类，发现实现类又需要一个集合，集合的泛型是NameValuePair类型
		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params);
		// 通过setEntity 将我们的entity对象传递过去
		httpPost.setEntity(formEntity);
		// 通过client来执行请求，获取一个响应结果
		CloseableHttpResponse response = client.execute(httpPost);
		HttpEntity entity = response.getEntity();
		String result = EntityUtils.toString(entity, "UTF-8");
		response.close();
		return result;
	}
	
	 /**
     * 解析 hotels 17（XML）
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static boolean ParseHotelsXml(InputStream inputStream) throws Exception {
        if (inputStream == null){
            return false;
        }
        SAXReader reader = new SAXReader();// 读取输入流
        Document document = reader.read(inputStream);
        inputStream.close();
        Element root = document.getRootElement();// 得到xml根元素
        Element parametrosElem = root.element("parametros");// 获取parametros节点
        Element hotelesElem = parametrosElem.element("hoteles");// 获取paises节点
        Record hotelEntry = null;
		@SuppressWarnings("unchecked")
		List<Element> elementList = hotelesElem.elements();// 得到根元素的所有子节点
        for (Element element : elementList) {        // 遍历hoteles所有子节点
        	Element codesthotElem = element.element("codesthot");// 获取codigo_pais节点
        	Element codpobhotElem = element.element("codpobhot");// 获取codigo_pais节点
        	Element hot_codigoElem = element.element("hot_codigo");// 获取codigo_pais节点
        	Element hot_codcobolElem = element.element("hot_codcobol");// 获取codigo_pais节点
        	Element hot_coddupElem = element.element("hot_coddup");// 获取codigo_pais节点
        	Element hot_afiliacionElem = element.element("hot_afiliacion");// 获取codigo_pais节点        	
        	String hotel_code = hot_codigoElem.getTextTrim();//.replace("<![CDATA[", "").replace("]]>", "")
        	//不为空，且为数字
			if(!hotel_code.equals("") && StringUtils.isNumeric(hotel_code) && Integer.parseInt(hotel_code) > 0)
			{
				hotelEntry = Db.findFirst("select hot_codigo from sys_hotels where hot_codigo = ?", hotel_code);
				if(null == hotelEntry)
				{
					Db.batch("insert into sys_hotels(codesthot,codpobhot,hot_codigo,hot_codcobol,hot_coddup,hot_afiliacion,status) values(?,?,?,?,?,?,0)", new Object[][]{{codesthotElem.getTextTrim(),codpobhotElem.getTextTrim(),hotel_code,hot_codcobolElem.getTextTrim(),hot_coddupElem.getTextTrim(),hot_afiliacionElem.getTextTrim()}}, 500);
				} 
			}
			hotelEntry = null;
        }
        return true;
    }
	
    /**
     * 解析酒店详情 15（XML）
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static HotelModel ParseHotelInfoXml(InputStream inputStream) throws Exception {
        if (inputStream == null){
            return null;
        }
        HotelModel model = new HotelModel();
        model.setCurrency("USD");//默认结算货币为美元：
        SAXReader reader = new SAXReader();// 读取输入流
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();// 得到xml根元素
        Element parametrosElem = root.element("parametros");//获取parametros节点
        Element hotelElem = parametrosElem.element("hotel");//获取hotel节点
        @SuppressWarnings("unchecked")
		List<Element> elementList = hotelElem.elements();// 得到根元素的所有子节点
        for (Element element : elementList) {        // 遍历hotel所有子节点
        	String text = element.getTextTrim();
        	if(element.getName().equals("pais"))
			{
        		model.setPais(text);
			}
			else if(element.getName().equals("codigo_hotel"))
			{
				model.setCodigo_hotel(text);
			}
			else if(element.getName().equals("codigo"))
			{
				model.setCodigo(text);
			}
			else if(element.getName().equals("hot_afiliacion"))
			{
				model.setHot_afiliacion(text);
			}
			else if(element.getName().equals("nombre_h"))
			{
				model.setNombre_h(text);
			}
			else if(element.getName().equals("direccion"))
			{
				model.setDireccion(text);
			}
			else if(element.getName().equals("codprovincia"))
			{
				model.setCodprovincia(text);
			}
			else if(element.getName().equals("provincia"))
			{
				model.setProvincia(text);
			}
			else if(element.getName().equals("codpoblacion"))
			{
				model.setCodpoblacion(text);
			}
			else if(element.getName().equals("poblacion"))
			{
				model.setPoblacion(text);
			}
			else if(element.getName().equals("cp"))
			{
				model.setCp(text);
			}
			else if(element.getName().equals("mail"))
			{
				model.setMail(text);
			}
			else if(element.getName().equals("web"))
			{
				model.setWeb(text);
			}
			else if(element.getName().equals("telefono"))
			{
				model.setTelefono(text);
			}
			else if(element.getName().equals("fotos"))
			{
				String fotosStr = "";
				@SuppressWarnings("unchecked")
				List<Element> fotos = element.elements();
				if(fotos != null && fotos.size() > 0)
				{
					for (Element foto : fotos) {
						fotosStr += foto.getTextTrim() + ",";
					}
				}
				model.setFotos(fotosStr);
			}
			else if(element.getName().equals("plano"))
			{
				model.setPlano(text);
			}
			else if(element.getName().equals("desc_hotel"))
			{
				model.setDesc_hotel(text);
			}
			else if(element.getName().equals("num_habitaciones"))
			{
				model.setNum_habitaciones(text);
			}
			else if(element.getName().equals("como_llegar"))
			{
				model.setComo_llegar(text);
			}
			else if(element.getName().equals("tipo_establecimiento"))
			{
				model.setTipo_establecimiento(text);
			}
			else if(element.getName().equals("categoria"))
			{
				model.setCategoria(text);
			}
			else if(element.getName().equals("checkin"))
			{
				model.setCheckin(text);
			}
			else if(element.getName().equals("checkout"))
			{
				model.setCheckout(text);
			}
			else if(element.getName().equals("edadnindes"))
			{
				model.setEdadnindes(text);
			}
			else if(element.getName().equals("edadninhas"))
			{
				model.setEdadninhas(text);
			}
			else if(element.getName().equals("currency"))
			{
				model.setCurrency(text);
			}
			else if(element.getName().equals("longitud"))
			{
				model.setLongitude(text);
			}
			else if(element.getName().equals("latitud"))
			{
				model.setLatitude(text);
			}
        }
        inputStream.close();
        inputStream = null;
        return model;
    }
    
    /**
     * 解析房间价格
     * @param inputStream
     * @param hotelcode
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public static String Parse110XmlWithRR(InputStream inputStream,String hotelcode,String rtcode,String rpcode,String rtdesc,double usdrate,double tj,String ctrip_api_url) throws Exception
    {
    	 if (inputStream == null){
             return "";
         }
     	 SAXReader reader = new SAXReader();// 读取输入流
         Document document = reader.read(inputStream);
         inputStream.close();
         Element root = document.getRootElement();// 得到xml根元素
         Element paramElem = root.element("param");// 获取param节点
         if(null != paramElem.element("error"))
         {
        	 log.error("Parse110XmlWithRR访问Restel接口出现错误！"+paramElem.element("error").asXML());
        	 return "";
         }
         Element hotlsElem = paramElem.element("hotls");// 获取hotls 节点
         if(hotlsElem.attribute("num").getText().equals("0"))
         {
         	return "";
         }
         Element hot = hotlsElem.element("hot");
         Element res = hot.element("res");
         Element pax = res.element("pax");
         List<Element> paxlist = pax.elements("hab");
         for (Element element : paxlist) 
         {
        	 if(element.attribute("cod").getText().equals(rtcode) && rtdesc.equals(element.attribute("desc").getText()))
        	 {
        		 List<Element> reglist = element.elements("reg");
            	 for (Element element2 : reglist) {
            		 if(element2.attribute("cod").getText().equals(rpcode))
            		 {
        				 List<Element> linlist = element2.elements("lin");
            			 //element2.attribute("prr").getText();
            			 if(null != linlist && linlist.size() > 0)
            			 {
            				 for (Element lin : linlist) 
            				 {
            					 String prr = lin.getTextTrim().split("#")[3];
            					 double price = Double.parseDouble(prr);
            					 String start = lin.getTextTrim().split("#")[7];
            					 String end = lin.getTextTrim().split("#")[8];
            					 DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            					 Date startDate = DateUtils.StrToDate(start);
        				         start = format.format(startDate);
        				         end = format.format(DateUtils.StrToDate(end));
        				         int mealCount = 2;
     				            boolean isBF = false;
     				            boolean isLun = false;
     				            boolean isBD = false;
     				            if ((!rpcode.equals("RO")) && (!rpcode.equals("OB"))) {
     				              rpcode.equals("SA");
     				            }
     				            if ((rpcode.equals("FB")) || (rpcode.equals("PC")) || (rpcode.equals("AI")) || (rpcode.equals("TI")))
     				            {
     				              isBD = true;
     				              isBF = true;
     				              isLun = true;
     				            }
     				            if ((rpcode.equals("BB")) || (rpcode.equals("AD"))) {
     				              isBF = true;
     				            }
     				            if ((rpcode.equals("HB")) || (rpcode.equals("MP")))
     				            {
     				              isBF = true;
     				              isBD = true;
     				            }
								 StringBuffer buffer = new StringBuffer();
								 price = AmountUtil.multiply(price, usdrate, 2); //乘以汇率
								 price = AmountUtil.add(price, tj, 2);	//加上抬价
								 buffer.append("    		<Rate Start=\""+start+"\" End=\""+end+"\" LOS=\"0\">");
								 buffer.append("    			<BaseByGuestAmts>");
								 buffer.append("    				<BaseByGuestAmt Code=\"Sell\" AmountBeforeTax=\""+price+"\" AmountAfterTax=\""+price+"\" CurrencyCode=\"CNY\" />");
								 buffer.append("    			</BaseByGuestAmts>");
								 buffer.append("    			<MealsIncluded Breakfast=\"" + (isBF ? "true" : "false") + "\" Lunch=\"" + (isLun ? "true" : "false") + "\" Dinner=\"" + (isBD ? "true" : "false") + "\" NumberOfMeal=\"" + mealCount + "\" />");
								 buffer.append("    		</Rate>");
								 String xmlRP = HttpUtils.PushCtripXmlRoomPrice(hotelcode, rpcode, rtcode, buffer.toString());
								 String resultRP = HttpsUtils.doPost(ctrip_api_url, xmlRP);
								 InputStream streamRP = new ByteArrayInputStream(resultRP.getBytes());
								 if(HttpUtils.ParseCtripCMXml(streamRP).contains("Success"))
								 {
									Date now = new Date();
							        SimpleDateFormat fs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							        String dateStr = fs.format(now);
							        Record hotelRate = Db.findFirst("select id from sys_hotels_roomrate where hotelcode = '"+hotelcode+"' and rtcode = '"+rtcode+"' and rpcode = '"+rpcode+"' and timestamp = "+startDate.getTime());
							        String sql = "";
							        if(null != hotelRate)
							        {
							        	  sql = "update sys_hotels_roomrate set price="+price+",rprice="+prr+",rate="+usdrate+",tj="+tj+",altertime='"+dateStr+"' where hotelcode='"+hotelcode+"' and rtcode='"+rtcode+"' and rpcode='"+rpcode+"' and timestamp="+startDate.getTime();
							        }
							        else
							        {
							        	sql = "insert into sys_hotels_roomrate(hotelcode,timestamp,rtcode,rpcode,start,end,rate,tj,rprice,price,createtime,altertime) "
					            				+ "values('"+hotelcode+"',"+startDate.getTime()+",'"+rtcode+"','"+rpcode+"','"+start+"','"+end+"',"+usdrate+","+tj+","+prr+","+price+",'"+dateStr+"','"+dateStr+"')";
							        }
							        Db.update(sql);
								 }
							 }
            			 }
            		 }
            	 }	 
        	 }
         }
         return "";
    }
    
    /**
     * 解析并推送房间价格
     * @param inputStream
     * @param hotelcode
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public static String Parse110XmlWithPushPrice(InputStream inputStream,String hotelcode,String rtcode,String rpcode,String rtdesc,double usdrate,double tj,String ctrip_api_url) throws Exception
    {
    	 if (inputStream == null){
             return "";
         }
     	 SAXReader reader = new SAXReader();// 读取输入流
         Document document = reader.read(inputStream);
         inputStream.close();
         Element root = document.getRootElement();// 得到xml根元素
         Element paramElem = root.element("param");// 获取param节点
         if(null != paramElem.element("error"))
         {
        	 log.error("Parse110XmlWithPushPrice访问Restel接口出现错误！"+paramElem.element("error").asXML());
        	 return "";
         }
         Element hotlsElem = paramElem.element("hotls");// 获取hotls 节点
         if(hotlsElem.attribute("num").getText().equals("0"))
         {
         	return "";
         }
         Element hot = hotlsElem.element("hot");
         Element res = hot.element("res");
         Element pax = res.element("pax");
         List<Element> paxlist = pax.elements("hab");
         for (Element element : paxlist) 
         {
        	 if(element.attribute("cod").getText().equals(rtcode) && rtdesc.equals(element.attribute("desc").getText()))
        	 {
        		 log.info("当前code,描述"+element.attribute("cod").getText()+"\t"+element.attribute("desc").getText());
        		 List<Element> reglist = element.elements("reg");
            	 for (Element element2 : reglist) {
            		 if(element2.attribute("cod").getText().equals(rpcode))
            		 {
        				 List<Element> linlist = element2.elements("lin");
            			 if(null != linlist && linlist.size() > 0)
            			 {
            				 for (Element lin : linlist) 
            				 {
            					 String prr = lin.getTextTrim().split("#")[3];
            					 double price = Double.parseDouble(prr);
            					 String start = lin.getTextTrim().split("#")[7];
            					 String end = lin.getTextTrim().split("#")[8];
            					 DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            					 Date startDate = DateUtils.StrToDate(start);
        				         start = format.format(startDate);
        				         end = format.format(DateUtils.StrToDate(end));
        				         int mealCount = 2;
    				             boolean isBF = false;
    				             boolean isLun = false;
    				             boolean isBD = false;
    				             if ((!rpcode.equals("RO")) && (!rpcode.equals("OB"))) {
    				               rpcode.equals("SA");
    				             }
    				             if ((rpcode.equals("FB")) || (rpcode.equals("PC")) || (rpcode.equals("AI")) || (rpcode.equals("TI")))
    				             {
    				               isBD = true;
    				               isBF = true;
    				               isLun = true;
    				             }
    				             if ((rpcode.equals("BB")) || (rpcode.equals("AD"))) {
    				               isBF = true;
    				             }
    				             if ((rpcode.equals("HB")) || (rpcode.equals("MP")))
    				             {
    				               isBF = true;
    				               isBD = true;
    				             }
								 StringBuffer buffer = new StringBuffer();
								 price = AmountUtil.multiply(price, usdrate, 2); //乘以汇率
								 price = AmountUtil.add(price, tj, 2);	//加上抬价
								 buffer.append("    		<Rate Start=\"" + start + "\" End=\"" + end + "\" LOS=\"0\">");
								 buffer.append("    			<BaseByGuestAmts>");
								 buffer.append("    				<BaseByGuestAmt Code=\"Sell\" AmountBeforeTax=\"" + price + "\" AmountAfterTax=\"" + price + "\" CurrencyCode=\"CNY\" />");
								 buffer.append("    			</BaseByGuestAmts>");
								 buffer.append("    			<MealsIncluded Breakfast=\"" + (isBF ? "true" : "false") + "\" Lunch=\"" + (isLun ? "true" : "false") + "\" Dinner=\"" + (isBD ? "true" : "false") + "\" NumberOfMeal=\"" + mealCount + "\" />");
								 buffer.append("    		</Rate>");
								 String xmlRP = HttpUtils.PushCtripXmlRoomPrice(hotelcode, rpcode, rtcode, buffer.toString());
								 log.info("酒店："+hotelcode+"，携程房价请求报文："+xmlRP);
								 String resultRP = HttpsUtils.doPost(ctrip_api_url, xmlRP);
								 log.info("酒店："+hotelcode+"，携程房价响应报文："+resultRP);
								 InputStream streamRP = new ByteArrayInputStream(resultRP.getBytes());
								 if(HttpUtils.ParseCtripCMXml(streamRP).contains("Success"))
								 {
									log.info("酒店："+hotelcode+"，房价推送成功Success");
									Date now = new Date();
							        SimpleDateFormat fs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							        String dateStr = fs.format(now);
							        Record hotelRate = Db.findFirst("select id from sys_hotels_roomrate where hotelcode = '"+hotelcode+"' and rtcode = '"+rtcode+"' and rpcode = '"+rpcode+"' and timestamp = "+startDate.getTime());
							        String sql = "";
							        if(null != hotelRate)
							        {
							        	sql = "update sys_hotels_roomrate set price="+price+",rprice="+prr+",rate="+usdrate+",tj="+tj+",altertime='"+dateStr+"' where hotelcode='"+hotelcode+"' and rtcode='"+rtcode+"' and rpcode='"+rpcode+"' and timestamp="+startDate.getTime();							        	log.info("推送酒店"+hotelcode+"---更新房价:"+sql);
							        }
							        else
							        {
							        	sql = "insert into sys_hotels_roomrate(hotelcode,timestamp,rtcode,rpcode,start,end,rate,tj,rprice,price,createtime,altertime) "
					            				+ "values('"+hotelcode+"',"+startDate.getTime()+",'"+rtcode+"','"+rpcode+"','"+start+"','"+end+"',"+usdrate+","+tj+","+prr+","+price+",'"+dateStr+"','"+dateStr+"')";
							        	log.info("推送酒店"+hotelcode+"---增加房价:"+sql);
							        }
							        Db.update(sql);
								 }
							 }
            			 }
            		 }
            	 }	 
        	 }
         }
         return "";
    }
    
    
    /**
     * 解析酒店详情 15（XML）
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static HotelModel ParseHotelModelXml(InputStream inputStream) throws Exception {
        if (inputStream == null){
            return null;
        }
        HotelModel model = new HotelModel();
        SAXReader reader = new SAXReader();// 读取输入流
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();// 得到xml根元素
        Element parametrosElem = root.element("parametros");//获取parametros节点
        Element paisesElem = parametrosElem.element("paises");//获取paises节点
        Element hotelElem = paisesElem.element("hotel");//获取hotel节点
        @SuppressWarnings("unchecked")
		List<Element> elementList = hotelElem.elements();// 得到根元素的所有子节点
        for (Element element : elementList) {        // 遍历hotel所有子节点
        	String text = element.getTextTrim();
        	if(element.getName().equals("pais"))
			{
        		model.setPais(text);
			}
			else if(element.getName().equals("codigo_hotel"))
			{
				model.setCodigo_hotel(text);
			}
			else if(element.getName().equals("codigo"))
			{
				model.setCodigo(text);
			}
			else if(element.getName().equals("hot_afiliacion"))
			{
				model.setHot_afiliacion(text);
			}
			else if(element.getName().equals("nombre_h"))
			{
				model.setNombre_h(text);
			}
			else if(element.getName().equals("direccion"))
			{
				model.setDireccion(text);
			}
			else if(element.getName().equals("codprovincia"))
			{
				model.setCodprovincia(text);
			}
			else if(element.getName().equals("provincia"))
			{
				model.setProvincia(text);
			}
			else if(element.getName().equals("codpoblacion"))
			{
				model.setCodpoblacion(text);
			}
			else if(element.getName().equals("poblacion"))
			{
				model.setPoblacion(text);
			}
			else if(element.getName().equals("cp"))
			{
				model.setCp(text);
			}
			else if(element.getName().equals("coddup"))
			{
				//model.setcod
			}
			else if(element.getName().equals("mail"))
			{
				model.setMail(text);
			}
			else if(element.getName().equals("web"))
			{
				model.setWeb(text);
			}
			else if(element.getName().equals("telefono"))
			{
				model.setTelefono(text);
			}
			else if(element.getName().equals("fotos"))
			{
				String fotosStr = "";
				@SuppressWarnings("unchecked")
				List<Element> fotos = element.elements();
				if(fotos != null && fotos.size() > 0)
				{
					for (Element foto : fotos) {
						fotosStr += foto.getTextTrim() + ",";
					}
				}
				model.setFotos(fotosStr);
			}
			else if(element.getName().equals("plano"))
			{
				model.setPlano(text);
			}
			else if(element.getName().equals("desc_hotel"))
			{
				model.setDesc_hotel(text);
			}
			else if(element.getName().equals("num_habitaciones"))
			{
				model.setNum_habitaciones(text);
			}
			else if(element.getName().equals("como_llegar"))
			{
				model.setComo_llegar(text);
			}
			else if(element.getName().equals("tipo_establecimiento"))
			{
				model.setTipo_establecimiento(text);
			}
			else if(element.getName().equals("categoria"))
			{
				model.setCategoria(text);
			}
			else if(element.getName().equals("categoria2"))
			{
				//
			}
			else if(element.getName().equals("logo_h"))
			{
				
			}
			else if(element.getName().equals("checkin"))
			{
				model.setCheckin(text);
			}
			else if(element.getName().equals("checkout"))
			{
				model.setCheckout(text);
			}
			else if(element.getName().equals("edadnindes"))
			{
				model.setEdadnindes(text);
			}
			else if(element.getName().equals("edadninhas"))
			{
				model.setEdadninhas(text);
			}
			else if(element.getName().equals("currency"))
			{
				model.setCurrency(text);
			}
			else if(element.getName().equals("marca"))
			{
				//
			}
			else if(element.getName().equals("longitud"))
			{
				//
			}
			else if(element.getName().equals("latitud"))
			{
				//
			}
        }
        inputStream.close();
        inputStream = null;
        return model;
    }
	
    /**
     * 解析国家列表 5（XML）
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static boolean ParseCountrysXml(InputStream inputStream) throws Exception {
    	 if (inputStream == null){
             return false;
         }
         SAXReader reader = new SAXReader();// 读取输入流
         Document document = reader.read(inputStream);
         Element root = document.getRootElement();// 得到xml根元素
         Element parametrosElem = root.element("parametros");// 获取parametros节点
         Element paisesElem = parametrosElem.element("paises");// 获取paises节点
         Record country = null;
         @SuppressWarnings("unchecked")
 		 List<Element> elementList = paisesElem.elements();// 得到根元素的所有子节点
         for (Element element : elementList) {        // 遍历hoteles所有子节点
         	Element codigo_paisElem = element.element("codigo_pais");// 获取codigo_pais节点 Two-digit country code
         	Element nombre_paisElem = element.element("nombre_pais");// 获取nombre_pais节点
         	String codigo = codigo_paisElem.getTextTrim();
         	country = Db.findFirst("select codigo_pais from sys_country where codigo_pais = ?", codigo);
			if(null == country)
			{
				Db.batch("insert into sys_country(codigo_pais,nombre_pais) values(?,?)", new Object[][]{{codigo,nombre_paisElem.getTextTrim()}}, 100);
			} 
			country = null;
         }
         return true;
    }
    
    /**
     * 解析省列表 6（XML）
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static boolean ParseProvinceXml(InputStream inputStream) throws Exception {
    	if (inputStream == null){
            return false;
        }
        SAXReader reader = new SAXReader();// 读取输入流
        Document document = reader.read(inputStream);
        inputStream.close();
        Element root = document.getRootElement();// 得到xml根元素
        Element parametrosElem = root.element("parametros");// 获取parametros节点
        Element paisesElem = parametrosElem.element("provincias");// 获取paises节点
        Record province = null;
        @SuppressWarnings("unchecked")
		 List<Element> elementList = paisesElem.elements();// 得到根元素的所有子节点
        for (Element element : elementList) {        // 遍历hoteles所有子节点
        	Element codigo_paisElem = element.element("codigo_pais");// 获取codigo_pais节点 Two-digit country code
        	Element codigo_provinciaElem = element.element("codigo_provincia");// 获取nombre_pais节点
        	Element nombre_provinciaElem = element.element("nombre_provincia");// 获取nombre_pais节点
        	String codigo_pais = codigo_paisElem.getTextTrim();
        	String codigo_provincia = codigo_provinciaElem.getTextTrim();
        	province = Db.findFirst("select codigo_pais from sys_province where codigo_pais = ? and codigo_provincia = ?", codigo_pais,codigo_provincia);
			if(null == province)
			{
				Db.batch("insert into sys_province(codigo_pais,codigo_provincia,nombre_provincia) values(?,?,?)", new Object[][]{{codigo_pais,codigo_provincia,nombre_provinciaElem.getTextTrim()}}, 100);
			}
			province = null;
        }
        return true;
    }

    
    /**
     * 解析城市列表 18（XML） 暂未启用
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static boolean ParseCityXml(InputStream inputStream) throws Exception {
    	if (inputStream == null){
            return false;
        }
        SAXReader reader = new SAXReader();// 读取输入流
        Document document = reader.read(inputStream);
        inputStream.close();
        Element root = document.getRootElement();// 得到xml根元素
        Element parametrosElem = root.element("parametros");// 获取parametros节点
        Element paisesElem = parametrosElem.element("paises");// 获取paises节点
        Record city = null;
        @SuppressWarnings("unchecked")
		 List<Element> elementList = paisesElem.elements();// 得到根元素的所有子节点
        for (Element element : elementList) {        // 遍历hoteles所有子节点
        	Element codigo_paisElem = element.element("codigo_pais");// 获取codigo_pais节点 Two-digit country code
        	Element nombre_paisElem = element.element("nombre_pais");// 获取nombre_pais节点
        	String codigo = codigo_paisElem.getTextTrim();
        	city = Db.findFirst("select codigo_pais from sys_country where codigo_pais = ?", codigo);
			if(null == city)
			{
				Db.batch("insert into sys_country(codigo_pais,nombre_pais) values(?,?)", new Object[][]{{codigo,nombre_paisElem.getTextTrim()}}, 100);
			} 
			city = null;
        }
        return true;
    }

    /**
     * 解析测试XMl
     * @param inputStream
     * @throws Exception
     */
    public static void ParseTestXml(InputStream inputStream) throws Exception
    {
    	if (inputStream == null){
            return;
        }
        SAXReader reader = new SAXReader();// 读取输入流
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();// 得到xml根元素
        System.out.println(root.asXML());
    }
    
    /**
     * 解析xml110
     * @param inputStream
     * @param Hotel
     * @param type
     * @return
     * @throws Exception
     */
    public static List<RoomInfo> Parse110Xml(InputStream inputStream) throws Exception
    {
    	if (inputStream == null){
            return null;
        }
        SAXReader reader = new SAXReader();// 读取输入流
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();// 得到xml根元素
        Element paramElem = root.element("param");// 获取param节点
        Element hotlsElem = paramElem.element("hotls");// 获取hotls 节点
        List<RoomInfo> roomInfoList = new ArrayList<RoomInfo>();
        @SuppressWarnings("unchecked")
		List<Element> elementList = hotlsElem.elements();// 得到根元素的所有子节点
        for (Element element : elementList) {        // 遍历hoteles所有子节点
        	Element cod_Elem = element.element("cod");// 获取cod节点
        	Element nom_Elem = element.element("nom");// 获取nom节点
        	Element pro_Elem = element.element("pro");// 获取nom节点
        	Element res_Elem = element.element("res");// 获取nom节点
        	RoomInfo roomInfo = new RoomInfo(cod_Elem.getTextTrim(),nom_Elem.getTextTrim(),pro_Elem.getTextTrim(),null); 
        		Element pax_Elem = res_Elem.element("pax");
        			List<RoomType> roomTypes = new ArrayList<>();
        			@SuppressWarnings("unchecked")
					List<Element> habList = pax_Elem.elements();
        			for (Element hab : habList) {
        				String hab_cod = hab.attribute("cod").toString();
        				String hab_desc = hab.attribute("desc").toString();
        				RoomType roomType = new RoomType(hab_cod,hab_desc,null);
        				List<SubType> subTypes = new ArrayList<>();
        				@SuppressWarnings("unchecked")
						List<Element> regList = hab.elements("reg"); //子房型
        				for (Element reg : regList) {
	        				String reg_cod = reg.attribute("cod").toString();
	        				String reg_prr = reg.attribute("prr").toString();
	        				String reg_pvp = reg.attribute("pvp").toString();
	        				String reg_div = reg.attribute("div").toString();
	        				Element lin_Elem = reg.element("lin");
	        				subTypes.add(new SubType(reg_cod,reg_prr,reg_pvp,reg_div,lin_Elem.getTextTrim()));
        				}
        				roomType.setSubTypeList(subTypes);
        				roomTypes.add(roomType);
        			}
        			roomInfo.setRoomtype(roomTypes);
        			roomInfoList.add(roomInfo);
        }
        return roomInfoList;
    }
    
    /**
     * k可用性检查,返回List<RomeStay>
     * @param inputStream
     * @param Hotel
     * @param type
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked", "static-access" })
	public static List<RomeStay> Parse110XmlWithRS(InputStream inputStream,String rtcode,String rtdesc,String rpcode,int difference,String count,double usdrate,double tj,String start) throws Exception
    {
    	if (inputStream == null){
            return null;
        }
        SAXReader reader = new SAXReader();// 读取输入流
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();// 得到xml根元素
        Element paramElem = root.element("param");// 获取param节点
        Element hotlsElem = paramElem.element("hotls");// 获取hotls 节点
        if(hotlsElem.attribute("num").getText().equals("0"))
        {
        	return null;
        }
        Element hot = hotlsElem.element("hot");
        String hotelCode = hot.element("cod").getTextTrim();
        Element res = hot.element("res");
        Element pax = res.element("pax");
        List<Element> paxlist = pax.elements("hab");
        List<RomeStay> romeStayList = new ArrayList<RomeStay>(); 
        for (Element element : paxlist) 
        {
        	 if(element.attribute("cod").getText().equals(rtcode)  && rtdesc.equals(element.attribute("desc").getText()))
        	 {
        		 RomeStay romeStay = new RomeStay();
            	 romeStay.setRtCode(element.attribute("cod").getText());
        		 List<Element> reglist = element.elements("reg");
        		 List<Reg> regList = new ArrayList<Reg>();
        		 for (Element element2 : reglist) 
        		 {
        			 if(element2.attribute("cod").getText().equals(rpcode))
        			 {
    					 Reg reg = new Reg();
    					 int meal = 2;
    		             String RPcode = element2.attribute("cod").getText();
    		             reg.setRpCode(RPcode);
    		             if ((!RPcode.equals("RO")) && (!RPcode.equals("OB"))) {
    		                RPcode.equals("SA");
    		             }
    		             if ((RPcode.equals("FB")) || (RPcode.equals("PC")) || (RPcode.equals("AI")) || (RPcode.equals("TI")))
    		             {
    		               reg.setBF(true);
    		               reg.setLUN(true);
    		               reg.setBD(true);
    		             }
    		             if ((RPcode.equals("BB")) || (RPcode.equals("AD"))) {
    		               reg.setBF(true);
    		             }
    		             if ((RPcode.equals("HB")) || (RPcode.equals("MP")))
    		             {
    		               reg.setBF(true);
    		               reg.setBD(true);
    		             }
    		             reg.setNumberOfMeal(meal+"");
        				 List<Element> linlist = element2.elements("lin");
        				 List<Lin> Lins = new ArrayList<Lin>();
        				 String cancelPenalty  = "";
        				 double tempPrice = 0.0d;
        				 for (Element element3 : linlist) 
        				 {
        					 Lin lin = new Lin();
        					 String linText = element3.getTextTrim();
        					 if(null == cancelPenalty || "".equals(cancelPenalty))
        					 {
        						 String xmlInfo144 = HttpUtils.GetRestelXml144(hotelCode,linText);
    	     					 log.info("取消政策供应商请求报文："+xmlInfo144);
        						 String result144 = HttpUtils.HttpClientPost(xmlInfo144);
    							 log.info("取消政策供应商相应报文："+result144);
        						 InputStream stream144 = new ByteArrayInputStream(result144.getBytes());
    	    					 cancelPenalty = HttpUtils.Parse144Xml(stream144);
    	    					 log.info("解析后酒店的取消政策是："+cancelPenalty);
    	    					 if(null == cancelPenalty || "".equals(cancelPenalty))
    	    					 {
    	    						 cancelPenalty = "The rate is non-refundable";
    	    						 log.info("无效值，选择默认的取消政策："+cancelPenalty);
    	    					 }
        					 }
	    					 reg.setCancelPenalty(cancelPenalty);
	    					//根据取消政策，拼接Deadline,默认为不可取消
	    					 String Deadline = "<Deadline OffsetTimeUnit=\"Hour\" OffsetUnitMultiplier=\"24\" OffsetDropTime=\"BeforeArrival\" />";
    				         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");// 自定义时间格式
    				         Date date_a = simpleDateFormat.parse(start);//字符串转Date
	    					 if(cancelPenalty.contains("less than"))
	    					 {
	    						 int datas = Integer.parseInt(cancelPenalty.split(" ")[5]);
	    						 if( 0< datas && datas < 15 ){
		    						 Calendar ca = Calendar.getInstance();
		    				 		 ca.setTime(date_a);
		    				 		 ca.add(ca.DATE,-2);
	    	    					 date_a = ca.getTime();
	    	    					 System.out.println("-2"+ca.getTime());
	    	    					 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    	    					 String time = sdf.format(date_a);
	    	    					 
	    	    					 Deadline = "<Deadline AbsoluteDeadline=\""+time+"\" />";
	    							 
	    						 }

	    					 }
	    					 log.info("推送给携程的取消政策Deadline内容："+Deadline);
	    					 reg.setDeadline(Deadline);
	    					 lin.setLinText(linText);
	    					 tempPrice = AmountUtil.multiply(Double.parseDouble(linText.split("#")[3]), usdrate, 2);
	    					 tempPrice = AmountUtil.add(tempPrice, tj, 2);
	    					 lin.setPrr(tempPrice+"");
	    					 String st = linText.split("#")[7];
	    					 String ed = linText.split("#")[8];
	    					 lin.setStart(st.substring(0, 4)+"-"+st.substring(4,6)+"-"+st.substring(6));
	    					 lin.setEnd(ed.substring(0, 4)+"-"+ed.substring(4,6)+"-"+ed.substring(6,8));
	    					 Lins.add(lin);
        				 }
        				 double totalPrice = AmountUtil.multiply(tempPrice, linlist.size() * 1.0, 2);
        				 reg.setTotal(totalPrice+"");
        				 linlist.size();
        				 reg.setLinList(Lins);
        				 regList.add(reg);
        			 }
        			 else if("".equals(rpcode))
        			 {
    					 Reg reg = new Reg();
    					 int meal = 2;
    		             String RPcode = element2.attribute("cod").getText();
    		             reg.setRpCode(RPcode);
    		             if ((!RPcode.equals("RO")) && (!RPcode.equals("OB"))) {
    		                RPcode.equals("SA");
    		             }
    		             if ((RPcode.equals("FB")) || (RPcode.equals("PC")) || (RPcode.equals("AI")) || (RPcode.equals("TI")))
    		             {
    		               reg.setBF(true);
    		               reg.setLUN(true);
    		               reg.setBD(true);
    		             }
    		             if ((RPcode.equals("BB")) || (RPcode.equals("AD"))) {
    		               reg.setBF(true);
    		             }
    		             if ((RPcode.equals("HB")) || (RPcode.equals("MP")))
    		             {
    		               reg.setBF(true);
    		               reg.setBD(true);
    		             }
        				 reg.setNumberOfMeal(meal+"");
        				 List<Element> linlist = element2.elements("lin");
        				 List<Lin> Lins = new ArrayList<Lin>();
        				 String cancelPenalty  = "";
        				 double tempPrice = 0.0d;
        				 for (Element element3 : linlist) 
        				 {
        					 Lin lin = new Lin();
        					 String linText = element3.getTextTrim();
        					 if(null == cancelPenalty || "".equals(cancelPenalty))
        					 {
        						 String xmlInfo144 = HttpUtils.GetRestelXml144(hotelCode,linText);
        						 log.info("取消政策供应商请求报文："+xmlInfo144);
    	     					 String result144 = HttpUtils.HttpClientPost(xmlInfo144);
    							 log.info("取消政策供应商相应报文："+result144);
    							 InputStream stream144 = new ByteArrayInputStream(result144.getBytes());
    	    					 cancelPenalty = HttpUtils.Parse144Xml(stream144);
    	    					 log.info("解析后酒店的取消政策是："+cancelPenalty);
    	    					 if(null == cancelPenalty || "".equals(cancelPenalty))
    	    					 {
    	    						 cancelPenalty = "The rate is non-refundable";
    	    						 log.info("无效值，选择默认的取消政策："+cancelPenalty);
    	    					 }
        					 }
	    					 reg.setCancelPenalty(cancelPenalty);
	    					//根据取消政策，拼接Deadline,默认为不可取消
	    					 String Deadline = "<Deadline OffsetTimeUnit=\"Hour\" OffsetUnitMultiplier=\"24\" OffsetDropTime=\"BeforeArrival\" />";
    				         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");// 自定义时间格式
    				         Date date_a = simpleDateFormat.parse(start);//字符串转Date
	    					 if(cancelPenalty.contains("less than"))
	    					 {
	    						 int datas = Integer.parseInt(cancelPenalty.split(" ")[5]);
	    						 if( 0< datas && datas < 15 ){
		    						 Calendar ca = Calendar.getInstance();
		    				 		 ca.setTime(date_a);
		    				 		 ca.add(ca.DATE,-2);
	    	    					 date_a = ca.getTime();
	    	    					 System.out.println("-2"+ca.getTime());
	    	    					 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    	    					 String time = sdf.format(date_a);
	    	    					 
	    	    					 Deadline = "<Deadline AbsoluteDeadline=\""+time+"\" />";
	    							 
	    						 }

	    					 }
	    					 log.info("推送给携程的取消政策Deadline内容："+Deadline);
	    					 reg.setDeadline(Deadline);
	    					 lin.setLinText(linText);
	    					 tempPrice = AmountUtil.multiply(Double.parseDouble(linText.split("#")[3]), usdrate, 2);
	    					 tempPrice = AmountUtil.add(tempPrice, tj, 2);
	    					 lin.setPrr(tempPrice+"");
	    					 String st = linText.split("#")[7];
	    					 String ed = linText.split("#")[8];
	    					 lin.setStart(st.substring(0, 4)+"-"+st.substring(4,6)+"-"+st.substring(6));
	    					 lin.setEnd(ed.substring(0, 4)+"-"+ed.substring(4,6)+"-"+ed.substring(6,8));
	    					 Lins.add(lin);
        				 }
        				 double totalPrice = AmountUtil.multiply(tempPrice, linlist.size() * 1.0, 2);
        				 reg.setTotal(totalPrice+"");
        				 reg.setLinList(Lins);
        				 regList.add(reg);
        			 }
    			}
        		romeStay.setRegList(regList);
        		romeStay.setGuestCount(count.split("-")[0]);
        		romeStayList.add(romeStay);
        	 }
        	 else if("".equals(rtcode))
        	 {
        		 RomeStay romeStay = new RomeStay();
            	 romeStay.setRtCode(element.attribute("cod").getText());
        		 List<Element> reglist = element.elements("reg");
        		 List<Reg> regList = new ArrayList<Reg>();
        		 for (Element element2 : reglist) 
        		 {
        			 if(!("".equals(rpcode)) && element2.attribute("cod").getText().equals(rpcode))
        			 {
    					 Reg reg = new Reg();
    					 int meal = 2;
    		             String RPcode = element2.attribute("cod").getText();
    		             reg.setRpCode(RPcode);
    		             if ((!RPcode.equals("RO")) && (!RPcode.equals("OB"))) {
    		                RPcode.equals("SA");
    		             }
    		             if ((RPcode.equals("FB")) || (RPcode.equals("PC")) || (RPcode.equals("AI")) || (RPcode.equals("TI")))
    		             {
    		               reg.setBF(true);
    		               reg.setLUN(true);
    		               reg.setBD(true);
    		             }
    		             if ((RPcode.equals("BB")) || (RPcode.equals("AD"))) {
    		               reg.setBF(true);
    		             }
    		             if ((RPcode.equals("HB")) || (RPcode.equals("MP")))
    		             {
    		               reg.setBF(true);
    		               reg.setBD(true);
    		             }
        				 reg.setNumberOfMeal(meal+"");
        				 List<Element> linlist = element2.elements("lin");
        				 List<Lin> Lins = new ArrayList<Lin>();
        				 String cancelPenalty  = "";
        				 double tempPrice = 0.0d;
        				 for (Element element3 : linlist) 
        				 {
        					 Lin lin = new Lin();
        					 String linText = element3.getTextTrim();
        					 if(null == cancelPenalty || "".equals(cancelPenalty))
        					 {
        						 String xmlInfo144 = HttpUtils.GetRestelXml144(hotelCode,linText);
        						 log.info("取消政策供应商请求报文："+xmlInfo144);
    	     					 String result144 = HttpUtils.HttpClientPost(xmlInfo144);
    							 log.info("取消政策供应商相应报文："+result144);
    							 InputStream stream144 = new ByteArrayInputStream(result144.getBytes());
    	    					 cancelPenalty = HttpUtils.Parse144Xml(stream144);
    	    					 log.info("解析后酒店的取消政策是："+cancelPenalty);
    	    					 if(null == cancelPenalty || "".equals(cancelPenalty))
    	    					 {
    	    						 cancelPenalty = "The rate is non-refundable";
    	    						 log.info("无效值，选择默认的取消政策："+cancelPenalty);
    	    					 }
        					 }
	    					 reg.setCancelPenalty(cancelPenalty);
	    					//根据取消政策，拼接Deadline,默认为不可取消
	    					 String Deadline = "<Deadline OffsetTimeUnit=\"Hour\" OffsetUnitMultiplier=\"24\" OffsetDropTime=\"BeforeArrival\" />";
    				         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");// 自定义时间格式
    				         Date date_a = simpleDateFormat.parse(start);//字符串转Date
	    					 if(cancelPenalty.contains("less than"))
	    					 {
	    						 int datas = Integer.parseInt(cancelPenalty.split(" ")[5]);
	    						 if( 0< datas && datas < 15 ){
		    						 Calendar ca = Calendar.getInstance();
		    				 		 ca.setTime(date_a);
		    				 		 ca.add(ca.DATE,-2);
	    	    					 date_a = ca.getTime();
	    	    					 System.out.println("-2"+ca.getTime());
	    	    					 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    	    					 String time = sdf.format(date_a);
	    	    					 
	    	    					 Deadline = "<Deadline AbsoluteDeadline=\""+time+"\" />";
	    							 
	    						 }

	    					 }
	    					 log.info("推送给携程的取消政策Deadline内容："+Deadline);
	    					 reg.setDeadline(Deadline);
	    					 lin.setLinText(linText);
	    					 tempPrice = AmountUtil.multiply(Double.parseDouble(linText.split("#")[3]), usdrate, 2);
	    					 tempPrice = AmountUtil.add(tempPrice, tj, 2);
	    					 lin.setPrr(tempPrice+"");
	    					 String st = linText.split("#")[7];
	    					 String ed = linText.split("#")[8];
	    					 lin.setStart(st.substring(0, 4)+"-"+st.substring(4,6)+"-"+st.substring(6));
	    					 lin.setEnd(ed.substring(0, 4)+"-"+ed.substring(4,6)+"-"+ed.substring(6,8));
	    					 Lins.add(lin);
        				 }
        				 double totalPrice = AmountUtil.multiply(tempPrice, linlist.size() * 1.0, 2);
        				 reg.setTotal(totalPrice+"");
        				 reg.setLinList(Lins);
        				 regList.add(reg);
        			 }
        			 else
        			 {
    					 Reg reg = new Reg();
    					 int meal = 2;
    		             String RPcode = element2.attribute("cod").getText();
    		             reg.setRpCode(RPcode);
    		             if ((!RPcode.equals("RO")) && (!RPcode.equals("OB"))) {
    		                RPcode.equals("SA");
    		             }
    		             if ((RPcode.equals("FB")) || (RPcode.equals("PC")) || (RPcode.equals("AI")) || (RPcode.equals("TI")))
    		             {
    		               reg.setBF(true);
    		               reg.setLUN(true);
    		               reg.setBD(true);
    		             }
    		             if ((RPcode.equals("BB")) || (RPcode.equals("AD"))) {
    		               reg.setBF(true);
    		             }
    		             if ((RPcode.equals("HB")) || (RPcode.equals("MP")))
    		             {
    		               reg.setBF(true);
    		               reg.setBD(true);
    		             }
        				 reg.setNumberOfMeal(meal+"");
        				 List<Element> linlist = element2.elements("lin");
        				 List<Lin> Lins = new ArrayList<Lin>();
        				 String cancelPenalty  = "";
        				 double tempPrice = 0.0d;
        				 for (Element element3 : linlist) 
        				 {
        					 Lin lin = new Lin();
        					 String linText = element3.getTextTrim();
        					 if(null == cancelPenalty || "".equals(cancelPenalty))
        					 {
        						 String xmlInfo144 = HttpUtils.GetRestelXml144(hotelCode,linText);
        						 log.info("取消政策供应商请求报文："+xmlInfo144);
    	     					 String result144 = HttpUtils.HttpClientPost(xmlInfo144);
    							 log.info("取消政策供应商相应报文："+result144);
    							 InputStream stream144 = new ByteArrayInputStream(result144.getBytes());
    	    					 cancelPenalty = HttpUtils.Parse144Xml(stream144);
    	    					 log.info("解析后酒店的取消政策是："+cancelPenalty);
    	    					 if(null == cancelPenalty || "".equals(cancelPenalty))
    	    					 {
    	    						 cancelPenalty = "The rate is non-refundable";
    	    						 log.info("无效值，选择默认的取消政策："+cancelPenalty);
    	    					 }
        					 }
	    					 reg.setCancelPenalty(cancelPenalty);
	    					 //根据取消政策，拼接Deadline,默认为不可取消
	    					 String Deadline = "<Deadline OffsetTimeUnit=\"Hour\" OffsetUnitMultiplier=\"24\" OffsetDropTime=\"BeforeArrival\" />";
    				         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");// 自定义时间格式
    				         Date date_a = simpleDateFormat.parse(start);//字符串转Date
	    					 if(cancelPenalty.contains("less than"))
	    					 {
	    						 int datas = Integer.parseInt(cancelPenalty.split(" ")[5]);
	    						 if( 0< datas && datas < 15 ){
		    						 Calendar ca = Calendar.getInstance();
		    				 		 ca.setTime(date_a);
		    				 		 ca.add(ca.DATE,-2);
	    	    					 date_a = ca.getTime();
	    	    					 System.out.println("-2"+ca.getTime());
	    	    					 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    	    					 String time = sdf.format(date_a);
	    	    					 
	    	    					 Deadline = "<Deadline AbsoluteDeadline=\""+time+"\" />";
	    							 
	    						 }

	    					 }
	    					 log.info("推送给携程的取消政策Deadline内容："+Deadline);
	    					 reg.setDeadline(Deadline);
	    					 lin.setLinText(linText);
	    					 tempPrice = AmountUtil.multiply(Double.parseDouble(linText.split("#")[3]), usdrate, 2);
	    					 tempPrice = AmountUtil.add(tempPrice, tj, 2);
	    					 lin.setPrr(tempPrice+"");
	    					 String st = linText.split("#")[7];
	    					 String ed = linText.split("#")[8];
	    					 lin.setStart(st.substring(0, 4)+"-"+st.substring(4,6)+"-"+st.substring(6));
	    					 lin.setEnd(ed.substring(0, 4)+"-"+ed.substring(4,6)+"-"+ed.substring(6,8));
	    					 Lins.add(lin);
        				 }
        				 double totalPrice = AmountUtil.multiply(tempPrice, linlist.size() * 1.0, 2);
        				 reg.setTotal(totalPrice+"");
        				 reg.setLinList(Lins);
        				 regList.add(reg);
        			 }
    			}
        		romeStay.setRegList(regList);
        		romeStay.setGuestCount(count.split("-")[0]);
        		romeStayList.add(romeStay); 
        	 } 
		}
        return romeStayList;
    }
    
    /**
     * 预订获取lins
     * @param inputStream
     * @param rtcode
     * @param rpcode
     * @param difference
     * @param count
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public static List<Lin> Parse110XmlWithLins(InputStream inputStream,String rtcode,String rpcode,String total,String prr) throws Exception
    {
    	if (inputStream == null){
            return null;
        }
        SAXReader reader = new SAXReader();// 读取输入流
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();// 得到xml根元素
        Element paramElem = root.element("param");// 获取param节点
        Element hotlsElem = paramElem.element("hotls");// 获取hotls 节点
        if(hotlsElem.attribute("num").getText().equals("0"))
        {
        	return null;
        }
        Element hot = hotlsElem.element("hot");
        Element res = hot.element("res");
        Element pax = res.element("pax");
        List<Element> paxlist = pax.elements("hab");
        List<Lin> Lins = new ArrayList<Lin>();
        for (Element element : paxlist) 
        {
        	 if(element.attribute("cod").getText().equals(rtcode))
        	 {
        		 List<Element> reglist = element.elements("reg");
        		 for (Element element2 : reglist) {
        			 int cmp = AmountUtil.compareTo(Double.parseDouble(element2.attribute("prr").getText()), Double.parseDouble(total));
        			 if(element2.attribute("cod").getText().equals(rpcode) && cmp==0)
        			 {
        				 List<Element> linlist = element2.elements("lin");
        				 for (Element element3 : linlist) 
        				 {
        					 Lin lin = new Lin();
        					 String linText = element3.getTextTrim();
	    					 lin.setLinText(linText);
	            			 int cmp2 = AmountUtil.compareTo(Double.parseDouble(linText.split("#")[3]), Double.parseDouble(prr));
	    					 if(cmp2 == 0)
	    					 {
	    						 lin.setPrr(linText.split("#")[3]);
		    					 String st = linText.split("#")[7];
		    					 String ed = linText.split("#")[8];
		    					 lin.setStart(st.substring(0, 4)+"-"+st.substring(4,6)+"-"+st.substring(6));
		    					 lin.setEnd(ed.substring(0, 4)+"-"+ed.substring(4,6)+"-"+ed.substring(6,8));
		    					 Lins.add(lin);
	    					 }
        				 }
        			 }
        		 }
        	 }
        }	
    	return Lins;
    }
    
    /**
     * 
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static String Parse144Xml(InputStream inputStream) throws Exception
    {
    	if (inputStream == null){
            return null;
        }
    	SAXReader reader = new SAXReader();// 读取输入流
    	Document document = reader.read(inputStream);
    	Element root = document.getRootElement();// 得到xml根元素
    	Element parametrosElem = root.element("parametros");// 获取parametros节点
    	Element politicaCancElem = parametrosElem.element("politicaCanc");// 获取estado 节点
    	Element conceptoElem = politicaCancElem.element("concepto");
    	return conceptoElem.getTextTrim();
    }
    
    /**
     * 解析202接口Xml
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static String Parse202Xml(InputStream inputStream) throws Exception
    {
    	if (inputStream == null){
            return null;
        }
        SAXReader reader = new SAXReader();// 读取输入流
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();// 得到xml根元素
        System.out.println(root.asXML());
        Element parametrosElem = root.element("parametros");// 获取parametros节点
        Element estadoElem = parametrosElem.element("estado");// 获取estado 节点
        Element n_localizadorElem = parametrosElem.element("n_localizador");// 获取n_localizador 节点
        return estadoElem.getTextTrim()+"#"+n_localizadorElem.getTextTrim();
    }
    
    /**
     * 解析3接口Xml
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static String Parse3Xml(InputStream inputStream) throws Exception
    {
    	if (inputStream == null){
            return null;
        }
        SAXReader reader = new SAXReader();// 读取输入流
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();// 得到xml根元素
        Element parametrosElem = root.element("parametros");// 获取parametros节点
        Element estadoElem = parametrosElem.element("estado");// 获取estado 节点
        Element localizadorElem = parametrosElem.element("localizador");// 获取localizador 节点
        Element localizador_cortoElem = parametrosElem.element("localizador_corto");// 获取localizador 节点
        return estadoElem.getTextTrim()+"#"+localizadorElem.getTextTrim()+"#"+localizador_cortoElem.getTextTrim();
    }
    
    /**
     * 解析401接口Xml
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static String Parse401Xml(InputStream inputStream) throws Exception
    {
    	if (inputStream == null){
            return null;
        }
        SAXReader reader = new SAXReader();// 读取输入流
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();// 得到xml根元素
        Element parametrosElem = root.element("parametros");// 获取parametros节点
        Element estadoElem = parametrosElem.element("estado");// 获取estado 节点
        Element localizadorElem = parametrosElem.element("localizador");// 获取localizador 节点
        return estadoElem.getTextTrim()+"#"+localizadorElem.getTextTrim();
    }
    
    /**
     * 获取110接口xml
     * @param hotel
     * @param pais
     * @param start
     * @param end
     * @return
     */
    public static String GetRestelXml110(String hotel_code,String pais,String pro,String start,String end,String number,String count,String content)
    {
    	StringBuffer xml = new StringBuffer();
    	xml.append("<peticion>");
    	xml.append("<tipo>110</tipo>");
    	xml.append("<parametros>");
    	xml.append("<hotel>"+hotel_code+"#</hotel>");
    	xml.append("<pais>"+pais+"</pais>");
    	xml.append("<provincia>"+pro+"</provincia>");
    	xml.append("<poblacion></poblacion>");
    	xml.append("<numhab1>"+number+"</numhab1>"); //房间类型1
    	xml.append("<paxes1>"+count+"</paxes1>"); //房间客人类别 Format: adult-children. Eq: To ask for 2 adults and 1 child would be: 2-1.
    	xml.append(content);
    	xml.append("<usuario>14269</usuario>");
    	xml.append("<afiliacion>RS</afiliacion>");
    	xml.append("<fechaentrada>"+start+"</fechaentrada>");
    	xml.append("<fechasalida>"+end+"</fechasalida>");
    	xml.append("<pais_cliente>CN</pais_cliente>");//国籍，默认为中国CN
    	xml.append("<idioma>2</idioma>");
    	xml.append("<radio>9</radio>");
    	xml.append("<duplicidad>0</duplicidad>");
    	xml.append("<comprimido>2</comprimido>");
    	xml.append("</parametros>");
    	xml.append("</peticion>");
    	return xml.toString();
	}
    
    /**
     * 获取144接口xml
     * @param hotel
     * @param pais
     * @param start
     * @param end
     * @return
     */
    public static String GetRestelXml144(String hotel_code,String lin)
    {
    	StringBuffer xml = new StringBuffer();
    	xml.append("<peticion>");
    	xml.append("<tipo>144</tipo>");
    	xml.append("<parametros>");
    	xml.append("<datos_reserva>");
    	xml.append("<hotel>"+hotel_code+"</hotel>");
    	xml.append("<lin>"+lin+"</lin>");
    	xml.append("<idioma>2</idioma>");
    	xml.append("</datos_reserva>");
    	xml.append("</parametros>");
    	xml.append("</peticion>");
    	return xml.toString();
	}
    
    /**
     * 获取202接口xml
     * @param hotel
     * @param guestName
     * @param remark
     * @param agencyBookID
     * @param forma 12  Hotel Payment	25  Credit 44 
     * @param lin
     * @return
     */
    public static String GetRestelXml202(String hotelCode,String guestName,String remark,String forma,List<Lin> lins,String contact)
    {
    	StringBuffer xml = new StringBuffer();
    	xml.append("<peticion>");
    	xml.append("<tipo>202</tipo>");
    	xml.append("<parametros>");
    	xml.append("<codigo_hotel>"+hotelCode+"</codigo_hotel>");
    	xml.append("<nombre_cliente>"+guestName+"</nombre_cliente>");
    	xml.append("<observaciones>"+remark+"</observaciones>");
    	xml.append("<num_mensaje />");
    	xml.append("<afiliacion>RS</afiliacion>");
    	xml.append("<forma_pago>"+forma+"</forma_pago>"); // 12  Hotel Payment	25  Credit 44  Prepayment
    	xml.append("<res>");
    	for (Lin lin : lins) {
    		xml.append("<lin>"+lin.getLinText()+"</lin>");
		}
    	xml.append("</res>");
    	xml.append(contact);
    	xml.append("<idioma>2</idioma>");
    	xml.append("</parametros>");
    	xml.append("</peticion>");
    	return xml.toString();
    }
    
    /**
     * 获取3接口xml
     * @param localizador
     * @return
     */
    public static String GetRestelXml3(String localizador,String action)
    {
    	StringBuffer xml = new StringBuffer();
    	xml.append("<peticion>");
		xml.append("<tipo>3</tipo>");
		xml.append("<parametros>");
		xml.append("<localizador>"+localizador+"</localizador>");
		xml.append("<accion>"+action+"</accion>"); //AE  Reservation confirmation AI  Reservation annulment
		xml.append("<idioma>2</idioma>");
		xml.append("</parametros>");
		xml.append("</peticion>");
		return xml.toString();
    }
    
    /**
     * 获取401接口xml
     * @param localizador
     * @return
     */
    public static String GetRestelXml401(String localizador,String localizador_corto)
    {
    	StringBuffer xml = new StringBuffer();
    	xml.append("<peticion>");
		xml.append("<tipo>401</tipo>");
		xml.append("<parametros>");
		xml.append("<localizador_largo>"+localizador+"</localizador_largo>");
		xml.append("<localizador_corto>"+localizador_corto+"</localizador_corto>");
		xml.append("</parametros>");
		xml.append("</peticion>");
		return xml.toString();
    }
    
    /**
	 * 发起http请求并获取携程结果
	 * @author Administrator
	 *  requesturl   请求地址
	 *  requestMethod 请求方法
	 *  outpustr     返回数据
	 */
	 public static String httpsRequestByCtripXml(String requestUrl, String requestMethod, String outputStr)
	 {
		StringBuffer buffer = new StringBuffer(); // 拼接字符串
		try {
			URL url = new URL(requestUrl); // 代表一个绝对地址
			HttpsURLConnection httpsUrlConn = (HttpsURLConnection) url
					.openConnection(); // 返回一个url对象
			httpsUrlConn.setDoOutput(true); // 以后就可以使用conn.getOutputStream().write()
			httpsUrlConn.setDoInput(true); // 以后就可以使用conn.getInputStream().read();
			httpsUrlConn.setUseCaches(false); // 请求不可以使用缓存
			httpsUrlConn.addRequestProperty("connection", "Keep-Alive");
			 // 设置请求属性
			httpsUrlConn.setRequestProperty("Content-Type", "text/xml");
			// 设置请求方式（GET/POST）
			httpsUrlConn.setRequestMethod(requestMethod); // 设置请求方式 数据从参数传来
			httpsUrlConn.connect(); // 连接
			// 当有数据需要提交时
			if (null != outputStr) {
				OutputStream outputStream = httpsUrlConn.getOutputStream(); // 获取输出流
				// 注意编码格式，防止中文乱码
				outputStream.write(outputStr.getBytes()); // 向对象输出流写出数据，这些数据将存到内存缓冲区中
				outputStream.flush();
				outputStream.close(); // 关闭流
			}
			// 将返回的输入流转换成字符串
			InputStream inputStream = httpsUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str+"\n"); // 把读进来的数据添加到append
			}
			bufferedReader.close(); // 关闭管道
			inputStreamReader.close(); // 关闭流
			// 释放资源
			inputStream.close(); // 关闭流
			inputStream = null; // 设置为空
			httpsUrlConn.disconnect();
		} catch (ConnectException ce) {
			System.out.println(ce.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return buffer.toString();
	}
 
	 private static void trustAllHttpsCertificates() throws Exception {
	        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
	        javax.net.ssl.TrustManager tm = new miTM();
	        trustAllCerts[0] = tm;
	        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, null);
	        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
	 
	 static class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
	    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	        return null;
	    }
	
	    public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
	        return true;
	    }
	
	    public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
	        return true;
	    }
	
	    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
	            throws java.security.cert.CertificateException {
	        return;
	    }
	
	    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
	            throws java.security.cert.CertificateException {
	        return;
	    }
	}

	/**
	* post方式请求服务器(https协议)
	* @param url
	*            请求地址
	* @param content
	*            参数
	* @param charset
	*            编码
	* @return
	 * @throws Exception 
	*/
	public static String CtripHttpsPost(String content)
	            throws Exception {
		// 创建SSLContext
        SSLContext sslContext = SSLContext.getInstance("SSL");
        TrustManager[] trustManagers = {new X509TrustManager() {
            /*
             * 实例化一个信任连接管理器
             * 空实现是所有的连接都能访问
             */
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};
        // 初始化
        sslContext.init(null, trustManagers, new SecureRandom());
        javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        String ctrip_api_url = Config.getStr("ctrip_api_url");
        URL url = new URL(ctrip_api_url);
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
        httpsURLConnection.setSSLSocketFactory(sslSocketFactory);
        httpsURLConnection.setDoOutput(true);
        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.addRequestProperty("connection", "Keep-Alive");
        httpsURLConnection.setRequestProperty("Content-Type", "text/xml");
        httpsURLConnection.setHostnameVerifier(new TrustAnyHostnameVerifier());
        trustAllHttpsCertificates();
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
        
        httpsURLConnection.connect();
        DataOutputStream out = new DataOutputStream(httpsURLConnection.getOutputStream());
        out.write(content.getBytes("utf-8"));
        // 刷新、关闭
        out.flush();
        out.close();
        InputStream is = httpsURLConnection.getInputStream();
        if (is != null) {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            is.close();
            if(outStream != null){
            	return new String(outStream.toByteArray(),"utf-8");
            }
        }
        return null;
	}

	/**
	 * 获取携程确认订单8xml
	 * @return
	 */
	public static String PushCtripXmlConfirm(String r501,String r502,String r504)
	{
		StringBuilder xmlinfo = new StringBuilder();
		xmlinfo.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
		xmlinfo.append("<soap:Header />");
		xmlinfo.append("<soap:Body>");
		xmlinfo.append("<OTA_HotelResNumUpdateRQ Version=\"2.3\" TimeStamp=\""+DateUtils.getNowByGMT8()+"\" EchoToken=\"201904230717\" xmlns=\"http://www.opentravel.org/OTA/2003/05\">");
		xmlinfo.append("  <POS>");
		xmlinfo.append("    <Source>");
		xmlinfo.append("    	<RequestorID ID=\"hangye\" MessagePassword=\"hangye_101019\" Type=\"1\">");
		xmlinfo.append("    		<CompanyName Code=\"C\" CodeContext=\"978\" />");
		xmlinfo.append("    	</RequestorID>");
		xmlinfo.append("    </Source>");
		xmlinfo.append("  </POS>");
		xmlinfo.append("  <HotelReservations>");
		xmlinfo.append("    <HotelReservation>");
		xmlinfo.append("      <ResGlobalInfo>");
		xmlinfo.append("        <HotelReservationIDs>");
		xmlinfo.append("          <HotelReservationID ResID_Value=\""+r502+"\" ResID_Type=\"502\"/>");
		xmlinfo.append("          <HotelReservationID ResID_Value=\""+r501+"\" ResID_Type=\"501\"/>");
		xmlinfo.append("          <HotelReservationID ResID_Value=\""+r504+"\" ResID_Type=\"504\"/>");
		xmlinfo.append("        </HotelReservationIDs>");
		xmlinfo.append("      </ResGlobalInfo>");
		xmlinfo.append("    </HotelReservation>");
		xmlinfo.append("  </HotelReservations>");
		xmlinfo.append("</OTA_HotelResNumUpdateRQ>");
		xmlinfo.append("</soap:Body>");
		xmlinfo.append("</soap:Envelope>");
		return xmlinfo.toString();
	}
	
	 /**
     * 读取订单Xml
     * @param hotelInfoRecord
     * @param codigo_hotel
     * @return
     */
    public static String GetCtripXml9()
    {
    	StringBuilder xmlinfo = new StringBuilder();
		xmlinfo.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">");
		xmlinfo.append("  <SOAP-ENV:Header />");
		xmlinfo.append("  <SOAP-ENV:Body>");
		xmlinfo.append("    <OTA_ReadRQ xmlns=\"http://www.opentravel.org/OTA/2003/05\" EchoToken=\"201703292247\" TimeStamp=\"2017-03-29T22:47:58.1708455+08:00\" Version=\"2.2\" ReservationType=\"Hotel\">");
		xmlinfo.append("      <POS>");
		xmlinfo.append("        <Source>");
		xmlinfo.append("          <RequestorID ID=\"Ctrip\" Type=\"5\" MessagePassword=\"123qaz\">");
		xmlinfo.append("            <CompanyName Code=\"C\" CodeContext=\"134\" />");
		xmlinfo.append("          </RequestorID>");
		xmlinfo.append("        </Source>");
		xmlinfo.append("      </POS>");
		xmlinfo.append("      <ReadRequests>");
		xmlinfo.append("        <ReadRequest>");
		xmlinfo.append("          <UniqueID ID=\"34437045\" Type=\"501\" />");
		xmlinfo.append("          <UniqueID ID=\"170329224658137\" Type=\"502\" />");
		xmlinfo.append("        </ReadRequest>");
		xmlinfo.append("      </ReadRequests>");
		xmlinfo.append("    </OTA_ReadRQ>");
		xmlinfo.append("  </SOAP-ENV:Body>");
		xmlinfo.append("</SOAP-ENV:Envelope>");
    	return xmlinfo.toString();
    }
		
		
    /**
     * 携程酒店数据Xml
     * @param hotelInfoRecord
     * @param codigo_hotel
     * @return
     */
    public static String PushCtripXmlHotelInfo(Record hotelInfoRecord,String codigo_hotel)
    {
    	StringBuilder xmlinfo = new StringBuilder();
    	xmlinfo.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
    	xmlinfo.append("<soap:Body>");
		xmlinfo.append("<OTA_HotelDescriptiveContentNotifRQ Target=\"Production\" PrimaryLangID=\"en-us\" Version=\"1.0\" TimeStamp=\""+DateUtils.getNowByGMT8()+"\" xmlns=\"http://www.opentravel.org/OTA/2003/05\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
		xmlinfo.append("  <POS>");
		xmlinfo.append("    <Source>");
		xmlinfo.append("    	<RequestorID ID=\"hangye\" MessagePassword=\"hangye_101019\" Type=\"1\">");
		xmlinfo.append("    		<CompanyName Code=\"C\" CodeContext=\"978\" />");
		xmlinfo.append("    	</RequestorID>");
		xmlinfo.append("    </Source>");
		xmlinfo.append("  </POS>");
		xmlinfo.append("  <HotelDescriptiveContents  HotelCode=\""+codigo_hotel+"\">");
		xmlinfo.append("    <HotelDescriptiveContent>");
		xmlinfo.append("      <HotelInfo HotelStatus=\"Active\">");
		xmlinfo.append("        <Descriptions>");
		xmlinfo.append("          <MultimediaDescriptions>");
		xmlinfo.append("            <MultimediaDescription>");
		xmlinfo.append("              <TextItems>");
		xmlinfo.append("                <TextItem Language=\"en-us\" Title=\""+hotelInfoRecord.getStr("nombre_h")+"\">"); //语言和酒店名称
		xmlinfo.append("                	<Description>"+hotelInfoRecord.getStr("desc_hotel")+"</Description>");
		xmlinfo.append("                </TextItem>");
		xmlinfo.append("              </TextItems>");
		xmlinfo.append("            </MultimediaDescription>");
		xmlinfo.append("           </MultimediaDescriptions>");
		xmlinfo.append("         </Descriptions>");
//		xmlinfo.append("         <CategoryCodes>");
//		xmlinfo.append("           <GuestRoomInfo Quantity="+hotelInfoRecord.getStr("num_habitaciones")+" />"); //客房数量
//		xmlinfo.append("         </CategoryCodes>");
		xmlinfo.append("         <Position Longitude=\""+hotelInfoRecord.getStr("longitude")+"\" Latitude=\""+hotelInfoRecord.getStr("latitude")+"\"/>"); //经纬度
		xmlinfo.append("      </HotelInfo>");
		xmlinfo.append("      <ContactInfos>");
		xmlinfo.append("        <ContactInfo>");
		xmlinfo.append("          <Addresses>");
		xmlinfo.append("            <Address Language=\"en-us\">");
		xmlinfo.append("            	<AddressLine>"+hotelInfoRecord.getStr("direccion")+"</AddressLine>"); //地址
		xmlinfo.append("            </Address>");
		xmlinfo.append("          </Addresses>");
		xmlinfo.append("          <Phones>");
		xmlinfo.append("            <Phone PhoneNumber=\""+hotelInfoRecord.getStr("telefono")+"\" PhoneTechType=\"Voice\"/>");
		xmlinfo.append("          </Phones>");
		xmlinfo.append("        </ContactInfo>");
		xmlinfo.append("      </ContactInfos>");
		xmlinfo.append("    </HotelDescriptiveContent>");
		xmlinfo.append("  </HotelDescriptiveContents>");
		xmlinfo.append("</OTA_HotelDescriptiveContentNotifRQ>");
		xmlinfo.append("</soap:Body>");
		xmlinfo.append("</soap:Envelope>");
		return xmlinfo.toString();
    }

    /**
     * 解析携程房型反馈数据
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static String ParseCtripCMXml(InputStream inputStream) throws Exception
    {
    	if (inputStream == null){
            return null;
        }
        SAXReader reader = new SAXReader();// 读取输入流
        Document document = reader.read(inputStream);
        return document.asXML();
    }
    
    /**
     * 推送房型信息xml
     * @param hotelInfoRecord
     * @param codigo_hotel
     * @param at
     * @param atmax
     * @param atmin
     * @param cd
     * @param cdmax
     * @param cdmin
     * @param typeDesc
     * @param currency
     * @return
     */
    public static String PushCtripXmlRoomType(String codigo_hotel,String rtCode,String admax,String clmax,String typeTitle,String currency,String typeDesc)
    {
    	StringBuilder xmlinfo = new StringBuilder();
    	xmlinfo.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
    	xmlinfo.append("<soap:Body>");
		xmlinfo.append("<OTA_HotelInvNotifRQ Target=\"Production\" PrimaryLangID=\"en-us\" Version=\"1.0\" TimeStamp=\""+DateUtils.getNowByGMT8()+"\" xmlns=\"http://www.opentravel.org/OTA/2003/05\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
		xmlinfo.append("  <POS>");
		xmlinfo.append("    <Source>");
		xmlinfo.append("    	<RequestorID ID=\"hangye\" MessagePassword=\"hangye_101019\" Type=\"1\">");
		xmlinfo.append("    		<CompanyName Code=\"C\" CodeContext=\"978\" />");
		xmlinfo.append("    	</RequestorID>");
		xmlinfo.append("    </Source>");
		xmlinfo.append("  </POS>");
		xmlinfo.append("  <SellableProducts HotelCode=\""+codigo_hotel+"\">");
		xmlinfo.append("    <SellableProduct InvTypeCode=\""+rtCode+"\" InvStatusType=\"Active\">");
		xmlinfo.append("      <GuestRoom>");//<!--AgeQualifyingCode：入住是否成人 10 成人 8 儿童 -->
		xmlinfo.append("        <Occupancy AgeQualifyingCode=\"10\" MinOccupancy=\"1\" MaxOccupancy=\""+admax+"\"/>");	//成人
//		xmlinfo.append("        <Occupancy AgeQualifyingCode=\"8\" MinOccupancy=\"0\" MaxOccupancy=\""+clmax+"\"/>");	//儿童
//		xmlinfo.append("        <Room RoomTypeCode=\""+type+"\" Quantity=\"100\" SizeMeasurement=\"30\"/>"); 
		xmlinfo.append("        <Currency Code=\""+currency+"\"/>");
		xmlinfo.append("        <Description>");
		xmlinfo.append("          <Text Language=\"en-us\">"+typeTitle+"</Text>");
		xmlinfo.append("        </Description>");
		xmlinfo.append("        <LongDescription>");
		xmlinfo.append("          <Text Language=\"en-us\">"+typeDesc+"</Text>");
		xmlinfo.append("        </LongDescription>");
		xmlinfo.append("      </GuestRoom>");
		xmlinfo.append("    </SellableProduct>");
		xmlinfo.append("  </SellableProducts>");
		xmlinfo.append("</OTA_HotelInvNotifRQ>");
		xmlinfo.append("</soap:Body>");
		xmlinfo.append("</soap:Envelope>");
		return xmlinfo.toString();
    }
    
    /**
     * 推送售卖房型
     * @param codigo_hotel
     * @param rtCode
     * @param rtdesc
     * @return
     */
    public static String PushCtripXmlRatePlan(String codigo_hotel,String rpCode,String rtCode,String guest)
    {
    	StringBuilder xmlinfo = new StringBuilder();
    	xmlinfo.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
    	xmlinfo.append("<soap:Body>");
    	xmlinfo.append("<OTA_HotelRatePlanNotifRQ Target=\"Production\" PrimaryLangID=\"en-us\" Version=\"1.0\" TimeStamp=\""+DateUtils.getNowByGMT8()+"\" xmlns=\"http://www.opentravel.org/OTA/2003/05\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
		xmlinfo.append("  <POS>");
		xmlinfo.append("    <Source>");
		xmlinfo.append("    	<RequestorID ID=\"hangye\" MessagePassword=\"hangye_101019\" Type=\"1\">");
		xmlinfo.append("    		<CompanyName Code=\"C\" CodeContext=\"978\" />");
		xmlinfo.append("    	</RequestorID>");
		xmlinfo.append("    </Source>");
		xmlinfo.append("  </POS>");
		xmlinfo.append("  <RatePlans HotelCode=\""+codigo_hotel+"\">");
		xmlinfo.append("    <RatePlan RatePlanCode=\""+rpCode+"\" RatePlanCategory=\"501\" RatePlanStatusType=\"Active\">");
		xmlinfo.append("    	<BookingRules>");
		xmlinfo.append("    		<BookingRule MaxTotalOccupancy=\""+guest+"\" />");
		xmlinfo.append("    	</BookingRules>");
		xmlinfo.append("    	<SellableProducts>");
		xmlinfo.append("    		<SellableProduct InvTypeCode=\""+rtCode+"\" />");
		xmlinfo.append("    	</SellableProducts>");
//		xmlinfo.append("    	<Description>");
//		xmlinfo.append("    		<Text Language=\"en-us\">"+rpdesc+"</Text>");
//		xmlinfo.append("    	</Description>");
		xmlinfo.append("    </RatePlan>");
		xmlinfo.append("  </RatePlans>");
		xmlinfo.append("</OTA_HotelRatePlanNotifRQ>");
		xmlinfo.append("</soap:Body>");
		xmlinfo.append("</soap:Envelope>");
    	return xmlinfo.toString();
    }
    
    /**
     * 查询酒店推送状态
     * @param codigo_hotel
     * @return
     */
    public static String PushCtripXmlSearch(String codigo_hotel)
    {
    	StringBuilder xmlinfo = new StringBuilder();
    	xmlinfo.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://www.opentravel.org/OTA/2003/05\"><soap:Header /><soap:Body>");
    	xmlinfo.append("<OTA_HotelStatsNotifRQ Target=\"Production\" PrimaryLangID=\"en-us\" Version=\"1.0\" TimeStamp=\""+DateUtils.getNowByGMT8()+"\" xmlns=\"http://www.opentravel.org/OTA/2003/05\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
		xmlinfo.append("  <POS>");
		xmlinfo.append("    <Source>");
		xmlinfo.append("    	<RequestorID ID=\"hangye\" MessagePassword=\"hangye_101019\" Type=\"1\">");
		xmlinfo.append("    		<CompanyName Code=\"C\" CodeContext=\"978\" />");
		xmlinfo.append("    	</RequestorID>");
		xmlinfo.append("    </Source>");
		xmlinfo.append("  </POS>");
		xmlinfo.append("  <Statistics>");
		xmlinfo.append("    <Statistic HotelCode=\""+codigo_hotel+"\" />");
		xmlinfo.append("  </Statistics>");
		xmlinfo.append("</OTA_HotelStatsNotifRQ>");
		xmlinfo.append("</soap:Body></soap:Envelope>");
    	return xmlinfo.toString();
    }
    
    /**
     * 推送房间状态
     * @param codigo_hotel
     * @param rtCodeParent
     * @param rtCodeSub
     * @param start
     * @param end
     * @return
     */
    public static String PushCtripXmlRoomStatus(String codigo_hotel,String rtCodeParent,String rtCodeSub,String status,String start,String end)
    {
    	StringBuilder xmlinfo = new StringBuilder();
    	xmlinfo.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns=\"http://www.opentravel.org/OTA/2003/05\"><soap:Header /><soap:Body>");
    	xmlinfo.append("<OTA_HotelAvailNotifRQ PrimaryLangID=\"en-us\" Version=\"1.0\" TimeStamp=\""+DateUtils.getNowByGMT8()+"\" xmlns=\"http://www.opentravel.org/OTA/2003/05\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
		xmlinfo.append("  <POS>");
		xmlinfo.append("    <Source>");
		xmlinfo.append("    	<RequestorID ID=\"hangye\" MessagePassword=\"hangye_101019\" Type=\"1\">");
		xmlinfo.append("    		<CompanyName Code=\"C\" CodeContext=\"978\" />");
		xmlinfo.append("    	</RequestorID>");
		xmlinfo.append("    </Source>");
		xmlinfo.append("  </POS>");
		xmlinfo.append("  <AvailStatusMessages HotelCode=\""+codigo_hotel+"\">");
		xmlinfo.append("    <AvailStatusMessage BookingLimitMessageType=\"SetLimit\" BookingLimit=\"3\">");
		xmlinfo.append("    	<StatusApplicationControl InvTypeCode=\""+rtCodeParent+"\" RatePlanCode=\""+rtCodeSub+"\" RatePlanCategory=\"501\" Start=\""+start+"\" End=\""+end+"\"/>");
//		xmlinfo.append("    	<LengthsOfStay FixedPatternLength=\"7\">");
//		xmlinfo.append("    		<LengthOfStay MinMaxMessageType=\"FullPatternLOS\">");
//		xmlinfo.append("    			<LOS_Pattern FullPatternLOS=\"1111111\"/>");
//		xmlinfo.append("    		</LengthOfStay>");
//		xmlinfo.append("    	</LengthsOfStay>");
		xmlinfo.append("    	<RestrictionStatus Status=\""+status+"\"/>");
		xmlinfo.append("    </AvailStatusMessage>");
		xmlinfo.append("  </AvailStatusMessages>");
		xmlinfo.append("</OTA_HotelAvailNotifRQ>");
		xmlinfo.append("</soap:Body></soap:Envelope>");
    	return xmlinfo.toString();
    }
    
    /**
     * 推送房间价格xml
     * @param codigo_hotel
     * @param pct
     * @param pcd
     * @param rt
     * @param start
     * @param end
     * @param bprice
     * @param aprice
     * @param currency
     * @return
     */
    public static String PushCtripXmlRoomPrice(String codigo_hotel,String rpcode,String rtcode,String content)
    {
    	StringBuilder xmlinfo = new StringBuilder();
    	xmlinfo.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
    	xmlinfo.append("<soap:Body>");
    	xmlinfo.append("<OTA_HotelRateAmountNotifRQ PrimaryLangID=\"en-us\" Version=\"2.3\" TimeStamp=\""+DateUtils.getNowByGMT8()+"\" xmlns=\"http://www.opentravel.org/OTA/2003/05\">");
		xmlinfo.append("  <POS>");
		xmlinfo.append("    <Source>");
		xmlinfo.append("    	<RequestorID ID=\"hangye\" MessagePassword=\"hangye_101019\" Type=\"1\">");
		xmlinfo.append("    		<CompanyName Code=\"C\" CodeContext=\"978\" />");
		xmlinfo.append("    	</RequestorID>");
		xmlinfo.append("    </Source>");
		xmlinfo.append("  </POS>");
		xmlinfo.append("  <RateAmountMessages HotelCode=\""+codigo_hotel+"\">");
		xmlinfo.append("  	<RateAmountMessage>");
		xmlinfo.append("    	<StatusApplicationControl RatePlanCode=\""+rpcode+"\" RatePlanCategory=\"501\" InvTypeCode=\""+rtcode+"\" />");
		xmlinfo.append("    	<Rates>");
		xmlinfo.append(content);
		xmlinfo.append("    	</Rates>");
		xmlinfo.append("  	</RateAmountMessage>");
		xmlinfo.append("  </RateAmountMessages>");
		xmlinfo.append("</OTA_HotelRateAmountNotifRQ>");
		xmlinfo.append("</soap:Body>");
		xmlinfo.append("</soap:Envelope>");
    	return xmlinfo.toString();
    }
    
    public static void main(String[] args) 
    {
//    	String test = GetRestelXml110("AB","ABDAM","04/04/2019","04/09/2019");
//    	System.out.println(test);
    	//测试请求1
//    	String codigo_hotel = "805982";
//    	Record hotelInfoRecord = new Record();
//    	hotelInfoRecord.set("nombre_h", "NOVOTEL DAMMAM BUSINESS PARK");
//    	hotelInfoRecord.set("desc_hotel", "LOCATIONThis city hotel is situated in the business district of Dammam, around 10 km from the city centre, in the eastern province of Saudi Arabia. Dhahran Exhibition Centre is approximately 12 km from the hotel, Saudi Aramco exhibition grounds are 25 km away and King Fahd Port is 40 km away. The nearest beach, Half Moon Beach is approximately 30 km from the hotel. The property is close to the Al Shatea Shopping Mall, Dammam Corniche, Alwaha Shopping Mall and the international airport.ROOMSAll rooms feature air conditioning and a bathroom. A minibar is also available. The accommodation units offer a range of amenities, including tea and coffee making equipment. An ironing set is also available to guests. A telephone, a TV and a stereo system are provided for guests' convenience. A hairdryer can also be found in each of the bathrooms.RESTAURANTThe hotel has a restaurant that serves buffet meals and local dishes. There is also a lobby café with sofas and flat-screen TVs that offers snacks and lighter fare.AMENITIESThe hotel offers an outdoor pool and a children's pool. Sun loungers provide a great place to sunbathe. Leisure options at the hotel also include tennis, a gym and a sauna.Services and facilities at the hotel include currency exchange facilities, a restaurant, a café, room service, a laundry and a conference room. Shopping facilities are available. Guests can use the parking spaces if required.");
//    	hotelInfoRecord.set("longitude", "50.138521");
//    	hotelInfoRecord.set("latitude", "26.407597");
//    	hotelInfoRecord.set("direccion", "AL KHOBAR DAMMAM HIGHWAY P.O.BOX 5138 DAMMAM 31422");
//    	hotelInfoRecord.set("telefono", "13900000000");
//    	System.out.println(PushCtripXmlHotelInfo(hotelInfoRecord,codigo_hotel));
    	//测试请求2
//    	System.out.println(PushCtripXmlRoomType(codigo_hotel, "7-", "2", "1", "Sphere room (1pax)", "USD"));
    	
    	//测试请求3
//    	System.out.println(PushCtripXmlRatePlan(codigo_hotel, "71", "7-", "Villa Deluxe 1 pers"));
    	
    	//测试请求4
//    	System.out.println(PushCtripXmlSearch(codigo_hotel));
    	
    	//测试请求5
//    	String start = DateUtils.getAddDayNow(DateUtils.YMD,3);
//		String end = DateUtils.getAddDayNow(DateUtils.YMD,10);
//    	System.out.println(PushCtripXmlRoomStatus(codigo_hotel,"7-","71",start,end));
    	
    	//测试请求6
//		double price = 76.35;
//    	System.out.println(PushCtripXmlRoomPrice(codigo_hotel,"71","7-",start,end,price,"USD"));

//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//		System.out.println(sdf.format(new Date()));
//		
//		String linText = "DB#1#HL#107.61#0#BB#OK#20190407#20190408#EU#2-0#K1RRP1#ODBB25#201904041610#890516#";
//		String st = linText.split("#")[7];
//		String ed = linText.split("#")[8];
//		System.out.println(st.substring(0, 4)+"-"+st.substring(4,6)+"-"+st.substring(6));
//		System.out.println(ed.substring(0, 4)+"-"+ed.substring(4,6)+"-"+ed.substring(6,8));
//
//		String s = "553137717100&Ctrip&1115";
//		System.out.println(DigestUtils.md5Hex(s));
//		String res = Md5Utils.getMD5("4392 2500 1234 5670&Yu Jiwei&1115");
		//System.out.println(DigestUtils.md5Hex("4392 2500 1234 5670&Yu Jiwei&1115"));
		
//		try {
//			String ress = HttpClientPost("<peticion><tipo>17</tipo></peticion>");
//			String filePath="D:\\15xml.xml";
//			FileOutputStream fos = new FileOutputStream(filePath);
//			fos.write(ress.getBytes());
//			fos.close();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    	String ress = PushCtripXmlSearch("745388");
//    	System.out.println(ress);
//    	double test1 = 163.13;
//    	test1 = AmountUtil.multiply(test1, 6.941, 2); //乘以汇率
//    	test1 = AmountUtil.add(test1, 5.0, 2);	//加上抬价
//		System.out.println(test1);
		for (int i = 0; i < 6; i++) 
		{
			String start = DateUtils.getAddDayNow(DateUtils.MDY,i*15);
			String end = DateUtils.getAddDayNow(DateUtils.MDY,(i+1)*15);
			System.out.println(start+"\t"+end);
		}
	} 
}
