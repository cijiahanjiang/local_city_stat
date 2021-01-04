package com.bilibli.local.city.stat.picture;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bilibli.local.city.stat.commom.ImageUtil;
import eud.bupt.liujun.FileUtil;
import eud.bupt.liujun.HttpUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Test {

    private static final String OcrUrl = "http://grpc-proxy.bilibili.co/main.account-law.filter-image-service/filter_image.service.v1.FilterImage/FilterImage";

    private static Map<String, Object> resultMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        try {
            List<String> covers = FileUtil.readByLine("/Users/beibei/codes/java/local_city.stat/src/main/resources/tmp.txt");

            for (String cover : covers) {
                cover = cover.split(",")[0];
                if (cover.contains("gif")) {
                    FileUtil.appendFile("/Users/beibei/codes/java/local_city.stat/src/main/resources/tmp1.txt", String.format("%s,%d,%d,%f,%f,%s", cover, 0, 0, 0.0, 0.0, ""));
                } else {
                    ImageUtil.ImageStat imageStat = ImageUtil.getPictureStat(cover);
                    String words = "";
                    if (imageStat.sameLineRate > 0) {
                        words = getPictureWords(cover);
                    }
                    FileUtil.appendFile("/Users/beibei/codes/java/local_city.stat/src/main/resources/tmp1.txt", String.format("%s,%d,%d,%f,%f,%s", cover, imageStat.size, imageStat.colors, imageStat.sameLineRate, imageStat.colorRate, words));
                }
            }
        } catch (Exception e) {

        }
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
