package com.bilibli.local.city.stat.commom;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import eud.bupt.liujun.FileUtil;
import org.jsoup.internal.StringUtil;

import java.util.LinkedList;
import java.util.List;

public class JsonCsvUtil {

    public static void jsonToCsv(String source) {
        String output = source + ".csv";
        String s = FileUtil.readAsString(source);
        JSONArray jsonArray = JSON.parseArray(s);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            List<String> data = new LinkedList<>();
            if (i == 0) {
                FileUtil.appendFile(output, StringUtil.join(jsonObject.keySet(), ","));
            }
            for (String key : jsonObject.keySet()) {
                data.add(jsonObject.getString(key));
            }
            FileUtil.appendFile(output, StringUtil.join(data, ","));
        }
    }

    public static void main(String[] args) {
        String path = "/Users/beibei/codes/java/local_city.stat/src/main/resources/张影.json";
        jsonToCsv(path);
    }
}
