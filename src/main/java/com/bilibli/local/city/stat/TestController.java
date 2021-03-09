package com.bilibli.local.city.stat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bilibili.dynamic.databus.api.EsUtil;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class TestController {

    @GetMapping("/esquery")
    public static void esScroll() {
        List<Double> vec = new LinkedList<>();
        double[] param = {-0.10419999808073044, 0.1972000002861023, -0.2939999997615814, 0.17100000381469727, 0.03790000081062317, -0.2020999938249588, 0.022600000724196434, -0.11209999769926071, -0.40529999136924744, 0.12439999729394913, 0.2648000121116638, 0.20520000159740448, -0.26409998536109924, 0.08609999716281891, 0.8213000297546387, -0.026799999177455902, 0.35040000081062317, 0.13369999825954437, 0.16169999539852142, 0.24699999392032623, 0.11919999867677689, 0.1264999955892563, 0.3749000132083893, -0.3041999936103821, 0.011800000444054604, -0.014399999752640724, -0.011099999770522118, 0.6583999991416931, 0.125900000333786, 0.376800000667572, 0.14579999446868896, 0.05139999836683273, 0.2612999975681305, -0.1688999980688095, -0.43709999322891235, 0.15520000457763672, -0.1534000039100647, 0.17739999294281006, -0.39079999923706055, 0.010599999688565731, -0.020400000736117363, -0.193900004029274, -0.31869998574256897, -0.06970000267028809, -0.06019999831914902, -0.16840000450611115, 0.012799999676644802, 0.13650000095367432, 0.48399999737739563, -0.04500000178813934, -0.23819999396800995, 0.193900004029274, -0.211899995803833, 0.04399999976158142, -0.07680000364780426, -0.06239999830722809, 1.9999999494757503E-4, -0.4296000003814697, -0.0575999990105629, -0.2558000087738037, -0.3718999922275543, 0.37560001015663147, -0.29190000891685486, 0.21400000154972076};
        for (double d : param) {
            vec.add(d);
        }
        List<VecDO> queryResult = esQuery(vec);
        System.out.println(JSON.toJSONString(queryResult));
        Set<Integer> cityIds = new HashSet<>();
        queryResult.forEach(vecDO -> vecDO.cityIds.forEach(id -> cityIds.add(id)));
        cityIds.forEach(id -> {
            List<Long> dynamic_ids = new LinkedList<>();
            for (VecDO vecDO : queryResult) {
                if (dynamic_ids.size() >= 50) {
                    break;
                }
                if (vecDO.cityIds.contains(id)) {
                    dynamic_ids.add(vecDO.dynamicId);
                }
            }
            System.out.println(String.format("key:%s,value:%s", id, JSON.toJSONString(dynamic_ids)));
        });

    }

    public static List<VecDO> esQuery(List<Double> ids) {
        List<VecDO> data = new LinkedList<>();
        Request request = new Request("GET", "tianmavec-v3/_search");
        String queryParam = "{\"query\":{\"function_score\":{\"boost_mode\":\"replace\",\"script_score\":{\"script\":{\"source\":\"binary_vector_score\",\"lang\":\"knn\",\"params\":{\"cosine\":false,\"field\":\"embedding_vector\",\"vector\":%s}}}}},\"_source\":[\"dynamic_id\",\"city_ids\"],\"size\":1000}";
        request.setJsonEntity(String.format(queryParam, JSON.toJSONString(ids)));
        try {
            Response response = EsUtil.cityClient.performRequest(request);
            String result = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                    .lines().collect(Collectors.joining(System.lineSeparator()));
            JSONObject esQueryResult = JSON.parseObject(result);
            JSONObject hits = esQueryResult.getJSONObject("hits");
            if (hits != null) {
                JSONArray jsonArray = hits.getJSONArray("hits");
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject _source = jsonArray.getJSONObject(i).getJSONObject("_source");
                        if (_source != null) {
                            data.add(new VecDO(_source.getLong("dynamic_id"), _source.getJSONArray("city_ids").toJavaList(Integer.class)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    static class VecDO {
        public VecDO(long dynamicId, List<Integer> cityIds) {
            this.dynamicId = dynamicId;
            this.cityIds = cityIds;
        }

        public VecDO(String docId, long updateTime) {
            this.docId = docId;
            this.updateTime = updateTime;
        }

        public String docId;
        public long dynamicId;
        public List<Integer> cityIds;
        public long updateTime;
    }
}
