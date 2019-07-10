package com.supyuan.system.hotel;

import com.supyuan.component.base.BaseProjectModel;
import com.supyuan.jfinal.component.annotation.ModelBind;

@ModelBind(table = "sys_hotels_info", key = "codigo_hotel")
public class SysHotelsInfo extends BaseProjectModel<SysHotelsInfo> {
	private static final long serialVersionUID = 1L;
	public static final SysHotelsInfo dao = new SysHotelsInfo();
}
