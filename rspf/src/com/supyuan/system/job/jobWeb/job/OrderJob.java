package com.supyuan.system.job.jobWeb.job;

import com.supyuan.component.base.BaseJob;

public class OrderJob  extends BaseJob {

	/***
	 * 携程订单数据获取
	 * @param xmlInfo
	 * @return
	 */
	public String ctripOrdersDeal() {
		 Thread thread = new Thread(new Runnable(){
			 public void run(){
				System.out.println("*************************ctripOrdersDeal开始处理携程订单数据*****************");
				 try {
					 	//获取携程订单列表
					 	
					} catch (Exception e) {
						System.out.println(e.getMessage());
					}
				System.out.println("*************************ctripOrdersDeal处理携程订单数据结束*****************");
			 }
		 });  
		 thread.start();
	    return "";
	}
}
