package com.supyuan.system.job.jobWeb.job;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.supyuan.component.base.BaseJob;
import com.supyuan.system.hotel.HotelModel;
import com.supyuan.util.Config;
import com.supyuan.util.DateUtils;
import com.supyuan.util.HttpUtils;
import com.supyuan.util.HttpsUtils;

public class HotelJob extends BaseJob {
	
	private static final Logger log = Logger.getLogger(HotelJob.class);

	/***
	 * 获取酒店列表数据(只执行一次)
	 * @param xmlInfo
	 * @return
	 */
	public String hotelsGet(String xmlInfo) {
		 Thread thread = new Thread(new Runnable(){
			 public void run(){
				System.out.println("*************************hotelsGet开始获取酒店列表数据*****************");
				 try {
						String result = HttpUtils.HttpClientPost(xmlInfo);
						InputStream stream = new ByteArrayInputStream(result.getBytes());
						boolean res = HttpUtils.ParseHotelsXml(stream);
						if(!res)
						{
							System.out.println("解析数据失败");
						}
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				System.out.println("*************************hotelsGet获取酒店列表数据结束*****************");
			 }
		 });  
		 thread.start();
	    return "";
	}

	/***
	 * 获取酒店详情数据(每天执行)
	 * @param xmlInfo
	 * @return
	 */
    public String hotelsInfoGet() {
    	 Thread thread = new Thread(new Runnable(){
			 public void run(){
	    		//获取已经存在code,但是不存在详细信息的酒店
		        List<Record> hotels = Db.find("select s1.hot_codcobol from sys_hotels s1 "
		        		+ "LEFT JOIN sys_hotels_info s2 on s1.hot_codcobol = s2.codigo_hotel "
		        		+ "where s2.codigo_hotel is null");
        		System.out.println("*************************hotelsInfoGet开始获取酒店详细信息*************酒店数目：****"+hotels.size());
        		if(hotels != null && hotels.size() > 0)
		        {
        			int index = 0; //每3000次就结束
		        	for (Record record : hotels) 
		        	{
		        		if(index > 3000)
		        		{
		        			return;
		        		}
		        		try {
							String hot_codcobol = record.getStr("hot_codcobol");
							if(StringUtils.length(hot_codcobol) == 6)
							{
								String xmlinfo = "<peticion><tipo>15</tipo><parametros><codigo>"+ hot_codcobol +"</codigo><idioma>2</idioma></parametros></peticion>";
								String result = HttpUtils.HttpClientPost(xmlinfo);
								InputStream stream = new ByteArrayInputStream(result.getBytes());
								HotelModel model = ParseHotelInfoXml(stream);
								if(null != model)
								{
									Db.batch("insert into sys_hotels_info(codigo_hotel,pais,codigo,hot_afiliacion,nombre_h,direccion,"
											+ "codprovincia,provincia,codpoblacion,poblacion,cp,mail,web,telefono,fotos,plano,"
											+ "desc_hotel,num_habitaciones,como_llegar,tipo_establecimiento,categoria,checkin,"
											+ "checkout,edadnindes,edadninhas,currency,longitude,latitude) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 
											new Object[][]{{model.getCodigo_hotel(),model.getPais(),model.getCodigo(),model.getHot_afiliacion(),
												model.getNombre_h(),model.getDireccion(),model.getCodprovincia(),model.getProvincia(),
												model.getCodpoblacion(),model.getPoblacion(),model.getCp(),model.getMail(),model.getWeb(),
												model.getTelefono(),model.getFotos(),model.getPlano(),model.getDesc_hotel(),model.getNum_habitaciones(),
												model.getComo_llegar(),model.getTipo_establecimiento(),model.getCategoria(),model.getCheckin(),
												model.getCheckout(),model.getEdadnindes(),model.getEdadninhas(),model.getCurrency(),model.getLongitude(),model.getLatitude()}}, 50);
								}
							}
							index++;
						} catch (Exception e) {
							System.err.println("hotelsInfoGet开始获取酒店详细信息异常"+e.getMessage());
						}
					}
		        }
        		else
		        {
        			System.out.println("所有的酒店都已获取到详细信息！");
        			return;
		        }
		        System.out.println("*************************hotelsInfoGet获取酒店详细信息已经结束***********************");
			 }
		 });  
    	thread.start();
        return "";
    }
    
    /**
     * 解析酒店详情 15（XML）
     * @param inputStream
     * @return
     * @throws Exception
     */
    public synchronized HotelModel ParseHotelInfoXml(InputStream inputStream) throws Exception {
        if (inputStream == null){
            return null;
        }
        HotelModel model = new HotelModel();
        model.setCurrency("USD");//默认结算货币为美元
        model.setFotos("");
        model.setWeb("");
        model.setCp("");
        model.setPlano("");
        SAXReader reader = new SAXReader();// 读取输入流
        Document document = reader.read(inputStream);
        inputStream.close();
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
			else if(element.getName().equals("mail"))
			{
				model.setMail(text);
			}
			else if(element.getName().equals("telefono"))
			{
				model.setTelefono(text);
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
        return model;
    }

    /**
     * 获取酒店房型和售卖房型(每天执行)
     * @return
     */
	public String hotelRoomTypeAndRatePlanGet() 
    {
		 Thread thread = new Thread(new Runnable(){
			 public void run(){
				//获取存在详细信息但是没有RoomType的酒店
		    	List<Record> hotels = Db.find("select s1.codigo_hotel,s1.pais from sys_hotels_info s1 LEFT JOIN "
		    			+ "sys_hotels_roomtype s2 on s1.codigo_hotel = s2.hotelcode "
		    			+ "where s2.hotelcode is null");
				if(null != hotels && hotels.size() > 0)
				{
					int index = 0; //每3000次就结束
					for (Record record : hotels) 
					{
						if(index > 3000)
						{
							return;
						}
						String start = DateUtils.getAddDayNow(DateUtils.MDY,0);
						String end = DateUtils.getAddDayNow(DateUtils.MDY,1);
						String[] array = new String[]{"1-0", "2-0", "2-1"};
						for (int i = 0; i < array.length; i++) 
						{
							try 
							{
								String xmlInfo110 = HttpUtils.GetRestelXml110(record.getStr("codigo_hotel"),record.getStr("pais"),"", start, end, "1", array[i],"");
		    					String result110 = HttpUtils.HttpClientPost(xmlInfo110);
								InputStream stream110 = new ByteArrayInputStream(result110.getBytes());
								Parse110XmlWithRTRP(stream110,record.getStr("codigo_hotel"));
							}
							catch (Exception e) 
							{
								System.err.println("hotelRoomTypeAndRatePlanGet获取房型和售卖房型异常"+e.getMessage());
							}
						}
					}
				}
			 }
		});  
    	thread.start();
    	return "";
    }
    
    /**
     * 解析数据获取房型和售卖房型
     * @param inputStream
     * @param hotelcode
     * @throws DocumentException
     * @throws IOException 
     */
    @SuppressWarnings("unchecked")
	public synchronized void Parse110XmlWithRTRP(InputStream inputStream,String hotelcode) throws Exception
    {
    	 if (inputStream == null){
            return;
         }
    	 SAXReader reader = new SAXReader();// 读取输入流
         Document document = reader.read(inputStream);
         inputStream.close();
         Element root = document.getRootElement();// 得到xml根元素
         Element paramElem = root.element("param");// 获取param节点
         if(null != paramElem.element("error"))
         {
        	 System.err.println("Parse110XmlWithRTRP访问Restel接口出现错误！"+paramElem.element("error").asXML());
        	 return;
         }
         Element hotlsElem = paramElem.element("hotls");// 获取hotls 节点
         if(hotlsElem.attribute("num").getText().equals("0"))
         {
         	return;
         }
         Element hot = hotlsElem.element("hot");
         Element res = hot.element("res");
         Element pax = res.element("pax");
         List<Element> paxlist = pax.elements("hab");
         for (Element element : paxlist) 
         {
        	 String rtCode = element.attribute("cod").getText();
        	 List<Element> reglist = element.elements("reg");
        	 for (Element element2 : reglist) {
        		 String rpCode = element2.attribute("cod").getText();
        		 Record rt = Db.findFirst("select id from sys_hotels_roomtype where hotelcode='"+hotelcode+"' and rtcode='"+rtCode+"' and rpcode='"+rpCode+"'");
        		 if(null == rt)
        		 {
        			 Db.update("insert into sys_hotels_roomtype(hotelcode,rtcode,rpcode) values('"+hotelcode+"','"+rtCode+"','"+rpCode+"')");
        		 }
        	 }	 
         }
    }
    
    /**
     * 获取已推送酒店，维护价格，使价格保持最新状态(每天都要执行)
     * 
     * 0默认，1 push,2 pending 3 Active
     * @return
     */
	public String hotelRoomRateWH()
    {
		 Thread thread = new Thread(new Runnable(){
			 public void run(){
				//获取status=3的酒店
		    	List<Record> hotels = Db.find("select s1.hot_codcobol,s2.pais,s2.tj,s2.usdrate from sys_hotels s1 "
		    			+ "inner join sys_hotels_info s2 on s1.hot_codcobol = s2.codigo_hotel "
		    			+ "and s1.status = 3");
				String ctrip_api_url = Config.getStr("ctrip_api_url");
				if(null != hotels && hotels.size() > 0)
				{
					for (Record hotel : hotels) 
					{
						String hotelcode = hotel.getStr("hot_codcobol");
						String pais = hotel.getStr("pais");
						List<Record> rtlist = Db.find("select rtcode,rtdesc from sys_hotels_roomtype where hotelcode = '"+hotelcode+"' and rtstatus = 3");
						if(null != rtlist && rtlist.size() > 0)
						{
							for (Record rt : rtlist) 
							{
								String rtcode = rt.getStr("rtcode");
								String rtdesc = rt.getStr("rtdesc");
								List<Record> rplist = Db.find("select rpcode from sys_hotels_saletype "
										+ "where hotelcode = '"+hotelcode+"' and rtcode = '"+rtcode+"' and rpstatus = 3");
								if(null != rplist && rplist.size() > 0)
								{
									for (Record rp : rplist) 
									{
										try
										{
											String rpcode = rp.getStr("rpcode");
											for (int i = 0; i < 10; i++) 
											{
												String start = DateUtils.getAddDayNow(DateUtils.MDY,i*9);
												String end = DateUtils.getAddDayNow(DateUtils.MDY,(i+1)*9);
												String xmlInfo110 = HttpUtils.GetRestelXml110(hotelcode, pais, "", start, end, "1", "2-0","");
						    					String result110 = HttpUtils.HttpClientPost(xmlInfo110);
												InputStream stream110 = new ByteArrayInputStream(result110.getBytes());
												HttpUtils.Parse110XmlWithRR(stream110,hotelcode,rtcode,rpcode,rtdesc,hotel.getDouble("usdrate"),hotel.getDouble("tj"),ctrip_api_url);
											}
										}catch(Exception e)
										{
											log.error("hotelRoomRateWH维护房价异常"+e.getMessage());
										}
									}
								}
							}
						}
					}
				} 
			 }
		});  
	    thread.start();
    	return "";
    }
	
	 /**
     * 获取未推送酒店的价格(每天都要执行)
     * 0默认，1 push,2 pending 3 Active
     * @return
     */
	public String hotelRoomRateGet()
    {
		Thread thread = new Thread(new Runnable(){
			 public void run(){
				//获取status=0的酒店
		    	List<Record> hotels = Db.find("select s1.hot_codcobol,s2.pais from sys_hotels s1 "
		    			+ "inner join sys_hotels_info s2 on s1.hot_codcobol = s2.codigo_hotel "
		    			+ "and s1.status = 0");
				String ctrip_api_url = Config.getStr("ctrip_api_url");
				if(null != hotels && hotels.size() > 0)
				{
					for (Record hotel : hotels) 
					{
						String hotelcode = hotel.getStr("hot_codcobol");
						String pais = hotel.getStr("pais");
						List<Record> rtlist = Db.find("select rtcode,rtdesc from sys_hotels_roomtype where hotelcode = '"+hotelcode+"'");
						if(null != rtlist && rtlist.size() > 0)
						{
							for (Record rt : rtlist) 
							{
								String rtcode = rt.getStr("rtcode");
								String rtdesc = rt.getStr("rtdesc");
								List<Record> rplist = Db.find("select rpcode from sys_hotels_roomtype "
										+ "where hotelcode = '"+hotelcode+"' and rtcode = '"+rtcode+"'");
								if(null != rplist && rplist.size() > 0)
								{
									for (Record rp : rplist) 
									{
										try
										{
											String rpcode = rp.getStr("rpcode");
											for (int i = 0; i < 10; i++) 
											{
												String start = DateUtils.getAddDayNow(DateUtils.MDY,i*9);
												String end = DateUtils.getAddDayNow(DateUtils.MDY,(i+1)*9);
												String xmlInfo110 = HttpUtils.GetRestelXml110(hotelcode, pais, "", start, end, "1", "2-0","");
						    					String result110 = HttpUtils.HttpClientPost(xmlInfo110);
												InputStream stream110 = new ByteArrayInputStream(result110.getBytes());
												HttpUtils.Parse110XmlWithRR(stream110,hotelcode,rtcode,rpcode,rtdesc,hotel.getDouble("usdRate"),hotel.getDouble("tj"),ctrip_api_url);
											}
										}catch(Exception e)
										{
											System.out.println("hotelRoomRateWH 维护已推送酒店价格异常"+e.getMessage());
										}
									}
								}
							}
						}
					}
				}
			 }
		});  
	    thread.start();
    	return "";
    }
    
    /**
	 * 获取酒店的状态数据（完）
	 * 0默认，1 push,2 pending 3 Active
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String ctripHotelStatusGet()
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
					//存在酒店详情，且状态为已推送状态的酒店 0默认，1 pushed 2 pending 3 Active 4 Failed
					List<Record> hotels = Db.find("select s2.codigo_hotel from sys_hotels s1 "
							+ "inner join sys_hotels_info s2 on s1.hot_codcobol = s2.codigo_hotel "
							+ "and (s1.status = 1 or s1.status = 2 or s1.status = 3 or s1.status = 4)");
					if(null != hotels && hotels.size() > 0)
					{
						for (Record hotel : hotels) 
						{
							try
							{
								String ctrip_api_url = Config.getStr("ctrip_api_url");
								String hotelStatus = HttpUtils.PushCtripXmlSearch(hotel.getStr("codigo_hotel"));
								log.info("ctripHotelStatusGet携程状态请求报文："+hotelStatus);
								String resultStatus = HttpsUtils.doPost(ctrip_api_url, hotelStatus);
								if(null != resultStatus)
								{
									log.info("ctripHotelStatusGet携程状态响应报文："+resultStatus);
									InputStream streamStatus = new ByteArrayInputStream(resultStatus.getBytes());
									if (null != streamStatus)
									{
										 SAXReader reader = new SAXReader();// 读取输入流
										 reader.setEncoding("utf-8");
										 Document document = reader.read(streamStatus);
										 streamStatus.close();
								         Element root = document.getRootElement();// 得到xml根元素  
								         Element Body = root.element("Body");
										 Element OTA_HotelStatsNotifRS = Body.element("OTA_HotelStatsNotifRS");
										 if(null != OTA_HotelStatsNotifRS.element("Success"))
								         {
								        	 Element TPA_Extensions = OTA_HotelStatsNotifRS.element("TPA_Extensions"); 
								        	 Element Hotels = TPA_Extensions.element("Hotels");
								        	 Element Hotel = Hotels.element("Hotel");
								        	 String hotelcode = Hotel.attributeValue("HotelCode");
								        	 String ctripHotelCode = Hotel.attributeValue("CtripHotelCode");
								        	 String Status = Hotel.attributeValue("Status");
								        	 int st = 1;
								        	 if(Status.equals("Pending"))
								        	 {
								        		 st = 2;
								        	 }
								        	 else if(Status.equals("Active"))
								        	 {
								        		 st = 3;
								        	 }
								        	 else if(Status.equals("Failed"))
								        	 {
								        		 st = 4;
								        		 String ctripinfo = Hotel.element("Errors").element("Error").attributeValue("ShortText");
								        		 String sql = "update sys_hotels set ctripinfo = '"+ctripinfo+"',status = 0  where hot_codcobol = '"+hotelcode+"'";
								        		 Db.update(sql);
								        	 }
								        	 if(st>0)
								        	 {
								        		 String sql = "update sys_hotels set ctripHotelCode = '"+ctripHotelCode+"',status ="+st+"  where hot_codcobol = '"+hotelcode+"'";
								        		 Db.update(sql);
								            	 Element BasicRooms = Hotel.element("BasicRooms");
								            	 if(null != BasicRooms)
								            	 {
													List<Element> BasicRoom = BasicRooms.elements("BasicRoom");
								            		 if(null != BasicRoom && BasicRoom.size() > 0)
								            		 {
								            			 for (Element element : BasicRoom) {
								    						String rtcode = element.attributeValue("RoomCode");
								    						String Status1 = element.attributeValue("Status");
								    						String CtripRoomCode = element.attributeValue("CtripRoomCode");
								    						int st1 = 0;
								    						if(Status1.equals("Pending"))
								    			        	{
								    			        		st1 = 2;
								    			        	}
								    			        	else if(Status1.equals("Active"))
								    			        	{
								    			        		st1 = 3;
								    			        	}
								    			        	else if(Status.equals("Failed"))
								    			        	{
								    			        		st1 = 4;
								    			        	}
								    						if(st1 > 0)
								    						{
								    							String sql2 = "update sys_hotels_roomtype set rtstatus = "+st1+",rtID='"+CtripRoomCode+"' where hotelcode = '"+hotelcode+"' and rtcode = '"+rtcode+"'";
								    							Db.update(sql2);
								    							Element RatePlans = element.element("RatePlans");
								        			        	if(null != RatePlans)
								        			        	{
																	List<Element> RatePlan = RatePlans.elements("RatePlan");
								        			        		if(null != RatePlan && RatePlan.size() > 0)
								        			        		{
								        			        			for (Element element2 : RatePlan) 
								        			        			{
								        			        				String rpcode = element2.attributeValue("RatePlanCode");
									        								Status = element2.attributeValue("Status");
									        								String CtripRatePlanCode = element2.attributeValue("CtripRatePlanCode");
									        								int st2 = 0;
									        								if(Status.equals("Pending"))
									        						        {
									        									st2 = 2;
									        						        }
									        						        else if(Status.equals("Active"))
									        						        {
									        						        	st2 = 3;
									        						        }
									        						        else if(Status.equals("Failed"))
									        					        	{
									        					        		 st2 = 4;
									        					        	}
									        								if(st2 > 0)
									        								{
									        									String sql3 = "update sys_hotels_saletype set rpstatus ="+st2+",rpID='"+CtripRatePlanCode+"' where hotelcode = '"+hotelcode+"' and rtcode = '"+rtcode+"' and rpcode = '"+rpcode+"'";
									        									Db.update(sql3);
									        								}
																		}
								        			        		} 
								        			        	}
								    						}
								    					}
								            		 }
								            	 }
								        	 } 
								         }
									}
								}
							}
							catch(Exception e)
							{
								log.error("GetCtripHotelStatus获取携程酒店和房型状态异常"+e.getMessage());
							}
						}
					}
				}
		});
		thread.start();
		return "";
	}
}
