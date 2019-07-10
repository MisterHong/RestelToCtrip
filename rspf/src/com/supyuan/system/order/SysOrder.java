package com.supyuan.system.order;

import com.supyuan.component.base.BaseProjectModel;
import com.supyuan.jfinal.component.annotation.ModelBind;

@ModelBind(table = "sys_order")
public class SysOrder  extends BaseProjectModel<SysOrder> {
	private static final long serialVersionUID = 1L;
	public static final SysOrder dao = new SysOrder();
}
