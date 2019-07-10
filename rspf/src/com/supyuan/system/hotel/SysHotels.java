package com.supyuan.system.hotel;

import com.supyuan.component.base.BaseProjectModel;
import com.supyuan.jfinal.component.annotation.ModelBind;

@ModelBind(table = "sys_hotels")
public class SysHotels extends BaseProjectModel<SysHotels> {
	private static final long serialVersionUID = 1L;
	public static final SysHotels dao = new SysHotels();
}
