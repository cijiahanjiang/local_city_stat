package com.bilibli.local.city.stat.es;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonObject;
import eud.bupt.liujun.HttpUtil;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EsController {

    private static String url1 = "172.22.8.62:9200/music-song-es1/_search?scroll=20m&pretty";

    public int countSongs() {
        JsonObject param1 = new JsonObject();
        param1.addProperty("from", 0);
        param1.addProperty("size", 1000);
        param1.addProperty("sort", "_id");
        param1.addProperty("_source", "songId");
        String rsp = HttpUtil.PostWithJsonString(url1, param1.toString());
        JSON.parseObject(rsp);
    }
}
