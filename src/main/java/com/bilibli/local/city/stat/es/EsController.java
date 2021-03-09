package com.bilibli.local.city.stat.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import eud.bupt.liujun.HttpUtil;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RestController
public class EsController {

    private static String url1 = "http://172.22.8.62:9200/music-song-es1/_search?scroll=20m&pretty";


    private static String url2 = "http://172.22.8.62:9200/_search/scroll";

    public int countSongs() {
        List<Integer> songIds = new LinkedList<>();
        JSONObject param1 = new JSONObject();
        param1.put("from", 0);
        param1.put("size", 3000);
        param1.put("sort", "_id");
        param1.put("_source", "songId");
        String rsp = HttpUtil.PostWithJsonString(url1, param1.toString());
        Result result = parseResult(rsp);
        songIds.addAll(result.songIds);

        while (result.songIds != null) {
            JSONObject param2 = new JSONObject();
            param2.put("scroll_id", result._scroll_id);
            param2.put("scroll", "60m");
            String rsp1 = HttpUtil.PostWithJsonString(url2, param2.toString());
            result = parseResult(rsp1);
            System.out.println(result.songIds.size());
            songIds.addAll(result.songIds);
            System.out.println("total:" + songIds.size());
        }
        System.out.println(songIds.size());
        return songIds.size();
    }

    private Result parseResult(String body) {
        Result result = new Result();
        List<Integer> songIds = new LinkedList<>();
        JSONObject response = JSON.parseObject(body);
        result._scroll_id = response.getString("_scroll_id");
        JSONObject rows = response.getJSONObject("hits");

        JSONArray jsonArray = rows.getJSONArray("hits");
        if (jsonArray == null || jsonArray.size() == 0) {
            return result;
        }
        for (Object o : jsonArray) {
            songIds.add(((JSONObject) o).getJSONObject("_source").getInteger("songId"));
        }
        result.songIds = songIds;
        return result;
    }

    private class Result {
        public String _scroll_id;
        public List<Integer> songIds;
    }
}
