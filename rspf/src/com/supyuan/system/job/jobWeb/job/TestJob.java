package com.supyuan.system.job.jobWeb.job;

import com.supyuan.component.base.BaseJob;

public class TestJob extends BaseJob {

    public String add(String name, String emali) {
        System.out.println("@@@@@@@@add: name->" + name + " emali->" + emali);
        return "";
    }

    public String del(String params1, String content, String data1) {
        System.out.println("@@@@@@@@del: param1->" + params1 + " content->" + content + " data->" + data1);
        return "";
    }

    public String upd(String adds, Integer tel) {
        System.out.println("@@@@@@@@upd: adds->" + adds + " tel->" + tel);
        return "";
    }

    public String sel(String uids, int num) {
        System.out.println("@@@@@@@@sel: uids->" + uids + " num->" + num);
        return "";
    }

    public String hello() {
        System.out.println("@@@@@@@@hello: hello");
        return "";
    }


}
