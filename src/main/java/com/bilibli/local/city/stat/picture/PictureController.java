package com.bilibli.local.city.stat.picture;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bilibli.local.city.stat.commom.ImageUtil;
import com.jdcloud.sdk.utils.StringUtils;
import eud.bupt.liujun.FileUtil;
import eud.bupt.liujun.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("picture")
public class PictureController {
    private static final String outputPath = "/home/dev/liujun/picture_stat.txt";
    private static final String errorResult = "/home/dev/liujun/picture_error.txt";
    private static final String badCover = "/home/dev/liujun/bad_cover.txt";
    private static final String vertical = "/home/dev/liujun/vertical_video.csv";
    private static final String cover = "/home/dev/liujun/cover.csv";

    private static final String OcrUrl = "http://grpc-proxy.bilibili.co/main.account-law.filter-image-service/filter_image.service.v1.FilterImage/FilterImage";

    @Autowired
    private PictureDAO pictureDAO;

    public void getBadCoverUrl() {
        int id = Integer.MAX_VALUE;
        int count = 0;
        while (count < 5000) {
            List<PictureDO> data = pictureDAO.getCover(id);
            id = data.get(data.size() - 1).getId();
            Map<Long, String> covers = getCover(data);
            count += covers.size();
            for (Long dynamicId : covers.keySet()) {
                FileUtil.appendFile(cover, String.format("http://t.bilibili.com/%d,%s", dynamicId, covers.get(dynamicId)));
            }
        }
    }

    public void getVerticalVideo() {
        List<PictureDO> data = pictureDAO.getVerticalVideo();
        for (PictureDO pictureDO : data) {
            FileUtil.appendFile(vertical, String.format("http://t.bilibili.com/%d,%d", pictureDO.getDynamic_id(), pictureDO.getRid()));
        }
    }

    public void getData() {
        List<PictureDO> data = pictureDAO.getData(1, Integer.MAX_VALUE);
        Map<Long, Long> badCover = new HashMap<>();
        for (PictureDO pictureDO : data) {
            badCover.put(pictureDO.getDynamic_id(), pictureDO.getSrc_type());
        }
        Map<Long, String> coverMap = getCover(data);
        for (Long l : coverMap.keySet()) {
            FileUtil.appendFile(outputPath, String.format("%s,%s", coverMap.get(l), badCover.get(l) == 9));
        }
    }

    public void statPicture() throws InterruptedException {
        //创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 20,
                10L, TimeUnit.SECONDS, new LinkedBlockingQueue());
        FileUtil.appendFile(outputPath, "封面,命中规则");
        Map<String, Object> resultMap = new ConcurrentHashMap<>();
        for (int i = 0; i < 100; i++) {
            List<PictureDO> data = pictureDAO.list(i, Integer.MAX_VALUE);
            Map<Long, String> coverMap = getCover(data);
            for (PictureDO pictureDO : data) {
                executor.submit(() -> {
                    String words = "";
                    String cover = coverMap.get(pictureDO.getDynamic_id());
                    if (cover == null) {
                        System.out.println("get cover failed,dynamic:" + pictureDO.getDynamic_id());
                        return;
                    }
                    if (cover.contains(".gif")) {
                        return;
                    }
                    if (cover.contains(".webp")) {
                        return;
                    }
                    try {
                        ImageUtil.ImageStat imageStat = ImageUtil.getPictureStat(cover);
                        words = getPictureWords(cover);
                        if (imageStat.colors < 100) {
                            resultMap.put(String.format("%s,%d", cover, 1), 1);
                            return;
                        }
                        if (imageStat.size < 80000 && imageStat.colors < 18000) {
                            resultMap.put(String.format("%s,%d", cover, 2), 1);
                            return;
                        }
                        if (imageStat.sameLineRate > 0 && words.length() > 30) {
                            resultMap.put(String.format("%s,%d", cover, 3), 1);
                            return;
                        }
                        resultMap.put(String.format("%s,%d", cover, -1), 1);
                    } catch (Exception e) {
                        System.out.println("get cover stat failed,cover:" + cover);
                        return;
                    }
                });
            }
            while (executor.getQueue().size() > 100) {
                Thread.sleep(2000);
            }
        }
        executor.shutdown();
        while (true) {
            if (executor.isTerminated()) {
                break;
            }
            Thread.sleep(2000);
        }
        System.out.println(resultMap.size());
        for (String s : resultMap.keySet()) {
            FileUtil.appendFile(outputPath, s);
        }
    }

    private boolean heatRule(ImageUtil.ImageStat imageStat) {
        if (imageStat.colors < 100) {
            return true;
        }
        if (imageStat.sameLineRate > 0.5) {
            return true;
        }
        if (imageStat.size < 80000 && imageStat.colors < 18000) {
            return true;
        }
        return false;
    }

    private Map<Long, String> getCover(List<PictureDO> data) {
        List<PictureDO> pictureList = new LinkedList<>();
        List<PictureDO> videoList = new LinkedList<>();
        Map<Long, String> coverMap = new HashMap();
        //区分
        for (PictureDO pictureDO : data) {
            if (StringUtils.isNotBlank(pictureDO.getVertical_cover())) {
                coverMap.put(pictureDO.getDynamic_id(), pictureDO.getVertical_cover());
                continue;
            }
            if (StringUtils.isNotBlank(pictureDO.getCover_url())) {
                coverMap.put(pictureDO.getDynamic_id(), pictureDO.getCover_url());
                continue;
            }
            if (pictureDO.getType() == 2) {
                pictureList.add(pictureDO);
            } else if (pictureDO.getType() == 8) {
                videoList.add(pictureDO);
            }
        }
        //批量拉取封面
        if (pictureList.size() > 0) {
            for (int i = 0; i < pictureList.size(); i += 20) {
                Map<Long, String> picMap = getPictureCover(pictureList.subList(i, Math.min(i + 20, pictureList.size())));
                System.out.println("batch get picture cover size：" + picMap.size());
                coverMap.putAll(picMap);
            }
        }
        if (videoList.size() > 0) {
            for (int i = 0; i < videoList.size(); i += 20) {
                Map<Long, String> videoMap = getVideoCover(videoList.subList(i, Math.min(i + 20, videoList.size())));
                System.out.println("batch get picture cover size：" + videoMap.size());
                coverMap.putAll(videoMap);
            }
        }
        return coverMap;
    }

    private Map<Long, String> getVideoCover(List<PictureDO> data) {
        String url = "http://grpc-proxy.bilibili.co/archive.service/archive.service.v1.Archive/Arcs";
        Map<Integer, Long> ridMap = new HashMap<>();
        for (PictureDO pictureDO : data) {
            ridMap.put(pictureDO.getRid(), pictureDO.getDynamic_id());
        }
        Map<Long, String> result = new HashMap<>();
        JSONObject param = new JSONObject();
        param.put("aids", ridMap.keySet());
        String rsp = HttpUtil.PostWithJsonString(url, param.toJSONString());
        if (rsp != null) {
            JSONObject jsonObject = JSON.parseObject(rsp);
            if (jsonObject.containsKey("arcs")) {
                JSONObject items = jsonObject.getJSONObject("arcs");
                for (String key : items.keySet()) {
                    result.put(ridMap.get(Integer.valueOf(key)), items.getJSONObject(key).getString("Pic"));
                }
            }
        }
        return result;
    }

    private Map<Long, String> getPictureCover(List<PictureDO> data) {
        Map<Integer, Long> ridMap = new HashMap<>();
        for (PictureDO pictureDO : data) {
            ridMap.put(pictureDO.getRid(), pictureDO.getDynamic_id());
        }
        Map<Long, String> result = new HashMap<>();
        String url = "http://api.vc.bilibili.co/link_draw/v0/Doc/dynamicDetails";
        JSONObject param = new JSONObject();
        param.put("ids", ridMap.keySet());
        String json = HttpUtil.PostWithJsonString(url, param.toJSONString());
        try {
            if (json != null) {
                JSONObject rsp = JSON.parseObject(json);
                if (rsp.getInteger("code") == 0) {
                    JSONArray jsonArray = rsp.getJSONArray("data");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        result.put(ridMap.get(jsonArray.getJSONObject(i).getJSONObject("item").getInteger("id")),
                                jsonArray.getJSONObject(i).getJSONObject("item").getJSONArray("pictures")
                                        .getJSONObject(0).getString("img_src"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String getPictureWords(String url) {
        JSONObject param = new JSONObject();
        param.put("url", url);
        String json = HttpUtil.PostWithJsonString(OcrUrl, param.toJSONString());
        if (json != null) {
            JSONObject rsp = JSON.parseObject(json);
            return rsp.getString("ocrText");
        }
        return "-1";
    }

    @GetMapping("insertOnlineResult")
    public void insertOnlineResult() {
        List<String> tmp = FileUtil.readByLine("/Users/beibei/codes/java/local_city.stat/src/main/resources/final_result.csv");
        for (String s : tmp) {
            String[] sa = s.split(",", -1);
            boolean flag = false;
            for (int i = 2; i < sa.length - 1; i++) {
                if ("1".equals(sa[i].trim())) {
                    flag = true;
                }
            }
            if (flag) {
                pictureDAO.updateCvCover(sa[0]);
            }
        }
    }
}
