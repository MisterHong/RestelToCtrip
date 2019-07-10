package com.supyuan.system.hotel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.supyuan.jfinal.base.BaseService;

public class HotelSvc extends BaseService {	
	private static final Logger log = Logger.getLogger(HotelController.class);
	 /**
     * 解析数据获取房型和售卖房型
     * @param inputStream
     * @param hotelcode
     * @throws DocumentException
     * @throws IOException 
     */
    @SuppressWarnings("unchecked")
	public void Parse110XmlWithRTRP(InputStream inputStream,String hotelcode) throws Exception
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
        	 log.error("Parse110XmlWithRTRP访问Restel接口出现错误！"+paramElem.element("error").asXML());
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
         for (Element hab : paxlist) 
         {
        	 String rtcod = hab.attributeValue("cod");
    		 String rtdesc = hab.attributeValue("desc");
        	 List<Element> regList = hab.elements("reg");
        	 for (Element reg : regList) 
        	 {
        		 String rpcod = reg.attributeValue("cod");
        		 String prr = reg.attributeValue("prr");   		
    			 Record record = Db.findFirst("select id from sys_hotels_temp_roomtype where hotelcode='"+hotelcode+"' and rtcode='"+rtcod+"' and rpcode='"+rpcod+"' and prr='"+prr+"' and rtdesc='"+rtdesc+"'");
        		 if(null == record)
        		 {
        			 Db.update("insert into sys_hotels_temp_roomtype(hotelcode,rtcode,rtdesc,rpcode,prr) values('"+hotelcode+"','"+rtcod+"','"+rtdesc+"','"+rpcod+"','"+prr+"')");
        		 } 
			 }
         }
    }
}
