package com.bilibli.local.city.stat.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jsoup.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
public class AiTestController {

    @Autowired
    private AiDAO aiDAO;

    public Object mai(int mid) throws Exception {
        return 1;
    }

    @GetMapping("scan")
    public List<DynDO> scan(@RequestParam("mid") int mid,
                            @RequestParam("city") int city) {
        int offset = 0;
        List<DynDO> result = new LinkedList<>();
        List<DynDO> data = aiDAO.getData(city, 0);
        List<Integer> rids = new LinkedList<>();
        while (data.size() > 0) {
            rids.clear();
            for (DynDO dynDO : data) {
                rids.add(dynDO.rid);
                offset = dynDO.id;
            }
            Map<Integer, Double> aiMap = getAiScore(mid, rids);
            for (DynDO dynDO : data) {
                if (aiMap.containsKey(dynDO.rid)) {
                    dynDO.ai_score = aiMap.get(dynDO.rid);
                    result.add(dynDO);
                }
            }
            result = result.stream().sorted(Comparator.comparing(DynDO::getAi_score).reversed()).collect(Collectors.toList());
            result = result.subList(0, 100);
            data = aiDAO.getData(city, offset);
        }
        for (int i = 0; i < result.size(); i++) {
            result.get(i).heat = aiDAO.getHeat(result.get(i).dynamic_id);
            result.get(i).ai_rank = i + 1;
        }
        result = result.stream().sorted(Comparator.comparing(DynDO::getHeat).reversed()).collect(Collectors.toList());
        for (int i = 0; i < result.size(); i++) {
            result.get(i).heat_rank = i + 1;
        }
        result = result.stream().sorted(Comparator.comparing(DynDO::getAi_score).reversed()).collect(Collectors.toList());
        return result;
    }

    public Map<Integer, Double> getAiScore(int mid, List<Integer> rids) {
        Map<Integer, Double> aiScore = new HashMap<>();
        String url = "http://data.bilibili.co/recommand";
        String param = String.format("?cmd=%s&mid=%d&avids=%s&request_cnt=%d", "bcity", mid, StringUtil.join(rids, ","), 100);
        GetMethod getMethod = new GetMethod(url + param);
        HttpClient httpClient = new HttpClient();
        try {
            httpClient.executeMethod(getMethod);
            String sa = getMethod.getResponseBodyAsString();
            JSONObject jsonObject = JSON.parseObject(sa);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    int rid = jsonArray.getJSONObject(i).getInteger("id");
                    double score = jsonArray.getJSONObject(i).getDouble("score");
                    aiScore.put(rid, score);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return aiScore;
    }

}
