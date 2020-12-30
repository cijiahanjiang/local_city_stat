package com.bilibli.local.city.stat.picture;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import eud.bupt.liujun.FileUtil;
import eud.bupt.liujun.HttpUtil;
import org.springframework.util.StringUtils;

import java.util.List;

public class Test {

    private static final String OcrUrl = "http://grpc-proxy.bilibili.co/main.account-law.filter-image-service/filter_image.service.v1.FilterImage/FilterImage";


    public static void main(String[] args) {
        List<String> lists = FileUtil.readByLine("/Users/beibei/codes/java/local_city.stat/src/main/resources/tmp.csv");
        for (String s : lists) {
            String[] tmp = s.split(",");
            double same = Double.valueOf(tmp[2]);
            if (same > 0 && (!StringUtils.hasLength(tmp[1]) || "null".equals(tmp[1]))) {
                String words = getPictureWords(tmp[0]);
                FileUtil.appendFile("/Users/beibei/codes/java/local_city.stat/src/main/resources/tmp.txt", String.format("%s,%s,%f", tmp[0], words, same));
            } else {
                FileUtil.appendFile("/Users/beibei/codes/java/local_city.stat/src/main/resources/tmp.txt", s);

            }
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
