package com.bilibli.local.city.stat.privacy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        String sa = "{\"privacy_on_lc_on\":{\"1\":134,\"2\":70,\"3\":119,\"4\":64,\"7\":73,\"8\":60,\"9\":42,\"10\":37,\"12\":23},\"privacy_off_lc_off\":{\"0\":150,\"1\":28741,\"2\":15214,\"3\":20143,\"4\":10330,\"7\":9961,\"8\":7805,\"9\":9176,\"10\":8844,\"12\":4203,\"13\":7},\"privacy_on_lc_off\":{\"0\":1,\"1\":303,\"2\":178,\"3\":283,\"4\":143,\"7\":145,\"8\":96,\"9\":98,\"10\":100,\"12\":47},\"privacy_off_lc_on\":{\"1\":5079,\"2\":2800,\"3\":3976,\"4\":2038,\"7\":2312,\"8\":1703,\"9\":1663,\"10\":1485,\"12\":332,\"13\":8}}";
        JSONObject jsonObject = JSON.parseObject(sa);
        Map<Integer, String> city = new HashMap<>();
        city.put(1, "上海");
        city.put(2, "杭州");
        city.put(3, "广州");
        city.put(4, "成都");
        city.put(5, "北京");
        city.put(6, "太原");
        city.put(7, "深圳");
        city.put(8, "重庆");
        city.put(9, "武汉");
        city.put(10, "南京");
        city.put(11, "天津");
        city.put(12, "西安");
        city.put(13, "长沙");
        int privacyOn = 7007;
        int privacyOff = 444602;
        JSONObject privacy_on_lc_on = jsonObject.getJSONObject("privacy_on_lc_on");
        JSONObject privacy_on_lc_off = jsonObject.getJSONObject("privacy_on_lc_off");
        JSONObject privacy_off_lc_on = jsonObject.getJSONObject("privacy_off_lc_on");
        JSONObject privacy_off_lc_off = jsonObject.getJSONObject("privacy_off_lc_off");
        int lcOn = 0;
        int lcOff = 0;
//        for (String s : privacy_on_lc_on.keySet()) {
//            System.out.println(String.format("开同城开隐私-%s:%d", city.get(Integer.valueOf(s)), privacy_on_lc_on.getInteger(s)));
//            lcOn += privacy_on_lc_on.getInteger(s);
//        }
//        for (String s : privacy_on_lc_off.keySet()) {
//            System.out.println(String.format("关同城开隐私-%s:%d", city.get(Integer.valueOf(s)), privacy_on_lc_off.getInteger(s)));
//            lcOn += privacy_on_lc_off.getInteger(s);
//        }
//        for (String s : privacy_off_lc_on.keySet()) {
//            System.out.println(String.format("开同城关隐私-%s:%d", city.get(Integer.valueOf(s)), privacy_off_lc_on.getInteger(s)));
//            lcOff += privacy_off_lc_on.getInteger(s);
//        }
//        for (String s : privacy_off_lc_off.keySet()) {
//            System.out.println(String.format("关同城关隐私-%s:%d", city.get(Integer.valueOf(s)), privacy_off_lc_off.getInteger(s)));
//            lcOff += privacy_off_lc_off.getInteger(s);
//        }
        for (Integer i : city.keySet()) {
            System.out.println(String.format("%s,%d,%d,%d,%d", city.get(i),
                    privacy_on_lc_on.containsKey(String.valueOf(i)) ? privacy_on_lc_on.getInteger(String.valueOf(i)) : 0,
                    privacy_on_lc_off.containsKey(String.valueOf(i)) ? privacy_on_lc_off.getInteger(String.valueOf(i)) : 0,
                    privacy_off_lc_on.containsKey(String.valueOf(i)) ? privacy_off_lc_on.getInteger(String.valueOf(i)) : 0,
                    privacy_off_lc_off.containsKey(String.valueOf(i)) ? privacy_off_lc_off.getInteger(String.valueOf(i)) : 0));
        }
//        System.out.println("不在同城开隐私：" + (privacyOn - lcOn));
//        System.out.println("不在同城关隐私：" + (privacyOff - lcOff));
//        不在同城开隐私：4991
//        不在同城关隐私：308632
    }
}