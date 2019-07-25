package com.supyuan.system.order;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.supyuan.component.base.BaseProjectController;
import com.supyuan.jfinal.component.annotation.ControllerBind;
import com.supyuan.system.hotel.Lin;
import com.supyuan.system.hotel.RomeStay;
import com.supyuan.util.AmountUtil;
import com.supyuan.util.DateUtils;
import com.supyuan.util.HttpUtils;
import com.supyuan.util.StrUtils;

@ControllerBind(controllerKey = "/system/order")
public class OrderCtripController  extends BaseProjectController {

	@SuppressWarnings("unused")
	private static final String path = "/pages/system/order/order_";
	
	public void index() {
	}
	
	/**
	 * check service
	 */
	public void check() {
		// 读取输入流
        System.out.println("*****************************可定检查开始*********************************");
		SAXReader reader = new SAXReader();
		setAttr("TimeStamp",DateUtils.getNowByGMT8());
		OrderCheck check = new OrderCheck();
		try
		{
			Document document = reader.read(getRequest().getInputStream());
	        Element root = document.getRootElement();// 得到xml根元素
	        System.out.println("请求xml:"+root.asXML());
	        Element Body = root.element("Body");
	        Element OTA_HotelAvailRQ = Body.element("OTA_HotelAvailRQ");
	        Element RequestorID = OTA_HotelAvailRQ.element("POS").element("Source").element("RequestorID");
	        String ID = RequestorID.attribute("ID").getText();
	        String MessagePassword = RequestorID.attribute("MessagePassword").getText();
	        if(!(ID.equals("hangye")) || !(MessagePassword.equals("hangye_alex")))
	        {
	        	System.out.println("认证失败！");
	        	renderXml("/pages/template/NoRome.xml");
	        }
	        Element AvailRequestSegments = OTA_HotelAvailRQ.element("AvailRequestSegments");
	        Element AvailRequestSegment = AvailRequestSegments.element("AvailRequestSegment");
	        Element HotelSearchCriteria = AvailRequestSegment.element("HotelSearchCriteria");
	        Element Criterion = HotelSearchCriteria.element("Criterion");
	        Element HotelRef = Criterion.element("HotelRef");
	        String hotelCode = HotelRef.attribute("HotelCode").getText();
	        check.setHotelCode(hotelCode);
	        Element dateRangeElement = Criterion.element("StayDateRange");
	        check.setStart(dateRangeElement.attribute("Start").getText());
	        check.setEnd(dateRangeElement.attribute("End").getText());
	        Element RatePlanCandidates = Criterion.element("RatePlanCandidates");
	        if(null != RatePlanCandidates)
	        {
	        	//case 2
	        	check.setRatePlanCode(RatePlanCandidates.element("RatePlanCandidate").attribute("RatePlanCode").getText());
	        }
	        Element RoomStayCandidate = Criterion.element("RoomStayCandidates").element("RoomStayCandidate");
	        check.setQuantity(RoomStayCandidate.attribute("Quantity").getText()); 		        
	        Attribute roomTypeCode = RoomStayCandidate.attribute("RoomTypeCode");
	        if(null != roomTypeCode)
	        {
	        	//case 3
	        	check.setRoomTypeCode(roomTypeCode.getText());
	        }
	        Element GuestCount = RoomStayCandidate.element("GuestCounts").element("GuestCount");
    		Attribute Count = GuestCount.attribute("Count");
    		if(null != Count)
	        {
	        	check.setCount(Count.getText());
	        }
		}catch(Exception e){
			log.error("数据流转换异常", e);
			renderXml("/pages/template/NoRome.xml");
		}
		
		Record hotelInfoRecord = Db.findFirst("select * from sys_hotels_info where codigo_hotel = "+check.getHotelCode());
		String[] array1 = check.getStart().split("-"); //2019-04-13
		String start = array1[1]+"/"+array1[2]+"/"+array1[0];
		String[] array2 = check.getEnd().split("-"); //2019-04-14
		String end = array2[1]+"/"+array2[2]+"/"+array2[0];
		long ts = DateUtils.between_days(check.getStart(), check.getEnd());
		double usdrate = hotelInfoRecord.getDouble("usdrate");
		double tj = hotelInfoRecord.getDouble("tj");

		Record roomTypeRecord = Db.findFirst("select rtdesc from sys_hotels_roomtype where hotelcode = '"+check.getHotelCode()+"' and rtcode='"+check.getRoomTypeCode()+"'");
	
		//[start] use case 1 and 4 count 无值
		if(null != check && null == check.getCount() &&  null == check.getRoomTypeCode() && null == check.getRatePlanCode())
		{
			StringBuffer xml = new StringBuffer();
			if(null != check.getQuantity())
			{
				//Quantity Number of rooms, maximum value is 20
				int number = Integer.parseInt(check.getQuantity());
				for (int i = 1; i < number; i++) 
				{
					xml.append("<numhab"+i+">1</numhab"+i+">"); //房间类型1
			    	xml.append("<paxes"+i+">2-0</paxes"+i+">"); //房间客人类别
				}
			}
		
			String[] array = new String[]{"1-0", "2-0", "2-1"/*, "3-0", "4-0", "5-0"*/};
			List<RomeStay> roomStays = new ArrayList<RomeStay>();
			if(null != hotelInfoRecord)
			{
				final CountDownLatch countDownLatch = new CountDownLatch(array.length);
				for (int i = 0; i < array.length; i++) 
				{
					String count = array[i];
					new Thread(new Runnable() {
						public void run() {
							try {
								String xmlInfo110 = HttpUtils.GetRestelXml110(check.getHotelCode(),hotelInfoRecord.getStr("pais"),hotelInfoRecord.getStr("codprovincia"), start, end, "1", count,xml.toString());
		    					System.out.println("供应商请求报文："+xmlInfo110);
								String result110 = HttpUtils.HttpClientPost(xmlInfo110);
		    					System.out.println(count+"供应商响应报文："+result110);
								InputStream stream110 = new ByteArrayInputStream(result110.getBytes());
								List<RomeStay> tempList = HttpUtils.Parse110XmlWithRS(stream110,"","","",(int)ts,count,usdrate,tj,start);
								if(null != tempList && tempList.size() > 0)
		    					{
									synchronized (roomStays) {
										roomStays.addAll(tempList);
									}
		    					}
							}
							catch (Exception e) 
							{
								log.info("循环内部出错了！"+e.getMessage());
								renderXml("/pages/template/NoRome.xml");
								return;
							}
							countDownLatch.countDown();
						}
					}).start();
				}
				try {
					countDownLatch.await();
				} catch (InterruptedException e) {
					log.info("countDownLatch.await() 出错了！"+e.getMessage());
					renderXml("/pages/template/NoRome.xml");
				}
				setAttr("roomStays", roomStays);
			}
		}
		//[end]
		//[start] use case 2 and case 3 and 5 count 有值
		else
		{
			if(null != hotelInfoRecord)
			{
				try {
					int count = Integer.parseInt(check.getCount());
					//获取酒店房型的最大入住人数
					Record record = Db.findFirst("select adults from sys_hotels_roomtype where hotelcode ='"+check.getHotelCode()+"' and rtcode='"+check.getRoomTypeCode()+"'");
					int adult = 2;
					if(null != record.getInt("adults"))
					{
						adult = record.getInt("adults");
					}
					String ct = "1-0";
					StringBuffer xml = new StringBuffer();
					if(null != check.getQuantity())
					{
						//Quantity Number of rooms, maximum value is 20
						int number = Integer.parseInt(check.getQuantity());
						for (int i = 1; i < number; i++) 
						{
							xml.append("<numhab"+i+">1</numhab"+i+">"); //房间类型1
					    	xml.append("<paxes"+i+">"+adult+"-0</paxes"+i+">"); //房间客人类别
						}
					}
					else
					{
						if(count > adult)
						{
							ct = adult+"-0";
							int zc = count / adult;
							int ys = count % adult;
							if(ys == 0)
							{
								for (int i = 1; i < zc; i++) 
								{
									xml.append("<numhab"+(i+1)+">1</numhab"+(i+1)+">"); //房间类型1
			    			    	xml.append("<paxes"+(i+1)+">"+adult+"-0</paxes"+(i+1)+">"); //房间客人类别
								}
							}
							else
							{
								int i = 1;
								for (; i < zc; i++) 
								{
									xml.append("<numhab"+(i+1)+">1</numhab"+(i+1)+">"); //房间类型1
			    			    	xml.append("<paxes"+(i+1)+">"+adult+"-0</paxes"+(i+1)+">"); //房间客人类别
								}
								xml.append("<numhab"+(i+1)+">1</numhab"+(i+1)+">"); //房间类型1
		    			    	xml.append("<paxes"+(i+1)+">"+ys+"-0</paxes"+(i+1)+">"); //房间客人类别
							}
						}
						{
							ct = count+"-0";
						}
					}
					
					if(count > adult)
					{
						ct = adult+"-0";
					}
					else
					{
						ct = count+"-0";
					}
					
					String xmlInfo110 = HttpUtils.GetRestelXml110(check.getHotelCode(),hotelInfoRecord.getStr("pais"),hotelInfoRecord.getStr("codprovincia"), start, end, "1", ct,xml.toString());
					System.out.println("供应商请求报文："+xmlInfo110);
					String result110 = HttpUtils.HttpClientPost(xmlInfo110);
					System.out.println("供应商响应报文："+result110);
					InputStream stream110 = new ByteArrayInputStream(result110.getBytes());
					List<RomeStay> roomStays = HttpUtils.Parse110XmlWithRS(stream110,check.getRoomTypeCode(),roomTypeRecord.getStr("rtdesc"),check.getRatePlanCode(), (int)ts, check.getCount(),usdrate,tj,check.getStart());
					if(null == roomStays || roomStays.size() == 0)
					{
						System.out.println("Case 2,roomStays == null 错误,返回无房间！");
						renderXml("/pages/template/NoRome.xml");
					}
					setAttr("roomStays", roomStays);
				} 
				catch (Exception e) 
				{
					log.info("case2 or 3 出错了，默认返回无房！"+e.getMessage());
					renderXml("/pages/template/NoRome.xml");
				}
			}
		}
		System.out.println("*****************************可定检查结束*********************************");
		setAttr("htcode", check.getHotelCode());
		setAttr("htname", hotelInfoRecord.getStr("nombre_h"));
		setAttr("start", check.getStart());
		setAttr("end", check.getEnd());
		setAttr("currency", hotelInfoRecord.getStr("currency"));
		//[end]
		renderXml("/pages/template/orderCheck.xml");
	}
	
	/**
	 * ordernew service
	 */
	@SuppressWarnings("unchecked")
	public void ordernew() {
		try
		{
			System.out.println("*****************************下单开始*********************************");
			SAXReader reader = new SAXReader();
			setAttr("TimeStamp",DateUtils.getNowByGMT8());
			OrderNew orderNew = new OrderNew();
			Document document = reader.read(getRequest().getInputStream());
			Element root = document.getRootElement();
	        System.out.println("请求xml:"+root.asXML());
			Element Body = root.element("Body");
	        Element OTA_HotelResRQ = Body.element("OTA_HotelResRQ");
	        Element RequestorID = OTA_HotelResRQ.element("POS").element("Source").element("RequestorID");
	        String ID = RequestorID.attribute("ID").getText();
	        String MessagePassword = RequestorID.attribute("MessagePassword").getText();
	        if(!(ID.equals("hangye")) || !(MessagePassword.equals("hangye_alex")))
	        {
	        	System.out.println("case 7-8 认证失败,返回Error！");
	        	renderXml("/pages/template/OrderNewError.xml");
	        }
	        String UniqueID = OTA_HotelResRQ.element("UniqueID").attribute("ID").getText();
	        Record orderIsV = Db.findFirst("select id,ResID501,ResID502 from sys_order where ctripUniqueID = "+UniqueID+" and orderState = 2");
	        String warning = "<Warning Type=\"3\">The Special request may not be satisfied</Warning>";
	        if(null != orderIsV)
	        {
	        	warning = "<Warning Type=\"3\" Code=\"127\">This creation is already successfully processed</Warning>";
	        	setAttr("ResID501", orderIsV.getStr("ResID501"));
	        	setAttr("ResID502",  orderIsV.getStr("ResID502"));
	        	System.out.println("重复性订单！");
	        	renderXml("/pages/template/OrderNewSuccess.xml");
	        }
	        setAttr("warning", warning);
	        
	        orderNew.setUniqueID(UniqueID);
	        OrderRoomStay orderRoomStay = new OrderRoomStay();
	        Element RoomStay = OTA_HotelResRQ.element("HotelReservations").element("HotelReservation").element("RoomStays").element("RoomStay");
	        String RoomTypeCode = RoomStay.element("RoomTypes").element("RoomType").attribute("RoomTypeCode").getText();
	        String RatePlansCode = RoomStay.element("RatePlans").element("RatePlan").attribute("RatePlanCode").getText();
	        String HotelCode = RoomStay.element("BasicPropertyInfo").attribute("HotelCode").getText();
	        orderRoomStay.setRoomTypeCode(RoomTypeCode);
	        orderRoomStay.setRatePlanCode(RatePlansCode);
	        orderRoomStay.setHotelCode(HotelCode);
	        Element RoomRate = RoomStay.element("RoomRates").element("RoomRate");
	        String NumberOfUnits = RoomRate.attribute("NumberOfUnits").getText();
	        String EffectiveDate = RoomRate.element("Rates").element("Rate").attribute("EffectiveDate").getText();
	        String ExpireDate = RoomRate.element("Rates").element("Rate").attribute("ExpireDate").getText();
	        String AmountAfterTax = RoomRate.element("Rates").element("Rate").element("Base").attribute("AmountAfterTax").getText();
	        orderRoomStay.setNumberOfUnits(NumberOfUnits);
	        orderRoomStay.setEffectiveDate(EffectiveDate); //start date
	        orderRoomStay.setExpireDate(ExpireDate);	//end date
	        orderRoomStay.setAmountAfterTax(AmountAfterTax);
	        orderNew.setOrderRoomStays(orderRoomStay);
	        Element ResGuest =  OTA_HotelResRQ.element("HotelReservations").element("HotelReservation").element("ResGuests").element("ResGuest");
	        OrderGuest orderGuest = new OrderGuest();
	        if(null != ResGuest.attribute("ArrivalTime"))
	        {
	        	String ArrivalTime = ResGuest.attribute("ArrivalTime").getText();
		        orderGuest.setArrivalTime(ArrivalTime);
	        }
	        Element Customer = ResGuest.element("Profiles").element("ProfileInfo").element("Profile").element("Customer");
	        List<Element> PersonNames = Customer.elements("PersonName");
	        List<Customer> customers = new ArrayList<Customer>();
	        String otherPeople = "";
	        int j = 1;
	        for (Element element : PersonNames) {
	        	Customer persion = new Customer();
	        	if(null != element.attribute("Age"))
	        	{
	        		persion.setAge(element.attribute("Age").getText());
	        	}
	        	persion.setGivenName(element.element("GivenName").getTextTrim());
	        	persion.setSurname(element.element("Surname").getTextTrim());
	        	if(null != element.attribute("Gender"))
	        	{
	        		persion.setGender(element.attribute("Gender").getText());
	        	}
	        	if(null != element.attribute("AgeQualifyingCode"))
	        	{
	        		persion.setAgeQualifyingCode(element.attribute("AgeQualifyingCode").getText());
	        	}
	        	otherPeople+="("+(j++)+")"+element.element("GivenName").getTextTrim()+":"+element.element("Surname").getTextTrim();
	        	persion.setContact(false);
	        	customers.add(persion);
			}
	        Element ContactPerson = Customer.element("ContactPerson");
	        Customer contactPersion = null;
	        if(null != ContactPerson)
	        {
	        	contactPersion = new Customer();
	        	contactPersion.setGivenName(ContactPerson.element("PersonName").element("GivenName").getTextTrim());
	        	contactPersion.setSurname(ContactPerson.element("PersonName").element("Surname").getTextTrim());
	        	if(null != ContactPerson.element("Telephone"))
	        	{
	        		contactPersion.setPhoneNumber(ContactPerson.element("Telephone").attribute("PhoneNumber").getText());
	        	}
	        	if(null != ContactPerson.element("Email"))
	        	{
	        		contactPersion.setEmail(ContactPerson.element("Email").getTextTrim());
	        	}
	        	contactPersion.setContact(true);
	        	customers.add(contactPersion);
	        }
	        orderNew.setOrderGuest(orderGuest);
	        Element ResGlobalInfo = OTA_HotelResRQ.element("HotelReservations").element("HotelReservation").element("ResGlobalInfo");
	        String guestCount = ResGlobalInfo.element("GuestCounts").element("GuestCount").attribute("Count").getText();
	        orderNew.setGuestCount(guestCount);
	        //存在虚拟信用卡
	        if(null != ResGlobalInfo.element("DepositPayments"))
	        {
	        	 Element PaymentCard = ResGlobalInfo.element("DepositPayments").element("GuaranteePayment").element("AcceptedPayments").element("AcceptedPayment").element("PaymentCard");
    		        
    		        if(null != PaymentCard.attribute("CardCode"))
    		        {
    		        	String CardCode = PaymentCard.attribute("CardCode").getText();
    		        	orderNew.setCardCode(CardCode);
    		        }
    		        if(null != PaymentCard.attribute("CardNumber"))
    		        {
    		        	 String CardNumber = PaymentCard.attribute("CardNumber").getText();
    	    		        orderNew.setCardType(CardNumber);
    		        }
    		        if(null != PaymentCard.attribute("CardType"))
    		        {
    		        	String CardType = PaymentCard.attribute("CardType").getText();
	    		        orderNew.setCardType(CardType);
    		        }
    		        if(null != PaymentCard.attribute("ExpireDate"))
    		        {
    		        	String CtripExpireDate = PaymentCard.attribute("ExpireDate").getText();
	    		        orderNew.setCtripExpireDate(CtripExpireDate);
    		        }
    		        if(null != PaymentCard.attribute("CardHolderName"))
    		        {
    		        	 String CardHolderName = PaymentCard.element("CardHolderName").getTextTrim();
    	    		     orderNew.setCardHolderName(CardHolderName);
    		        }
	        }
	        String TotalPay = ResGlobalInfo.element("Total").attribute("AmountAfterTax").getText();
	        orderNew.setTotalPay(TotalPay);
	        String remark = "";
	        Element SpecialRequests = ResGlobalInfo.element("SpecialRequests");
	        if(null != SpecialRequests)
	        {
	        	 Element SpecialRequest = SpecialRequests.element("SpecialRequest");
	        	if(null != SpecialRequest)
	        	{
	        		Element textEle = SpecialRequest.element("Text");
	        		List<Element> ListItem = SpecialRequest.elements("ListItem");
	        		if(null != textEle)
		        	{
	        			remark += textEle.getTextTrim();
		        	}
	        		if(null != ListItem)
	        		{
	        			int index = 1;
		        		for (Element element2 : ListItem) {
		        			remark += (index+":"+element2.getTextTrim()+";");
						}	
	        		}
	        	}
	        }
	        
	        String CurrencyCode = ResGlobalInfo.element("Total").attribute("CurrencyCode").getText();
	        orderNew.setCurrencyCode(CurrencyCode);
	        List<Element> HotelReservationIDs = ResGlobalInfo.element("HotelReservationIDs").elements("HotelReservationID");
	        for (Element element : HotelReservationIDs) {
				String type = element.attribute("ResID_Type").getText();
				if(type.equals("501"))
				{
					orderNew.setResID501(element.attribute("ResID_Value").getText());
					setAttr("ResID501", element.attribute("ResID_Value").getText());
				}
				else if(type.equals("507"))
				{
					orderNew.setResID507(element.attribute("ResID_Value").getText());
				}
			}

	        Record hotelInfoRecord = Db.findFirst("select * from sys_hotels_info where codigo_hotel = "+orderRoomStay.getHotelCode());
			String[] array1 = orderRoomStay.getEffectiveDate().split("-"); //2019-04-13
			String start = array1[1]+"/"+array1[2]+"/"+array1[0];
			String[] array2 = orderRoomStay.getExpireDate().split("-"); //2019-04-14
			String end = array2[1]+"/"+array2[2]+"/"+array2[0];
			long days = DateUtils.between_days(orderRoomStay.getEffectiveDate(), orderRoomStay.getExpireDate());//多少晚
			
			int count = Integer.parseInt(orderNew.getGuestCount());
			//获取酒店房型的最大入住人数
			Record rtrecord = Db.findFirst("select adults from sys_hotels_roomtype where hotelcode ='"+orderRoomStay.getHotelCode()+"' and rtcode='"+orderRoomStay.getRoomTypeCode()+"'");
			int adult = 2;
			if(null != rtrecord.getInt("adults"))
			{
				adult = rtrecord.getInt("adults");
			}
			int nulberUtils = 1;
			String ct = "1-0";
			StringBuffer xml = new StringBuffer();
			if(null != orderNew.getOrderRoomStay().getNumberOfUnits())
			{
				int number = Integer.parseInt(orderNew.getOrderRoomStay().getNumberOfUnits());
				for (int i = 1; i < number; i++) 
				{
					xml.append("<numhab"+i+">1</numhab"+i+">"); //房间类型1
			    	xml.append("<paxes"+i+">"+adult+"-0</paxes"+i+">"); //房间客人类别
				}
				nulberUtils = number;
			}
			else
			{
				if(count > adult)
				{
					int zc = count / adult;
					int ys = count % adult;
					if(ys == 0)
					{
						for (int i = 1; i < zc; i++) 
						{
							xml.append("<numhab"+(i+1)+">1</numhab"+(i+1)+">"); //房间类型1
	    			    	xml.append("<paxes"+(i+1)+">"+adult+"-0</paxes"+(i+1)+">"); //房间客人类别
						}
						nulberUtils = zc;
					}
					else
					{
						int i = 1;
						for (; i < zc; i++) 
						{
							xml.append("<numhab"+(i+1)+">1</numhab"+(i+1)+">"); //房间类型1
	    			    	xml.append("<paxes"+(i+1)+">"+adult+"-0</paxes"+(i+1)+">"); //房间客人类别
						}
						xml.append("<numhab"+(i+1)+">1</numhab"+(i+1)+">"); //房间类型1
    			    	xml.append("<paxes"+(i+1)+">"+ys+"-0</paxes"+(i+1)+">"); //房间客人类别
    			    	nulberUtils = (zc+1);
					}
				}
			}
			
			if(count > adult)
			{
				ct = adult+"-0";
			}
			else
			{
				ct = count+"-0";
			}
			
	        String xmlInfo110 = HttpUtils.GetRestelXml110(orderRoomStay.getHotelCode(),hotelInfoRecord.getStr("pais"),hotelInfoRecord.getStr("codprovincia"), start, end, "1", ct ,xml.toString());
	        System.out.println("供应商请求报文："+xmlInfo110);
	        String result110 = HttpUtils.HttpClientPost(xmlInfo110);
	        System.out.println("供应商响应报文："+result110);
	        InputStream stream110 = new ByteArrayInputStream(result110.getBytes());
			
			double total = Double.parseDouble(orderNew.getTotalPay());
			double price = Double.parseDouble(orderNew.getOrderRoomStay().getAmountAfterTax());
			System.out.println("携程传给我们的价格："+price+"\t总价格："+total);
			double rat = hotelInfoRecord.getDouble("usdrate");
			double tj = hotelInfoRecord.getDouble("tj");
			//得到供应商的单价
			double temp = AmountUtil.subtract(price,  tj, 2);
			price = AmountUtil.divide(temp, rat, 2);
			//得到供应商的总价
			total = AmountUtil.multiply(price, days*1.0, 2);

			Date date = new Date();  
	        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        String dateStr = format.format(date);
			
			List<Lin> lins = HttpUtils.Parse110XmlWithLins(stream110,orderNew.getOrderRoomStay().getRoomTypeCode(),orderNew.getOrderRoomStay().getRatePlanCode(), total+"",price+"");
			Record record = Db.findFirst("select id from sys_order where ResID501 = '"+orderNew.getResID501()+"'");
			
			if(null != lins && lins.size() > 0)
			{
				StringBuffer buffer = new StringBuffer();
				String titulo = "Sr.";//先生
				String guestName = ((Customer)customers.get(0)).getGivenName() + " " + ((Customer)customers.get(0)).getSurname();
				buffer.append("<paxes>");
				for(Customer paxs :customers)
				{
					buffer.append("<pax>");
					if(null != paxs.getGender())
					{
						if(paxs.getGender().equals("M"))
						{
							titulo = "Sr.";//先生
						}
						else if(paxs.getGender().equals("F"))
						{
							titulo = "Sra.";//先生
						}	
					}
					buffer.append("<titulo>"+titulo+"</titulo>");
					buffer.append("<nombrePax>"+paxs.getGivenName()+"</nombrePax>");
					buffer.append("<apellidos>"+paxs.getSurname()+"</apellidos>");
					if(null == paxs.getAge())
					{
						paxs.setAge("28");
					}
					buffer.append("<edad>"+paxs.getAge()+"</edad>");
					buffer.append("</pax>");
				}
				buffer.append("</paxes>");
				//Restel 202接口，预订
			 	String xmlInfo202 = HttpUtils.GetRestelXml202(orderNew.getOrderRoomStay().getHotelCode(),guestName, "", "25", lins,buffer.toString());
			 	System.out.println("供应商请求报文："+xmlInfo202);  
			 	String result202 = HttpUtils.HttpClientPost(xmlInfo202);
			 	System.out.println("供应商响应报文："+result202);   
			 	InputStream stream202 = new ByteArrayInputStream(result202.getBytes());
				String res202 = HttpUtils.Parse202Xml(stream202);
				//任何预订必须得到确认或取消, 如果在10分钟的最长时间内未确认预订，则会自动取消预订
				//并且在将来预订时可能会丢失相同参数的可用性。
				if(res202.split("#")[0].equals("00"))
				{
					System.out.println("预定成功！");
					setAttr("ResID502",res202.split("#")[1]);
					//开始确认订单
					String xmlInfo3 = HttpUtils.GetRestelXml3(res202.split("#")[1],"AE");
					System.out.println("供应商请求报文："+xmlInfo3);   
					String result3 = HttpUtils.HttpClientPost(xmlInfo3);
					System.out.println("供应商响应报文："+result3);   
					InputStream stream3 = new ByteArrayInputStream(result3.getBytes());
					String res3 = HttpUtils.Parse3Xml(stream3);
				 	if(res3.split("#")[0].equals("00"))
				 	{
				 		if(null != record)
				 		{
				 			Db.update("update sys_order set orderState = 2 where ResID501 = '"+orderNew.getResID501()+"'");
				 		}else
				 		{
				 			Db.update("insert into sys_order(customerName,customerCount,phone,email,StartTime,EndTime,hotelId,"
									+ "restelPay,ctripPay,romeTypeCode,ratePlanCode,payType,numberoOfRoom,ctripUniqueID,orderState,ResID501,ResID502,"
									+ "ResID504,otherPeople,localizador_corto,createtime) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 
										contactPersion.getGivenName()+":"+contactPersion.getSurname(),orderNew.getGuestCount(),
										contactPersion.getPhoneNumber(),contactPersion.getEmail(),orderNew.getOrderRoomStay().getEffectiveDate(),
										orderNew.getOrderRoomStay().getExpireDate(),orderNew.getOrderRoomStay().getHotelCode(),
										total+"",orderNew.getTotalPay(),orderNew.getOrderRoomStay().getRoomTypeCode(),
										orderNew.getOrderRoomStay().getRatePlanCode(),"501",nulberUtils,orderNew.getUniqueID(),"2",
										orderNew.getResID501(),res202.split("#")[1],res3.split("#")[1],otherPeople,res3.split("#")[2],dateStr);//2表示预定成功，确认成功
				 		}
				 		System.out.println("预定成功，确认成功！");
				 		if(StrUtils.isNotEmpty(remark))
				 		{
				 			setAttr("warning", remark);
				 		}
				 		else
				 		{
				 			setAttr("warning", warning);
				 		}
				 		renderXml("/pages/template/OrderNewSuccess.xml");
				 	}
				 	else
				 	{
				 		if(null == record)
				 		{
				 			Db.update("insert into sys_order(customerName,customerCount,phone,email,StartTime,EndTime,hotelId,"
									+ "restelPay,ctripPay,romeTypeCode,ratePlanCode,payType,numberoOfRoom,ctripUniqueID,orderState,ResID501,ResID502,"
									+ "ResID504,otherPeople,localizador_corto,createtime) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",contactPersion.getGivenName()+":"+contactPersion.getSurname(),
									orderNew.getGuestCount(),contactPersion.getPhoneNumber(),contactPersion.getEmail(),
									orderNew.getOrderRoomStay().getEffectiveDate(),orderNew.getOrderRoomStay().getExpireDate(),
									orderNew.getOrderRoomStay().getHotelCode(),total+"",orderNew.getTotalPay(),
									orderNew.getOrderRoomStay().getRoomTypeCode(),orderNew.getOrderRoomStay().getRatePlanCode(),
									"501",nulberUtils,orderNew.getUniqueID(),"1",orderNew.getResID501(),res202.split("#")[1],"",otherPeople,"",dateStr);//1表示预定成功，确认失败
				 		}
				 		else
				 		{
				 			Db.update("update sys_order set orderState = 1 where ResID501 = '"+orderNew.getResID501()+"'");
				 		}
				 		System.out.println("预定期间,lins == null 错误,返回Error！");
				 		renderXml("/pages/template/OrderNewError.xml");
				 	}
				}
				else
				{
					if(null == record)
					{
						Db.update("insert into sys_order(customerName,customerCount,phone,email,StartTime,EndTime,hotelId,"
								+ "restelPay,ctripPay,romeTypeCode,ratePlanCode,payType,numberoOfRoom,ctripUniqueID,orderState,ResID501,ResID502,"
								+ "ResID504,otherPeople,localizador_corto,createtime) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", contactPersion.getGivenName()+":"+contactPersion.getSurname(),orderNew.getGuestCount(),
								contactPersion.getPhoneNumber(),contactPersion.getEmail(),orderNew.getOrderRoomStay().getEffectiveDate(),
								orderNew.getOrderRoomStay().getExpireDate(),orderNew.getOrderRoomStay().getHotelCode(),total+"",orderNew.getTotalPay(),
								orderNew.getOrderRoomStay().getRoomTypeCode(),orderNew.getOrderRoomStay().getRatePlanCode(),
								"501",nulberUtils,orderNew.getUniqueID(),"0",orderNew.getResID501(),"","",otherPeople,"",dateStr); //0表示预订失败
					}
					else
					{
			 			Db.update("update sys_order set orderState = 0 where ResID501 = '"+orderNew.getResID501()+"'");
					}
					System.out.println("Restel 202,预订失败,返回Error！");
					renderXml("/pages/template/OrderNewError.xml");
				}
			}
			else
			{
				if(null == record)
				{
					Db.update("insert into sys_order(customerName,customerCount,phone,email,StartTime,EndTime,hotelId,"
							+ "restelPay,ctripPay,romeTypeCode,ratePlanCode,payType,numberoOfRoom,ctripUniqueID,orderState,ResID501,ResID502,"
							+ "ResID504,otherPeople,localizador_corto,createtime) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", contactPersion.getGivenName()+":"+contactPersion.getSurname(),orderNew.getGuestCount(),
							contactPersion.getPhoneNumber(),contactPersion.getEmail(),orderNew.getOrderRoomStay().getEffectiveDate(),
							orderNew.getOrderRoomStay().getExpireDate(),orderNew.getOrderRoomStay().getHotelCode(),total+"",orderNew.getTotalPay(),
							orderNew.getOrderRoomStay().getRoomTypeCode(),orderNew.getOrderRoomStay().getRatePlanCode(),
							"501",nulberUtils,orderNew.getUniqueID(),"-1",orderNew.getResID501(),"","",otherPeople,"",dateStr); //-1表示程序异常
				}
				else
				{
		 			Db.update("update sys_order set orderState = -1 where ResID501 = '"+orderNew.getResID501()+"'");
				}
				System.out.println("预定期间,lins == null 错误,返回Error！");
				renderXml("/pages/template/OrderNewError.xml");
			}
		}
		catch(Exception e)
		{
			System.out.println("预定期间,数据流转换异常,返回Error！"+e.getMessage());
			renderXml("/pages/template/OrderNewError.xml");
		} 
		System.out.println("*****************************下单结束*********************************");
	}
	
	/**
	 * ordercancel service
	 */
	@SuppressWarnings("unchecked")
	public void ordercancel() {
		try {
			System.out.println("*****************************取消订单开始*********************************");
			SAXReader reader = new SAXReader();
			setAttr("TimeStamp",DateUtils.getNowByGMT8());
			Document document = reader.read(getRequest().getInputStream());
			Element root = document.getRootElement();
			Element Body = root.element("Body");
	        Element OTA_CancelRQ = Body.element("OTA_CancelRQ");
	        Element RequestorID = OTA_CancelRQ.element("POS").element("Source").element("RequestorID");
	        String ID = RequestorID.attribute("ID").getText();
	        String MessagePassword = RequestorID.attribute("MessagePassword").getText();
	        if(!(ID.equals("hangye")) || !(MessagePassword.equals("hangye_alex")))
	        {
	        	System.out.println("订单取消,认证失败,返回Error！");
	        	renderXml("/pages/template/OrderCancelError.xml");
	        	return;
	        }
	        List<Element> UniqueIDs = OTA_CancelRQ.elements("UniqueID");
	        String ResID501="",ResID502="",HotelCode="";
	        for (Element element : UniqueIDs) {
	        	String type = element.attribute("Type").getText();
				if(type.equals("501"))
				{
					ResID501 = element.attribute("ID").getText();
				}
				else if(type.equals("502"))
				{
					ResID502 = element.attribute("ID").getText();
				}
				else if(type.equals("10"))
				{
					HotelCode = element.attribute("ID").getText();
				}
			}

	        Record orderIsV = Db.findFirst("select id,ResID501,ResID502,localizador_corto from sys_order where ctripUniqueID = '"+ResID501+"' and hotelId='"+HotelCode+"' and orderState = 2");
	        if(null == orderIsV)
	        {
	        	String error = "<Error ShortText=\"Reservation"+ResID501+"cannot be found\" Type=\"3\" Code=\"245\"/>";
	        	setAttr("error", error);
	        	setAttr("ResID501", ResID501);
	        	System.out.println("订单未发现！");
	        	renderXml("/pages/template/OrderCancelError.xml");
	        }
	        
	        //restel 401接口，取消订单
			String xmlInfo401 = HttpUtils.GetRestelXml401(orderIsV.getStr("ResID502"),orderIsV.getStr("localizador_corto"));
			System.out.println("供应商请求报文："+xmlInfo401);
			String result401 = HttpUtils.HttpClientPost(xmlInfo401);
			System.out.println("供应商响应报文："+result401);   
			InputStream stream401 = new ByteArrayInputStream(result401.getBytes());
			String res401 = HttpUtils.Parse401Xml(stream401);
		 	if(res401.split("#")[0].equals("00"))
		 	{
		 		//取消成功
		 		Db.update("update sys_order set orderState = 3 where id ="+orderIsV.getInt("id")); //orderstate = 3 取消订单
		 		String warning = "<Warning Type=\"3\">May need to double confirm with hotel</Warning>";
	        	setAttr("warning", warning);
	        	setAttr("ResID501", ResID501);
	        	setAttr("ResID502", ResID502);
	        	renderXml("/pages/template/OrderCancelSuccess.xml");
		 	}
		 	else
		 	{
		 		String error = "<Error ShortText=\"Reservation"+ResID501+"cannot be cancel from restel\" Type=\"3\" Code=\"245\"/>";
	        	setAttr("error", error);
	        	setAttr("ResID501", ResID501);
	        	System.out.println("供应商取消失败！");
	        	renderXml("/pages/template/OrderCancelError.xml");
		 	}
		} catch (Exception e) {
			System.out.println("订单取消,出现异常,返回Error！");
        	renderXml("/pages/template/OrderCancelError.xml");
		}
		System.out.println("*****************************取消订单结束*********************************");
	}
	
	/**
	 * read service
	 */
	@SuppressWarnings("unchecked")
	public void read() {
		try {
			SAXReader reader = new SAXReader();
			setAttr("TimeStamp",DateUtils.getNowByGMT8());
			Document document = reader.read(getRequest().getInputStream());
			Element root = document.getRootElement();
			Element Body = root.element("Body");
	        Element OTA_ReadRQ = Body.element("OTA_ReadRQ");
	        Element RequestorID = OTA_ReadRQ.element("POS").element("Source").element("RequestorID");
	        String ID = RequestorID.attribute("ID").getText();
	        String MessagePassword = RequestorID.attribute("MessagePassword").getText();
	        if(!(ID.equals("hangye")) || !(MessagePassword.equals("hangye_101019")))
	        {
	        	System.out.println("case 10 认证失败,返回Error！");
	        	renderXml("/pages/template/OrderReadError.xml");
	        }
	        
	        List<Element> UniqueIDs = OTA_ReadRQ.element("ReadRequests").element("ReadRequest").elements("UniqueID");
	        String ResID501="",ResID502="";
	        for (Element element : UniqueIDs) {
	        	String type = element.attribute("Type").getText();
				if(type.equals("501"))
				{
					ResID501 = element.attribute("ID").getText();
				}
				else if(type.equals("502"))
				{
					ResID502 = element.attribute("ID").getText();
				}
			}
	        
	        Record orderIsV = Db.findFirst("select id,ResID501,ResID502,orderState from sys_order where ctripUniqueID = '"+ResID501+"' and ResID502 = '"+ResID502+"'");
	        String stu = "C";
	        if(null != orderIsV)
	        {
	        	int status = orderIsV.getInt("orderState");
	        	if(status == 2)
	        	{
	        		stu = "S";
	        	}
	        	else if(status == 3)
	        	{
	        		stu = "C";
	        	}
	        	setAttr("ststus", stu);
		        setAttr("ResID501", ResID501);
		        setAttr("ResID502", ResID502);
				renderXml("/pages/template/OrderReadSuccess.xml");
				return;
	        }
	        else
	        {
	        	setAttr("ststus", stu);
	        	setAttr("ResID501", ResID501);
			    setAttr("ResID502", ResID502);
	        	renderXml("/pages/template/OrderReadError.xml");
	        	return;
	        }
		} catch (Exception e) {
			System.out.println("读取订单异常,返回Error！"+e.getMessage());
			setAttr("ststus", "C");
        	renderXml("/pages/template/OrderReadError.xml");
        	return;
		}
	}

	/**
	 * 测试
	 */
	@SuppressWarnings("unchecked")
	public void test()
	{
		try
		{
		 SAXReader reader = new SAXReader();// 读取输入流
		 Document document = reader.read(getRequest().getInputStream());
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
        		 st = 0;
        	 }
        	 if(st>0)
        	 {
        		 Db.update("update sys_hotels set ctripHotelCode = '"+ctripHotelCode+"',status ="+st+"  where hot_codcobol = '"+hotelcode+"'");
            	 Element BasicRooms = Hotel.element("BasicRooms");
            	 if(null != BasicRooms)
            	 {
            		 List<Element> BasicRoom = BasicRooms.elements("BasicRoom");
            		 if(null != BasicRoom && BasicRoom.size() > 0)
            		 {
            			 for (Element element : BasicRoom) {
    						String rtcode = element.attributeValue("RoomCode");
    						String Status1 = element.attributeValue("Status");
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
    			        		st1 = 0;
    			        	}
    						if(st1 > 0)
    						{
    							 Db.update("update sys_hotels_roomtype set rtstatus = "+st1+" where hotelcode = '"+hotelcode+"' and rtcode = '"+rtcode+"'");
    							Element RatePlans = Hotel.element("RatePlans");
        			        	if(null != RatePlans)
        			        	{
        			        		 List<Element> RatePlan = RatePlans.elements("RatePlan");
        			        		 if(null != RatePlan && RatePlan.size() > 0)
        			        		 {
        			        			 String rpcode = element.attributeValue("RatePlanCode");
        								 Status = element.attributeValue("Status");
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
        					        		 st2 = 0;
        					        	 }
        								 if(st2 > 0)
        								 {
        									 Db.update("update sys_hotels_roomtype set rpstatus ="+st+" where hotelcode = '"+hotelcode+"' and rtcode = '"+rtcode+"' and rpcode = '"+rpcode+"'");
        								 }
        			        		 } 
        			        	}
    						}
    					}
            		 }
            	 }
        	 } 
         }
		}catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		renderXml("/pages/template/OrderReadError.xml");
	}
}
