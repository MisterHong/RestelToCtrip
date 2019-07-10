package com.supyuan.system.order;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.supyuan.component.base.BaseProjectController;
import com.supyuan.jfinal.component.annotation.ControllerBind;
import com.supyuan.jfinal.component.db.SQLUtils;
import com.supyuan.system.hotel.SysHotels;
import com.supyuan.system.menu.SysMenu;
import com.supyuan.util.Config;
import com.supyuan.util.HttpUtils;
import com.supyuan.util.HttpsUtils;
import com.supyuan.util.StrUtils;

/**
 * 菜单
 * 
 * @author yaosir 2019-2-24
 */
@ControllerBind(controllerKey = "/system/ods")
public class OrderController extends BaseProjectController {
	private static final String path = "/pages/system/order/order_";
	OrderSvc orderSvc = new OrderSvc();
	
	public void index() {
		list();
	}
	
	/**
	 * List of Order service
	 */
	public void list() {
		SysHotels model = getModelByAttr(SysHotels.class);
		String search = getPara();
		SQLUtils sql = new SQLUtils("from sys_order od INNER JOIN sys_hotels_info shi on od.hotelId = shi.codigo_hotel");
		if (StrUtils.isNotEmpty(search)) {
			sql.whereLike("search", model.getStr("search"));
			setAttr("search", search);
		}
		
		Page<SysMenu> page = SysMenu.dao.paginate(getPaginator(), "select od.*,shi.nombre_h,shi.codigo_hotel",
				sql.toString().toString());
		
		setAttr("page", page);
		render(path + "list.html");
	}
	
	/**
	 * push 确认号
	 */
	public void push()
	{
		String confirm = getPara("confirm");
		String id = getPara("id");
		String ctrip_api_url = Config.getStr("ctrip_api_url");
		Record orderRecord = Db.findById("sys_order", "id", id);
		if(null != orderRecord)
		{
			//推送酒店信息
	 		String confirminfo = HttpUtils.PushCtripXmlConfirm(orderRecord.getStr("ResID501"),orderRecord.getStr("ResID502"),confirm);
	 		System.out.println("推送酒店确认号报文："+confirminfo);
	 		String result = HttpsUtils.doPost(ctrip_api_url, confirminfo);
			System.out.println("推送酒店信息获取到的携程数据结果："+result);
			if(result.contains("Success"))
			{
				System.out.println("推送酒店确认号成功！");
	        	Db.update("update sys_order set ResID504 = '"+confirm+"' where id = "+id);
			}
		}
	}
}
