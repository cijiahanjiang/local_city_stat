package com.bilibli.local.city.stat.picture;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bilibli.local.city.stat.commom.ImageUtil;
import com.jdcloud.sdk.utils.StringUtils;
import eud.bupt.liujun.FileUtil;
import eud.bupt.liujun.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("picture")
public class PictureController {
    private static final String outputPath = "/home/dev/liujun/picture_stat.txt";

    private static final String OcrUrl = "http://grpc-proxy.bilibili.co/main.account-law.filter-image-service/filter_image.service.v1.FilterImage/FilterImage";

    @Autowired
    private PictureDAO pictureDAO;

    //    @PostConstruct
//    public int fetchOnlineData() {
//        ThreadPoolExecutor executor = new ThreadPoolExecutor(80, 80,
//                10L, TimeUnit.SECONDS, new LinkedBlockingQueue());
//        try {
//            long currentTime = System.currentTimeMillis();
//            Calendar calendar = Calendar.getInstance();
//            calendar.add(Calendar.HOUR_OF_DAY, -4);
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String time = simpleDateFormat.format(calendar.getTime());
//            FileUtil.appendFile(outputPath, "动态,封面,色彩丰富度,相似行数,");
//            for (int i = 0; i < 100; i++) {
//                int offset = Integer.MAX_VALUE;
//                List<PictureDO> data = pictureDAO.list(i, offset, time);
//                while (data.size() > 0) {
//                    Map<Integer, String> coverMap = getCover(data);
//                    for (PictureDO pictureDO : data) {
//                        offset = pictureDO.getId();
//                        String cover = coverMap.get(pictureDO.getRid());
//                        if (cover != null) {
//                            if (cover.endsWith("webp")) {
//                                continue;
//                            }
//                            if (cover.endsWith("gif")) {
//                                FileUtil.appendFile(outputPath, String.format("https://t.bilibili.com/%d,%s,%d,%d,%d", pictureDO.getDynamic_id(), cover, -1, -1, -2));
//                            } else {
//                                executor.submit(() -> {
//                                    ImageUtil.getPictureStat(cover);
//                                });
//                            }
//                        }
//                    }
//                    while (executor.getQueue().size() > 20) {
//                        System.out.println(executor.getQueue().size());
//                        Thread.sleep(2000);
//                    }
//                    System.out.println("try to get next data:" + offset + ",table:" + i);
//                    data = pictureDAO.list(i, offset, time);
//                }
//            }
//            System.out.println("job done:" + (System.currentTimeMillis() - currentTime));
//            return 1;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }

    @PostConstruct
    public void statPicture() throws InterruptedException {
        //创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 20,
                10L, TimeUnit.SECONDS, new LinkedBlockingQueue());
        FileUtil.appendFile(outputPath, "封面,是否badcase,文本,命中长文本");
        for (int i = 0; i < 100; i++) {
            List<PictureDO> data = pictureDAO.list(i, Integer.MAX_VALUE);
            Map<Long, String> coverMap = getCover(data);
            for (PictureDO pictureDO : data) {
                executor.submit(() -> {
                    boolean flag = false;
                    boolean flag1 = false;
                    String words = "";
                    String cover = coverMap.get(pictureDO.getDynamic_id());
                    if (cover != null && cover.contains(".gif")) {
                        flag = true;
                    } else {
                        try {
                            ImageUtil.ImageStat imageStat = ImageUtil.getPictureStat(cover);
                            flag = heatRule(imageStat);
                            if (!flag && imageStat.sameLineRate > 0) {
                                words = getPictureWords(cover);
                                if (words != null && words.length() > 20) {
                                    flag = true;
                                    flag1 = true;
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("get cover stat failed,cover:" + cover);
                        }
                    }
                    FileUtil.appendFile(outputPath, String.format("%s,%b,%s,%b", cover, flag, words, flag1));
                });
            }
            while (executor.getQueue().size() > 100) {
                Thread.sleep(2000);
            }
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
            Map<Long, String> picMap = getPictureCover(pictureList);
            coverMap.putAll(picMap);
        }
        if (videoList.size() > 0) {
            Map<Long, String> videoMap = getVideoCover(videoList);
            coverMap.putAll(videoMap);
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

    private String getPictureWords(String url) {
        JSONObject param = new JSONObject();
        param.put("url", url);
        String json = HttpUtil.PostWithJsonString(OcrUrl, param.toJSONString());
        if (json != null) {
            JSONObject rsp = JSON.parseObject(json);
            return rsp.getString("ocrText");
        }
        return "";
    }
}
