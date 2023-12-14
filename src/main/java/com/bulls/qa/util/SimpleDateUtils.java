package com.bulls.qa.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class SimpleDateUtils {

    public static Date getNow() {
        Calendar cal = Calendar.getInstance();
        Date currDate = cal.getTime();
        return currDate;
    }

    public static String dateStr(Date date, String f) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat(f);
        String str = format.format(date);
        return str;
    }


    public static Date rollDay(Date d, int day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.DAY_OF_MONTH, day);
        return cal.getTime();
    }

    public static Date getDayBegin(Date date,int day) {
        if (date == null){
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static Date getDayEnd(Date date,int day) {
        if (date == null){
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }


    public static String getThisDay(int day, String f) {
        return dateStr(rollDay(new Date(), day), f);
    }
    public static String getThisDayBegin(int day, String f) {
        return dateStr(getDayBegin(new Date(), day), f);
    }
    public static String getThisDayEnd(int day, String f) {
        return dateStr(getDayEnd(new Date(), day), f);
    }


    public static void main(String[] args) throws Exception {
        System.out.println(getThisDay(0,"yyyyMMddHHmmssSSS"));
        System.out.println(getThisDayBegin(0,"yyyy-MM-dd HH:mm:ss"));
        System.out.println(getThisDayEnd(0,"yyyy-MM-dd HH:mm:ss"));
        System.out.println(StringUtils.generateRandomOrder());
        System.out.println(StringUtils.generateRandomPhone());
        Date date=new Date();
        System.out.println(date);
        System.out.println(dateStr(date,"yyyy-MM-dd HH:mm:ss"));

    }

}
