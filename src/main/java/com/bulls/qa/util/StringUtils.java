package com.bulls.qa.util;


import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class StringUtils {

    private static String[] telFirst = "134,135,136,137,138,139,150,151,152,157,158,159,130,131,132,155,156,133,153,180,181"
            .split(",");

    public static String generateRandomString(int length) {
        String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int num = random.nextInt(36);
            buf.append(str.charAt(num));
        }
        return buf.toString();
    }

    public static String generateNumString(int length) {
        String str = "0123456789";
        Random random = new Random();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int num = random.nextInt(10);
            buf.append(str.charAt(num));
        }
        return buf.toString();
    }

    //符合规则的订单号
    public static String generateRandomOrder() {
        String str = "0123456789";
        Random random = new Random();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 8; i++) {
            int num = random.nextInt(10);
            buf.append(str.charAt(num));
        }
        String time = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        return "2" + time + random.nextInt(10) + "D" + buf.toString();
    }

    //符合规则的手机号
    public static String generateRandomPhone() {

        String str = "0123456789";
        Random random = new Random();
        String head = telFirst[random.nextInt(telFirst.length)];
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 8; i++) {
            int num = random.nextInt(10);
            buf.append(str.charAt(num));
        }
        return head + buf.toString();
    }

    public static List<String> generateRandomPhone(int count) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date date = calendar.getTime();
        //long startTime = date.getTime();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = count % telFirst.length;
            String gap = String.valueOf(System.currentTimeMillis());
            gap=gap.substring(gap.length()-8);
            String phone = telFirst[index] + gap;
            list.add(phone);

            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    public static String getRandomPhone() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date date = calendar.getTime();

        Random random = new Random();
        int index = random.nextInt(100) % telFirst.length;
        String gap = String.valueOf(System.currentTimeMillis());
        gap=gap.substring(gap.length()-8);
        String phone = telFirst[index] + gap;


        return phone;
    }

    public static String replaceStr(String str, Map<String, String> strMap) {
        for (Map.Entry entry : strMap.entrySet()) {
            str = str.replaceAll(entry.getKey().toString(), entry.getValue().toString());
        }
        return str;
    }
    public static void main(String[] args){
        System.out.println((long)(0.31415926*10));
    }
}
