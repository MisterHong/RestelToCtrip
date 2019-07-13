package com.supyuan.system.hotel;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.supyuan.component.base.BaseProjectController;
import com.supyuan.jfinal.component.annotation.ControllerBind;
import com.supyuan.jfinal.component.db.SQLUtils;
import com.supyuan.system.menu.SysMenu;
import com.supyuan.util.Config;
import com.supyuan.util.DateUtils;
import com.supyuan.util.HttpUtils;
import com.supyuan.util.HttpsUtils;
import com.supyuan.util.StrUtils;

/**
 * 菜单
 * 
 * @author yaosir 2019-2-24
 */
@ControllerBind(controllerKey = "/system/hotel")
public class HotelController extends BaseProjectController {
	
	private static final String path = "/pages/system/hotel/hotel_";
	private static final Logger log = Logger.getLogger(HotelController.class);
	HotelSvc svc = new HotelSvc();

	public void index() {
		list();
	}
	
	/**
	 * 酒店列表查询
	 */
	public void list() {
		String search = getPara();
		String ctryCode = getPara("ctry");
		String provCode = getPara("prov");
		Integer ps = getParaToInt("ps",0);
		SQLUtils sql = new SQLUtils(" from sys_hotels_info s1 inner join sys_hotels s2 on s1.codigo_hotel = s2.hot_codcobol where 1 = 1");
		if (StrUtils.isNotEmpty(search)) {
			try {
				search = URLDecoder.decode(search,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.error(e.getMessage());
			}
			sql.whereLike("nombre_h", search);
		}
		if(StrUtils.isNotEmpty(ctryCode) && !("-1").equals(ctryCode))
		{
			sql.whereEquals("pais", ctryCode);
		}
		if(StrUtils.isNotEmpty(provCode) && !("-1").equals(provCode))
		{
			sql.whereEquals("codprovincia", provCode);
		}
		sql.whereEquals("s2.status", ps);
		Page<SysMenu> page = SysMenu.dao.paginate(getPaginator(), "select s1.codigo_hotel,s1.nombre_h,s1.longitude,s1.latitude,s1.direccion,s2.status,s2.ctripHotelCode,s1.usdrate,s1.tj,s2.ctripinfo,s1.remark",
				sql.toString().toString());
		
		Record rate = Db.findFirst("select usdRate from sys_rate order by id desc");
		
		log.info("查询sql------select s1.codigo_hotel,s1.nombre_h,s1.longitude,s1.latitude,s1.direccion,s2.status,s2.ctripHotelCode,s1.usdrate,s1.tj,s2.ctripinfo"+sql.toString());
		setAttr("country", Db.find("select dict_id,dict_name from sys_dict"));
        setAttr("province", Db.find("select dict_type,detail_name from sys_dict_detail"));
        setAttr("ps", ps);
		setAttr("page", page);
		setAttr("nombre_h", search);
		setAttr("rate", rate);
		render(path + "list.html");
	}
	
	/**
	 * 推送酒店
	 */
	public void pushHotel()
	{
		Double moy = Double.parseDouble(getPara("moy","0.0"));
		double usdrate =  Double.parseDouble(getPara("usdrate","1.0"));
		String codigo_hotel = getPara("codigo_hotel");
		try
		{
			String ctrip_api_url = Config.getStr("ctrip_api_url");
			Record hotelInfoRecord = Db.findById("sys_hotels_info", "codigo_hotel", codigo_hotel);
			if(null != hotelInfoRecord)
			{
				Record hotelRecord = Db.findFirst("select hot_codcobol from sys_hotels where hot_codcobol = '"+codigo_hotel+"' and status = 3");
				if(null == hotelRecord)
				{
					//推送酒店信息
			 		String hotelstaticinfo = HttpUtils.PushCtripXmlHotelInfo(hotelInfoRecord, codigo_hotel);
			 		log.info("酒店："+codigo_hotel+"，推送酒店报文："+hotelstaticinfo);
			 		String result = HttpsUtils.doPost(ctrip_api_url, hotelstaticinfo);
			 		log.info("酒店："+codigo_hotel+"，推送酒店信息获取到的携程数据结果："+result);
					if(result.contains("Success"))
					{
						log.info("酒店："+codigo_hotel+"，推送酒店成功Success");
						//更新状态
			        	Db.update("update sys_hotels set status = 1  where hot_codcobol = '"+codigo_hotel+"'");
			        	//更新汇率和抬价
						Db.update("update sys_hotels_info set tj = " + moy + ",usdrate = "+usdrate+" where codigo_hotel = '"+codigo_hotel+"'");
					}
				}
				else
				{
					//更新抬价
					Db.update("update sys_hotels_info set tj = " + moy + " where codigo_hotel = '"+codigo_hotel+"'");
				}
			}
		}
		catch(Exception e)
		{
			log.error("推送酒店"+codigo_hotel+"异常"+e.getMessage());
		}
		redirect("/system/hotel/list");
	}
	
	/**
	 * 获取基础房型和售卖房型
	 */
	public void GetRTRP()
	{
		String codigo_hotel = getPara();
		Record hotelInfoRecord = Db.findById("sys_hotels_info", "codigo_hotel", codigo_hotel);
		//先删除临时数据
		Db.update("delete from sys_hotels_temp_roomtype where hotelcode='"+codigo_hotel+"'");
		try {
	    	//开始和结束日期都向后
	        String start = DateUtils.getAddDayNow("MM/dd/yyyy", 20);
	        String end = DateUtils.getAddDayNow("MM/dd/yyyy", 21);
            String xmlInfo110 = HttpUtils.GetRestelXml110(hotelInfoRecord.getStr("codigo_hotel"), hotelInfoRecord.getStr("pais"), "", start, end, "1", "2-0", "");
            log.info("获取酒店："+codigo_hotel+"房型请求报文："+xmlInfo110);
            String result110 = HttpUtils.HttpClientPost(xmlInfo110);
            log.info("获取酒店："+codigo_hotel+"房型响应报文："+result110);
            InputStream stream110 = new ByteArrayInputStream(result110.getBytes());
            svc.Parse110XmlWithRTRP(stream110, hotelInfoRecord.getStr("codigo_hotel"));
		} catch (Exception e) 
		{
			log.error("获取酒店："+codigo_hotel+"房型时出错，原因为："+e.getMessage());
		}
		
		SQLUtils sql = new SQLUtils(" from sys_hotels_temp_roomtype where hotelcode = '"+codigo_hotel+"'");
		Page<SysMenu> page = SysMenu.dao.paginate(getPaginator(), "select *",
				sql.toString().toString());
		setAttr("page", page);
		render(path + "rplist.html");
	}
	
	/**
	 * 推送基础房型
	 */
	public void pushBasicRoomType()
	{
		String codigo_hotel = getPara("codigo_hotel");
		String rtcode = getPara("rtcode");
		String rtdesc = getPara("rtdesc");
		String ctripdesc = getPara("ctripdesc");
		String ctrip_api_url = Config.getStr("ctrip_api_url");
		try {
			String sql = "select * from sys_rome_type where roomName = '"+rtcode+"'";
			Record rtRecord = Db.findFirst(sql);
			int adults = 2;
			int childs = 0;
			if(null != rtRecord)
			{
				adults = rtRecord.getInt("adults");
				childs = rtRecord.getInt("childs");
			}
			Record tempRecord = Db.findFirst("select id from sys_hotels_roomtype where hotelcode = '"+codigo_hotel+"' and rtcode = '"+rtcode+"'");
			if(null == tempRecord)
			{
				String xmlrt = HttpUtils.PushCtripXmlRoomType(codigo_hotel,rtcode,String.valueOf(adults),String.valueOf(childs),ctripdesc,"CNY",rtdesc);
				log.info("推送酒店："+codigo_hotel+"，基础房型携程请求报文："+xmlrt);
				String resultrt = HttpsUtils.doPost(ctrip_api_url, xmlrt);
				log.info("推送酒店："+codigo_hotel+"，基础房型携程响应报文："+resultrt);
				InputStream streamrt = new ByteArrayInputStream(resultrt.getBytes());
				if(HttpUtils.ParseCtripCMXml(streamrt).contains("Success"))
				{
					log.info("酒店："+codigo_hotel+"，基础房型数据已经推送至携程Success");
					Date now = new Date();
			        SimpleDateFormat fs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			        String dateStr = fs.format(now);
					Db.update("insert into sys_hotels_roomtype(hotelcode,rtcode,rtdesc,ctriprtdesc,adults,rtstatus,createtime) values('"+codigo_hotel+"','"+rtcode+"','"+rtdesc+"','"+ctripdesc+"',"+adults+",1,'"+dateStr+"')");
				}
			}
			else
			{
				renderJson("酒店："+codigo_hotel+"基础房型("+rtcode+")已存在,重复推送房型!");	
			}
		} catch (Exception e) {
			log.error("推送酒店："+codigo_hotel+"基础房型时出错，原因为："+e.getMessage());
			renderJson("推送酒店："+codigo_hotel+"基础房型时失败!");
			return;
		}
		renderJson("Success");
	}
	
	/**
	 * 推送售卖房型
	 */
	public void pushSaleType()
	{
		String codigo_hotel = getPara("codigo_hotel");
		String rtcode = getPara("rtcode");
		String rpcode = getPara("rpcode");
		String ctrip_api_url = Config.getStr("ctrip_api_url");
		try {
			String sql = "select * from sys_rome_type where roomName = '"+rpcode+"'";
			Record rtRecord = Db.findFirst(sql);
			int adults = 2;
			if(null != rtRecord)
			{
				adults = rtRecord.getInt("adults");
			}
			Record tempRecord = Db.findFirst("select id from sys_hotels_saletype where hotelcode = '"+codigo_hotel+"' and rtcode = '"+rtcode+"' and rpcode = '"+rpcode+"'");
			if(null == tempRecord)
			{
				String xmlsubrt = HttpUtils.PushCtripXmlRatePlan(codigo_hotel,rpcode,rtcode,String.valueOf(adults));
				log.info("推送酒店："+codigo_hotel+"，售卖房型携程请求报文："+xmlsubrt);
				String resultsubrt =  HttpsUtils.doPost(ctrip_api_url, xmlsubrt);
				log.info("推送酒店："+codigo_hotel+"，售卖房型携程响应报文："+resultsubrt);
				InputStream streamsubrt = new ByteArrayInputStream(resultsubrt.getBytes());
				if(HttpUtils.ParseCtripCMXml(streamsubrt).contains("Success"))
				{
					log.info("酒店："+codigo_hotel+"，售卖房型数据已经推送至携程Success");
					Date now = new Date();
			        SimpleDateFormat fs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			        String dateStr = fs.format(now);
					Db.update("insert into sys_hotels_saletype(hotelcode,rtcode,rpcode,adults,rpstatus,createtime) values('"+codigo_hotel+"','"+rtcode+"','"+rpcode+"',"+adults+",1,'"+dateStr+"')");
				}
			}
			else
			{
				renderJson("酒店："+codigo_hotel+"售卖房型("+rpcode+")已存在,重复推送房型!");	
			}
		} catch (Exception e) {
			log.error("推送酒店："+codigo_hotel+"售卖房型时出错，原因为："+e.getMessage());
			renderJson("推送酒店："+codigo_hotel+"售卖房型时失败!");
			return;
		}
		renderJson("Success");
	}
	
	/**
	 * 推送房态
	 */
	public void pushRoomState()
	{
		String codigo_hotel = getPara("codigo_hotel");
		String rtcode = getPara("rtcode");
		String rpcode = getPara("rpcode");
		int st = getParaToInt("status",1);
		String ctrip_api_url = Config.getStr("ctrip_api_url");
		try {
			//String sql = "select * from sys_hotels_roomtype s1 inner join sys_hotels_saletype s2 on s1.rtcode = s2.rtcode where s1.rtstatus = 3 and s2.rpstatus = 3 and s1.hotelcode = '"+codigo_hotel+"'";
			String ctripStarTime = DateUtils.getAddDayNow(DateUtils.YMD,0);
			String ctripEndTime = DateUtils.getAddDayNow(DateUtils.YMD,90);
			String status = "Open";//Close
			if(2 == st)
			{
				status = "Close";
			}
			String xmlRomeStatus = HttpUtils.PushCtripXmlRoomStatus(codigo_hotel, rtcode, rpcode, status,ctripStarTime, ctripEndTime);
			log.info("推送酒店："+codigo_hotel+"，房态"+status+"携程请求报文："+xmlRomeStatus);
			String resultRS = HttpsUtils.doPost(ctrip_api_url, xmlRomeStatus);
			log.info("推送酒店："+codigo_hotel+"，房态"+status+"携程响应报文："+resultRS);
			InputStream streamsubRS = new ByteArrayInputStream(resultRS.getBytes());
			if(HttpUtils.ParseCtripCMXml(streamsubRS).contains("Success"))
			{
				log.info("酒店："+codigo_hotel+"，房态"+status+"数据已经推送至携程Success");
				Date now = new Date();
		        SimpleDateFormat fs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		        String dateStr = fs.format(now);
				Record tempRecord = Db.findFirst("select id from sys_hotels_roomstatus where hotelcode = '"+codigo_hotel+"' and rtcode = '"+rtcode+"' and rpcode = '"+rpcode+"'");
				if(null == tempRecord)
				{
					Db.update("insert into sys_hotels_roomstatus(hotelcode,rtcode,rpcode,status,createtime) values('"+codigo_hotel+"','"+rtcode+"','"+rpcode+"',"+st+",'"+dateStr+"')");
				}
				else
				{
					Db.update("update sys_hotels_roomstatus set status = "+st+",altertime = '"+dateStr+"' where hotelcode = '"+codigo_hotel+"' and rtcode = '"+rtcode+"' and rpcode = '"+rpcode+"'");
				}
			}
		} catch (Exception e) {
			log.error("推送酒店："+codigo_hotel+"房态时出错，原因为："+e.getMessage());
			renderJson("推送酒店："+codigo_hotel+"房态时失败!");
			return;
		}
		renderJson("Success");
	}
	
	/**
	 * 推送房价
	 */
	public void pushHousePrice()
	{
		String codigo_hotel = getPara("codigo_hotel");
		String rtcode = getPara("rtcode");
		//String rtdesc = getPara("rtdesc");
		String rpcode = getPara("rpcode");
		Record tempRecord = Db.findFirst("select rtdesc from sys_hotels_roomtype where hotelcode = '"+codigo_hotel+"' and rtcode = '"+rtcode+"'");
		String rtdesc = tempRecord.getStr("rtdesc");
		String ctrip_api_url = Config.getStr("ctrip_api_url");
		log.info("推送酒店："+codigo_hotel+"，基础，售卖，描述"+rtcode+"\t"+rpcode+"\t"+rtdesc);
		try {
			Record hotelInfoRecord = Db.findById("sys_hotels_info", "codigo_hotel", codigo_hotel);
			//推送价格信息
			for (int i = 0; i < 6; i++) 
			{
				String start = DateUtils.getAddDayNow(DateUtils.MDY,i*15);
				String end = DateUtils.getAddDayNow(DateUtils.MDY,(i+1)*15);
				String xmlInfoRR = HttpUtils.GetRestelXml110(codigo_hotel, hotelInfoRecord.getStr("pais"), "", start, end, "1", "2-0","");
				log.info("推送酒店："+codigo_hotel+"，房价供应商请求报文："+xmlInfoRR);
				String resultRR = HttpUtils.HttpClientPost(xmlInfoRR);
				log.info("推送酒店："+codigo_hotel+"，房价供应商响应报文："+resultRR);
				InputStream streamRR = new ByteArrayInputStream(resultRR.getBytes());
				HttpUtils.Parse110XmlWithRR(streamRR,codigo_hotel,rtcode,rpcode,rtdesc,hotelInfoRecord.getDouble("usdrate"),hotelInfoRecord.getDouble("tj"),ctrip_api_url);
			}
		} catch (Exception e) 
		{
			log.error("推送酒店："+codigo_hotel+"房价时出错，原因为："+e.getMessage());
			renderJson("推送酒店："+codigo_hotel+"房价时失败!");
			return;
		}
		renderJson("Success");
	}
	
	/***
	 * 酒店详情-房型列表
	 */
	public void roomDetails()
	{
		String codigo_hotel = getPara("codigo_hotel");
		SQLUtils sql = new SQLUtils(" from sys_hotels_roomtype s1 inner join sys_hotels_saletype s2 on s1.hotelcode = s2.hotelcode and s1.rtcode = s2.rtcode where s1.hotelcode='"+codigo_hotel+"' group by s1.rtcode");
		Page<SysMenu> page = SysMenu.dao.paginate(getPaginator(), "select s1.hotelcode,s1.rtcode,s1.rtdesc,s1.ctriprtdesc,s1.rtstatus,s1.rtID,s2.rpcode,s2.rpstatus,s2.rpID",
				sql.toString().toString());
		setAttr("page", page);
		render(path + "details.html");
	}
	
	/***
	 * 酒店详情-房价列表
	 */
	public void rateDetails()
	{
		String codigo_hotel = getPara("codigo_hotel");
		String rtcode = getPara("rtcode");
		String rpcode = getPara("rpcode");
		String sql = "select * from sys_hotels_roomrate where hotelcode = '"+codigo_hotel+"' and rtcode='"+rtcode+"' and rpcode='"+rpcode+"' order by start desc";
		List<Record> rpricelist = Db.find(sql);
		setAttr("rpricelist", rpricelist);
		render(path + "rprice.html");
	}
	
	/**
	 * 更新所有的汇率
	 */
	public void updateRate()
	{
		String usdRate = getPara("usdRate");
		Date now = new Date();
        SimpleDateFormat fs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = fs.format(now);
		Db.update("insert into sys_rate(usdRate,createtime) values("+usdRate+",'"+dateStr+"')");
		String sql = "update sys_hotels_info set usdrate="+usdRate;
		Db.update(sql);
		redirect("/system/hotel/list");
	}
	
	/**
	 * 更新备注
	 */
	public void updateRemark()
	{
		String codigo_hotel = getPara("codigo_hotel");
		String remark = getPara("remark");
		try {
			remark = URLDecoder.decode(remark, "UTF-8");
			log.info("备注："+remark);
		} catch (UnsupportedEncodingException e) {
			log.info("备注异常："+remark+"\t"+e.getMessage());
		} 
		String sql = "update sys_hotels_info set remark='"+remark+"' where codigo_hotel = '"+codigo_hotel+"'";
		Db.update(sql);
		redirect("/system/hotel/list");
	}
	
	/**
	 * 更新备注
	 */
	public void closeRoomType()
	{
		String codigo_hotel = getPara("codigo_hotel");
		String rtcode = getPara("rtcode");
		String rpcode = getPara("rpcode");
		String ctrip_api_url = Config.getStr("ctrip_api_url");
		String ctripStarTime = DateUtils.getAddDayNow(DateUtils.YMD,0);
		String ctripEndTime = DateUtils.getAddDayNow(DateUtils.YMD,90);
		String status = "Close";//Close
		String xmlRomeStatus = HttpUtils.PushCtripXmlRoomStatus(codigo_hotel, rtcode, rpcode, status,ctripStarTime, ctripEndTime);
		log.info("酒店："+codigo_hotel+"，推送房态报文："+xmlRomeStatus);
		String resultRS = HttpsUtils.doPost(ctrip_api_url, xmlRomeStatus);
		log.info("酒店："+codigo_hotel+"，推送房态获取到的携程数据结果："+resultRS);
		StringBuffer buffer = new StringBuffer();
		buffer.append("酒店："+codigo_hotel+"<br>\n");
		buffer.append("基础房型："+rtcode+"<br>\n");
		buffer.append("售卖房型："+rpcode+"<br>\n");
		buffer.append("开始时间(关闭房型的开始时间)："+ctripStarTime+"<br>\n");
		buffer.append("接收时间(关闭房型的结束时间)："+ctripEndTime+"<br>\n");
		buffer.append("推送房态报文：<xmp>"+xmlRomeStatus+"</xmp><br>\n");
		buffer.append("推送房态获取到的携程数据结果：<xmp>"+resultRS+"</xmp><br>\n");
		renderHtml(buffer.toString());
	}
	
	public void deactivatedRoomType()
	{
		try {
			String ctrip_api_url = Config.getStr("ctrip_api_url");
			List<Record> fqRecordList = Db.find("select * from sys_hotels_rtfq where status = 0");
			for (Record record : fqRecordList) {
				Record tempRecord = Db.findFirst("select hot_codcobol from sys_hotels where ctripHotelCode = '"+record.getStr("ctid")+"'");
				String codigo_hotel = tempRecord.getStr("hot_codcobol");
				if(null == codigo_hotel || "".equals(codigo_hotel))
					continue;
				String rtcode = record.getStr("rtcode");
				String rpcode = record.getStr("rpcode");
				String adults = record.getStr("adults");
				String desc = record.getStr("desc");
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
				xmlinfo.append("    <SellableProduct InvTypeCode=\""+rtcode+"\" InvStatusType=\"Deactivated\">");
				xmlinfo.append("      <GuestRoom>");
				xmlinfo.append("        <Occupancy AgeQualifyingCode=\"10\" MinOccupancy=\"1\" MaxOccupancy=\""+adults+"\"/>");	//成人
				xmlinfo.append("        <Currency Code=\"CNY\"/>");
				xmlinfo.append("        <Description>");
				xmlinfo.append("          <Text Language=\"en-us\">"+desc+"</Text>");
				xmlinfo.append("        </Description>");
				xmlinfo.append("      </GuestRoom>");
				xmlinfo.append("    </SellableProduct>");
				xmlinfo.append("  </SellableProducts>");
				xmlinfo.append("</OTA_HotelInvNotifRQ>");
				xmlinfo.append("</soap:Body>");
				xmlinfo.append("</soap:Envelope>");
				
				String resultrt =  HttpsUtils.doPost(ctrip_api_url, xmlinfo.toString());
				if(resultrt.contains("Success")){
					Db.update("update sys_hotels_rtfq set hid='"+codigo_hotel+"',status=1 where id="+record.getInt("id"));
				}
				log.info("废弃结果："+resultrt);

				StringBuilder xmlinfo2 = new StringBuilder();
		    	xmlinfo2.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
		    	xmlinfo2.append("<soap:Body>");
		    	xmlinfo2.append("<OTA_HotelRatePlanNotifRQ Target=\"Production\" PrimaryLangID=\"en-us\" Version=\"1.0\" TimeStamp=\""+DateUtils.getNowByGMT8()+"\" xmlns=\"http://www.opentravel.org/OTA/2003/05\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
				xmlinfo2.append("  <POS>");
				xmlinfo2.append("    <Source>");
				xmlinfo2.append("    	<RequestorID ID=\"hangye\" MessagePassword=\"hangye_101019\" Type=\"1\">");
				xmlinfo2.append("    		<CompanyName Code=\"C\" CodeContext=\"978\" />");
				xmlinfo2.append("    	</RequestorID>");
				xmlinfo2.append("    </Source>");
				xmlinfo2.append("  </POS>");
				xmlinfo2.append("  <RatePlans HotelCode=\""+codigo_hotel+"\">");
				xmlinfo2.append("    <RatePlan RatePlanCode=\""+rpcode+"\" RatePlanCategory=\"501\" RatePlanStatusType=\"Deactivated\">");
				xmlinfo2.append("    	<BookingRules>");
				xmlinfo2.append("    		<BookingRule MaxTotalOccupancy=\""+adults+"\" />");
				xmlinfo2.append("    	</BookingRules>");
				xmlinfo2.append("    	<SellableProducts>");
				xmlinfo2.append("    		<SellableProduct InvTypeCode=\""+rtcode+"\" />");
				xmlinfo2.append("    	</SellableProducts>");
				xmlinfo2.append("    </RatePlan>");
				xmlinfo2.append("  </RatePlans>");
				xmlinfo2.append("</OTA_HotelRatePlanNotifRQ>");
				xmlinfo2.append("</soap:Body>");
				xmlinfo2.append("</soap:Envelope>");
				
				String resultsubrt =  HttpsUtils.doPost(ctrip_api_url, xmlinfo2.toString());
				if(resultsubrt.contains("Success")){
					Db.update("update sys_hotels_rtfq set status=2 where id="+record.getInt("id"));
				}
				log.info("废弃结果2："+resultsubrt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) 
	{
		String ctrip_api_url = Config.getStr("ctrip_api_url");
		String codigo_hotel = "162307";
		String rtcode = "02";
		String rpcode = "OB";
		String adults = "4";
		String desc = "Double or Twin SUPERIOR";
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
		xmlinfo.append("    <SellableProduct InvTypeCode=\""+rtcode+"\" InvStatusType=\"Deactivated\">");
		xmlinfo.append("      <GuestRoom>");
		xmlinfo.append("        <Occupancy AgeQualifyingCode=\"10\" MinOccupancy=\"1\" MaxOccupancy=\""+adults+"\"/>");	//成人
		xmlinfo.append("        <Currency Code=\"CNY\"/>");
		xmlinfo.append("        <Description>");
		xmlinfo.append("          <Text Language=\"en-us\">"+desc+"</Text>");
		xmlinfo.append("        </Description>");
		xmlinfo.append("      </GuestRoom>");
		xmlinfo.append("    </SellableProduct>");
		xmlinfo.append("  </SellableProducts>");
		xmlinfo.append("</OTA_HotelInvNotifRQ>");
		xmlinfo.append("</soap:Body>");
		xmlinfo.append("</soap:Envelope>");
		
//		String resultrt =  HttpsUtils.doPost(ctrip_api_url, xmlinfo.toString());
//		log.info("废弃结果："+resultrt);

		
		StringBuilder xmlinfo2 = new StringBuilder();
    	xmlinfo2.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
    	xmlinfo2.append("<soap:Body>");
    	xmlinfo2.append("<OTA_HotelRatePlanNotifRQ Target=\"Production\" PrimaryLangID=\"en-us\" Version=\"1.0\" TimeStamp=\""+DateUtils.getNowByGMT8()+"\" xmlns=\"http://www.opentravel.org/OTA/2003/05\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
		xmlinfo2.append("  <POS>");
		xmlinfo2.append("    <Source>");
		xmlinfo2.append("    	<RequestorID ID=\"hangye\" MessagePassword=\"hangye_101019\" Type=\"1\">");
		xmlinfo2.append("    		<CompanyName Code=\"C\" CodeContext=\"978\" />");
		xmlinfo2.append("    	</RequestorID>");
		xmlinfo2.append("    </Source>");
		xmlinfo2.append("  </POS>");
		xmlinfo2.append("  <RatePlans HotelCode=\""+codigo_hotel+"\">");
		xmlinfo2.append("    <RatePlan RatePlanCode=\""+rpcode+"\" RatePlanCategory=\"501\" RatePlanStatusType=\"Deactivated\">");
		xmlinfo2.append("    	<BookingRules>");
		xmlinfo2.append("    		<BookingRule MaxTotalOccupancy=\""+adults+"\" />");
		xmlinfo2.append("    	</BookingRules>");
		xmlinfo2.append("    	<SellableProducts>");
		xmlinfo2.append("    		<SellableProduct InvTypeCode=\""+rtcode+"\" />");
		xmlinfo2.append("    	</SellableProducts>");
		xmlinfo2.append("    </RatePlan>");
		xmlinfo2.append("  </RatePlans>");
		xmlinfo2.append("</OTA_HotelRatePlanNotifRQ>");
		xmlinfo2.append("</soap:Body>");
		xmlinfo2.append("</soap:Envelope>");
		
		String resultsubrt =  HttpsUtils.doPost(ctrip_api_url, xmlinfo2.toString());
		log.info("废弃结果2："+resultsubrt);
//		
		String hotelStatus = HttpUtils.PushCtripXmlSearch(codigo_hotel);
		log.info("ctripHotelStatusGet携程状态请求报文："+hotelStatus);
		String resultStatus = HttpsUtils.doPost(ctrip_api_url, hotelStatus);
		System.out.println(resultStatus);
	}
}
