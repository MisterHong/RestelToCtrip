/**
 * Copyright 2015-2025 FLY的狐狸(email:jflyfox@sina.com qq:369191470).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.supyuan.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * 日期处理
 * 
 * 
 * 2014年5月5日 下午12:00:00
 * flyfox 330627517@qq.com
 */
public class DateUtils {
	
	public static final int SECOND = 1;
	public static final int MINUTE_SECOND = 60 * SECOND;
	public static final int HOUR_SECOND = 60 * MINUTE_SECOND;
	public static final int DAY_SECOND = 24 * HOUR_SECOND;
	public static final int WEEK_SECOND = 7 * DAY_SECOND;

	/** 日期格式：yyyy-MM-dd HH:mm:ss.SSS */
	public static final String YMD_HMSSS = "yyyy-MM-dd HH:mm:ss.SSS";
	/** 日期格式：yyyyMMddHHmmssSSS */
	public static final String YMDHMSSS = "yyyyMMddHHmmssSSS";
	/** 日期格式：yyyy-MM-dd HH:mm:ss */
	public static final String YMD_HMS = "yyyy-MM-dd HH:mm:ss";
	/** 日期格式：yyyy-MM-dd HH:mm */
	public static final String YMD_HM = "yyyy-MM-dd HH:mm";
	/** 日期格式：yyyyMMddHHmmss */
	public static final String YMDHMS = "yyyyMMddHHmmss";
	/** 日期格式：yyyy-MM-dd */
	public static final String YMD = "yyyy-MM-dd";
	/** 日期格式：MM/dd/yyyy */
	public static final String MDY = "MM/dd/yyyy";
	/** 时间格式：HH:mm:ss */
	public static final String HMS = "HH:mm:ss";
	
	/** 时间格式：yyyy-MM-dd'T'HH:mm:ss'Z' 2019-02-13T14:21:27Z */
	public static final String GMT8 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	
	/**
	 * 默认的日期格式 .
	 */
	public static final String DEFAULT_REGEX = "yyyy-MM-dd";
	/**
	 * 默认的日期格式 .
	 */
	public static final String DEFAULT_REGEX_YYYYMMDD = "yyyyMMdd";
	/**
	 * 默认的日期格式 .
	 */
	public static final String DEFAULT_REGEX_YYYY_MM_DD_HH_MIN_SS = "yyyy-MM-dd HH:mm:ss";
	/**
	 * 默认的DateFormat 实例
	 */
	private static final EPNDateFormat DEFAULT_FORMAT = new EPNDateFormat(DEFAULT_REGEX);
	/**
	 * 默认的DateFormat 实例
	 */
	private static final EPNDateFormat DEFAULT_FORMAT_YYYY_MM_DD_HH_MIN_SS = new EPNDateFormat(
			DEFAULT_REGEX_YYYY_MM_DD_HH_MIN_SS);
	/**
	 * 默认的DateFormat 实例
	 */
	private static final EPNDateFormat DEFAULT_FORMAT_YYYYMMDD = new EPNDateFormat(DEFAULT_REGEX_YYYYMMDD);
	private static Map<String, EPNDateFormat> formatMap = new HashMap<String, EPNDateFormat>();
	static {
		formatMap.put(DEFAULT_REGEX, DEFAULT_FORMAT);
		formatMap.put(DEFAULT_REGEX_YYYY_MM_DD_HH_MIN_SS, DEFAULT_FORMAT_YYYY_MM_DD_HH_MIN_SS);
		formatMap.put(DEFAULT_REGEX_YYYYMMDD, DEFAULT_FORMAT_YYYYMMDD);
	}

	private DateUtils() {

	}

	/**
	 * 时间对象格式化成String ,等同于java.text.DateFormat.format();
	 * 
	 * @param date
	 *            需要格式化的时间对象
	 * 
	 * 2014年5月5日 下午12:00:00
	 * flyfox 330627517@qq.com
	 * @return 转化结果
	 */
	public static String format(java.util.Date date) {
		return DEFAULT_FORMAT.format(date);
	}

	/**
	 * 时间对象格式化成String ,等同于java.text.SimpleDateFormat.format();
	 * 
	 * @param date
	 *            需要格式化的时间对象
	 * @param regex
	 *            定义格式的字符串
	 * 
	 * 2014年5月5日 下午12:00:00
	 * flyfox 330627517@qq.com    
	 * @return 转化结果
	 */
	public static String format(java.util.Date date, String regex) {
		return getDateFormat(regex).format(date);
	}

	private static EPNDateFormat getDateFormat(String regex) {
		EPNDateFormat fmt = formatMap.get(regex);
		if (fmt == null) {
			fmt = new EPNDateFormat(regex);
			formatMap.put(regex, fmt);
		}
		return fmt;
	}

	/**
	 * 尝试解析时间字符串 ,if failed return null;
	 * 
	 * @author wangp
	 * @since 2008.12.20
	 * @param time
	 * 
	 * 2014年5月5日 下午12:00:00
	 * flyfox 330627517@qq.com
	 * @return
	 */
	public static Date parseByAll(String time) {
		Date stamp = null;
		if (time == null || "".equals(time))
			return null;
		Pattern p3 = Pattern.compile("\\b\\d{2}[.-]\\d{1,2}([.-]\\d{1,2}){0,1}\\b");
		if (p3.matcher(time).matches()) {
			time = (time.charAt(0) == '1' || time.charAt(0) == '0' ? "20" : "19") + time;
		}

		stamp = DateUtils.parse(time, "yyyy-MM-ddHH:mm:ss");
		if (stamp == null)
			stamp = DateUtils.parse(time, "yyyy-MM-dd");
		if (stamp == null)
			stamp = DateUtils.parse(time, "yyyy.MM.dd");
		if (stamp == null)
			stamp = DateUtils.parse(time, "yyyy-MM");
		if (stamp == null)
			stamp = DateUtils.parse(time, "yyyy.MM");
		if (stamp == null)
			stamp = DateUtils.parse(time, "yyyy-MM-dd");
		if (stamp == null)
			stamp = DateUtils.parse(time, "yy-MM-dd");
		if (stamp == null)
			stamp = DateUtils.parse(time, "yyyy.MM.dd");
		if (stamp == null)
			stamp = DateUtils.parse(time, "yyyy-MM.dd");
		if (stamp == null)
			stamp = DateUtils.parse(time, "yyyy.MM-dd");
		if (stamp == null)
			stamp = DateUtils.parse(time, "yyyyMMdd");
		if (stamp == null)
			stamp = DateUtils.parse(time, "yyyy年MM月dd日");
		if (stamp == null)
			stamp = DateUtils.parse(time, "yyyyMM");
		if (stamp == null)
			stamp = DateUtils.parse(time, "yyyy年MM月");
		if (stamp == null)
			stamp = DateUtils.parse(time, "yyyy");
		if (stamp == null)
			stamp = DateUtils.parse(time, "yyyy年");
		return stamp;
	}

	/**
	 * 解析字符串成时间 ,遇到错误返回null不抛异常
	 * 
	 * @param source
	 * 
	 * 2014年5月5日 下午12:00:00
	 * flyfox 330627517@qq.com
	 * @return 解析结果
	 */
	public static java.util.Date parse(String source) {
		try {
			return DEFAULT_FORMAT.parse(source);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 解析字符串成时间 ,遇到错误返回null不抛异常
	 * 
	 * @param source
	 * @param 格式表达式
	 * 
	 * 2014年5月5日 下午12:00:00
	 * flyfox 330627517@qq.com
	 * @return 解析结果
	 */
	public static java.util.Date parse(String source, String regex) {
		try {
			EPNDateFormat fmt = getDateFormat(regex);
			return fmt.parse(source);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 取得当前时间的Date对象 ;
	 * 
	 * 2014年5月5日 下午12:00:00
	 * flyfox 330627517@qq.com
	 * @return
	 */
	public static Date getNowDate() {
		return new Date(System.currentTimeMillis());
	}
	
	/**
	 * 获取当前时间字符串
	 * 
	 * 2014年5月5日 下午12:00:00
	 * flyfox 330627517@qq.com
	 * @return
	 */
	public static String getNow() {
		return getNow(DEFAULT_REGEX);
	}
	
	/**
	 * 获取当前时间字符串
	 * 
	 * 2014年7月4日 下午11:47:10
	 * flyfox 330627517@qq.com
	 * @param regex 格式表达式
	 * @return
	 */
	public static String getNow(String regex) {
		return format(getNowDate(), regex);
	}
	
	/**
	 * 获取前天-1，明天1，后天2时间字符串
	 * 
	 * 2014年7月4日 下午11:47:10
	 * flyfox 330627517@qq.com
	 * @param regex 格式表达式
	 * @return
	 */
	@SuppressWarnings("static-access")
	public static String getAddDayNow(String regex,int day) {
		Calendar ca = Calendar.getInstance();
		ca.setTime(getNowDate());
		ca.add(ca.DATE,day);//把日期往后增加一天.整数往后推,负数往前移动
		return format(ca.getTime(), regex);
	}
	
	
	/***
	 * 获取指定时间所在天的开始时间
	 * 
	 * @param date
	 * @return
	 */
	public static String getCurrenDayBeginTime(Date date) {
		Calendar ca = Calendar.getInstance();
		ca.setTime(date);
		ca.set(Calendar.HOUR_OF_DAY, 0);
		ca.set(Calendar.MINUTE, 0);
		ca.set(Calendar.SECOND, 0);
		return format(ca.getTime(), "yyyy-MM-dd HH:mm:ss");
	}

	/***
	 * 获取指定时间所在天的结束时间
	 * 
	 * @param date
	 * @return
	 */
	public static String getCurrenDayEndTime(Date date) {
		Calendar ca = Calendar.getInstance();
		ca.setTime(date);
		ca.set(Calendar.HOUR_OF_DAY, 23);
		ca.set(Calendar.MINUTE, 59);
		ca.set(Calendar.SECOND, 59);
		return format(ca.getTime(), "yyyy-MM-dd HH:mm:ss");
	}

	/***
	 * 时间戳转换成标准时间字符串
	 *
	 * @param time
	 * @param format
	 * @return
	 */
	public static String timestampToDate(Long time, String format) {
		if (time == null || time == 0l)
			return "";
		else {
			//判断时间戳time是否是10位，10位需要补充000，13位则不需要处理
			String str = time + "";
			if(str.length() == 10)
				str += "000";

			time = Long.parseLong(str);
			Date utilDate = new java.util.Date(time);

			SimpleDateFormat df = new SimpleDateFormat(format);
			return df.format(utilDate);
		}
	}

	/**
	 * 获取系统当前时间戳 取到秒级
	 * @return
	 */
	public static long getCurrentTimestamp() {

		long time = new Date().getTime();
		//mysq时间戳只有10位
		String timeStamp = (time + "").substring(0, 10);
		return Long.parseLong(timeStamp);
	}

	/**
	 * 将时间字符串转成时间戳
	 * @param dateTime
	 * @return
	 */
	public static long dateTime2timeStamp(String dateTime){

		return dateTime2timeStamp(dateTime,YMD_HMS);
	}

	public static long dateTime2timeStamp(String dateTime,String format){

		long time = 0l;
		if(StrUtils.isEmpty(dateTime)) {
			return time;
		}
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
			Date date = simpleDateFormat.parse(dateTime);
			time = date.getTime();
			String timeStamp = (time + "").substring(0, 10);
			return Long.parseLong(timeStamp);
		} catch (ParseException e) {
			e.printStackTrace();
			return time;
		}
	}
	
	public static Date getGMT8Time(){
        Date gmt8 = null;
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"),Locale.CHINESE); Calendar day = Calendar.getInstance();
            day.set(Calendar.YEAR, cal.get(Calendar.YEAR));
            day.set(Calendar.MONTH, cal.get(Calendar.MONTH));
            day.set(Calendar.DATE, cal.get(Calendar.DATE));
            day.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
            day.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
            day.set(Calendar.SECOND, cal.get(Calendar.SECOND));
            gmt8 = day.getTime();
        } catch (Exception e) {
            System.out.println("获取GMT8时间 getGMT8Time() error !");
            e.printStackTrace();
            gmt8 = null;
        }
        return  gmt8;
    }
	
	/**
	 * 获取当前时间字符串
	 * 
	 * 2019-02-13T14:21:27Z
	 * @param regex 格式表达式
	 * @return
	 */
	public static String getNowByGMT8() {
		return format(getGMT8Time(), GMT8);
	}
	
	/**
	 * 获取两个时间字符串相差的天数
	 * @param a
	 * @param b
	 * @return
	 */
	public static Long between_days(String a, String b) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");// 自定义时间格式

        Calendar calendar_a = Calendar.getInstance();// 获取日历对象
        Calendar calendar_b = Calendar.getInstance();

        Date date_a = null;
        Date date_b = null;

        try {
            date_a = simpleDateFormat.parse(a);//字符串转Date
            date_b = simpleDateFormat.parse(b);
            calendar_a.setTime(date_a);// 设置日历
            calendar_b.setTime(date_b);
        } catch (ParseException e) {
            e.printStackTrace();//格式化异常
        }

        long time_a = calendar_a.getTimeInMillis();
        long time_b = calendar_b.getTimeInMillis();

        long between_days = (time_b - time_a) / (1000 * 3600 * 24);//计算相差天数

        return between_days;
    }
	
	/***
	 * 
	 * @return
	 */
	public static Date StrToDate(String str)
	{
		 SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");// 自定义时间格式
		 Date date_a = null;
		 try {
			date_a = simpleDateFormat.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		 return date_a;
	}
	
	public static void main(String[] args) {
		//Date date = getGMT8Time(0);
		Date dt = new Date();
		//格式化时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		String time = sdf.format(dt);
		System.out.println(time);
		System.out.println(dt.toGMTString());
		
		String start = DateUtils.getNow(DateUtils.MDY);
		System.out.println(start);
		
		String end = DateUtils.getAddDayNow(DateUtils.MDY,1);
		System.out.println(end);
		
		long a = DateUtils.between_days("2019-05-05","2019-05-08");
		System.out.println(a);
		
		Date dts = StrToDate("20190403");
		System.out.println(DateFormat.getDateInstance(DateFormat.MEDIUM).format(dts));
	}
	
	
}

class EPNDateFormat {
	private SimpleDateFormat instance;

	EPNDateFormat() {
		instance = new SimpleDateFormat(DateUtils.DEFAULT_REGEX);
		instance.setLenient(false);
	}

	EPNDateFormat(String regex) {
		instance = new SimpleDateFormat(regex);
		instance.setLenient(false);
	}

	synchronized String format(java.util.Date date) {
		if (date == null)
			return "";
		return instance.format(date);
	}

	synchronized java.util.Date parse(String source) throws ParseException {
		return instance.parse(source);
	}
}
