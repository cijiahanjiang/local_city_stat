package com.bilibli.local.city.stat.picture;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bilibli.local.city.stat.commom.ImageUtil;
import eud.bupt.liujun.HttpUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Test {

    private static final String OcrUrl = "http://grpc-proxy.bilibili.co/main.account-law.filter-image-service/filter_image.service.v1.FilterImage/FilterImage";

    private static Map<String, Object> resultMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        String sa = "http://i0.hdslb.com/bfs/aionecitycover/dfe73363e3f2ecc8779996915e2500513d222daa.jpg,FALSE,,,1,,,";
        String[] tmp = sa.split(",",-1);
        System.out.println(JSON.toJSONString(tmp));
    }


    private static String getPictureWords(String url) {
        JSONObject param = new JSONObject();
        param.put("url", url);
        try {
            String json = HttpUtil.PostWithJsonString(OcrUrl, param.toJSONString());
            if (json != null) {
                JSONObject rsp = JSON.parseObject(json);
                return rsp.getJSONObject("返回数据").getString("ocrText");
            }
        } catch (Exception e) {
            System.out.println("get cover ocr failed,cover:" + url);
        }
        return null;
    }
}
