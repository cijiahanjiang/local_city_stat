package com.bilibli.local.city.stat.picture;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bilibli.local.city.stat.commom.ImageUtil;
import com.jdcloud.sdk.utils.StringUtils;
import eud.bupt.liujun.FileUtil;
import eud.bupt.liujun.HttpUtil;
import eud.bupt.liujun.ImageStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("picture")
public class PictureController {
    private static final String outputPath = "/home/dev/liujun/picture_stat.txt";

    @Autowired
    private PictureDAO pictureDAO;

    //    @PostConstruct
    public int fetchOnlineData() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(80, 80,
                10L, TimeUnit.SECONDS, new LinkedBlockingQueue());
        try {
            long currentTime = System.currentTimeMillis();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -4);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = simpleDateFormat.format(calendar.getTime());
            FileUtil.appendFile(outputPath, "动态,封面,色彩丰富度,相似行数,");
            for (int i = 0; i < 100; i++) {
                int offset = Integer.MAX_VALUE;
                List<PictureDO> data = pictureDAO.list(i, offset, time);
                while (data.size() > 0) {
                    Map<Integer, String> coverMap = getCover(data);
                    for (PictureDO pictureDO : data) {
                        offset = pictureDO.getId();
                        String cover = coverMap.get(pictureDO.getRid());
                        if (cover != null) {
                            if (cover.endsWith("webp")) {
                                continue;
                            }
                            if (cover.endsWith("gif")) {
//                                FileUtil.appendFile(outputPath, String.format("https://t.bilibili.com/%d,%s,%d,%d,%d", pictureDO.getDynamic_id(), cover, -1, -1, -2));
                            } else {
                                executor.submit(() -> {
                                    ImageUtil.getPictureStat(cover);
                                });
                            }
                        }
                    }
                    while (executor.getQueue().size() > 20) {
                        System.out.println(executor.getQueue().size());
                        Thread.sleep(2000);
                    }
                    System.out.println("try to get next data:" + offset + ",table:" + i);
                    data = pictureDAO.list(i, offset, time);
                }
            }
            System.out.println("job done:" + (System.currentTimeMillis() - currentTime));
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    //    @PostConstruct
    public void statPicture() throws InterruptedException {
        //创建线程池
        long t1 = System.currentTimeMillis();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 20,
                10L, TimeUnit.SECONDS, new LinkedBlockingQueue());
        int offset = 0;
        List<PictureDO> data = pictureDAO.getFromLocal(offset);
        while (data.size() > 0) {
            for (PictureDO pictureDO : data) {
                offset = pictureDO.getId();
                executor.submit(() -> {
                    if (pictureDO.getCover_url().contains(".gif")) {
                        return;
                    }
                    System.out.println(JSON.toJSONString(pictureDO));
                    ImageStat imageStat = ImageUtil.getPictureStat(pictureDO.getCover_url());
                    pictureDAO.updateAll(pictureDO.getId(), imageStat.sameLineRate, imageStat.colorRate, imageStat.size);
                });
            }
            while (executor.getQueue().size() > 100) {
                System.out.println(executor.getQueue().size());
                Thread.sleep(2000);
            }
            data = pictureDAO.getFromLocal(offset);
        }
        System.out.println(System.currentTimeMillis() - t1);
    }

    //    @PostConstruct
    public void fixData() throws Exception {
        List<String> sa = FileUtil.readByLine("/home/dev/liujun/tmp.txt");
        for (int i = 0; i < sa.size(); i++) {
            System.out.println(sa.get(i));
            String[] s = sa.get(i).split("\t");
            pictureDAO.updateBadData(s[0], s[1]);
            Thread.sleep(50);
        }
    }

    private Map<Integer, String> getCover(List<PictureDO> data) {
        List<Integer> picturesToFetchCover = new LinkedList<>();
        List<Integer> videosToFetchCover = new LinkedList<>();
        Map<Integer, String> coverMap = new HashMap();
        //区分
        for (PictureDO pictureDO : data) {
            if (StringUtils.isNotBlank(pictureDO.getVertical_cover())) {
                coverMap.put(pictureDO.getRid(), pictureDO.getVertical_cover());
                continue;
            }
            if (StringUtils.isNotBlank(pictureDO.getCover_url())) {
                coverMap.put(pictureDO.getRid(), pictureDO.getCover_url());
                continue;
            }
            if (pictureDO.getType() == 2) {
                picturesToFetchCover.add(pictureDO.getRid());
            } else if (pictureDO.getType() == 8) {
                videosToFetchCover.add(pictureDO.getRid());
            }
        }
        //批量拉取封面
        if (picturesToFetchCover.size() > 0) {
            getPictureCover(picturesToFetchCover, coverMap);
        }
        if (videosToFetchCover.size() > 0) {
            getVideoCover(videosToFetchCover, coverMap);
        }
        return coverMap;
    }

    private void getVideoCover(List<Integer> rids, Map<Integer, String> coverMap) {
        String url = "http://grpc-proxy.bilibili.co/archive.service/archive.service.v1.Archive/Arcs";
        JSONObject param = new JSONObject();
        param.put("aids", rids);
        String rsp = HttpUtil.PostWithJsonString(url, param.toJSONString());
        if (rsp != null) {
            JSONObject jsonObject = JSON.parseObject(rsp);
            if (jsonObject.containsKey("arcs")) {
                JSONObject items = jsonObject.getJSONObject("arcs");
                for (String key : items.keySet()) {
                    coverMap.put(Integer.valueOf(key), items.getJSONObject(key).getString("Pic"));
                }
            }
        }
        return;
    }

    private void getPictureCover(List<Integer> rids, Map<Integer, String> coverMap) {
        String url = "http://api.vc.bilibili.co/link_draw/v0/Doc/dynamicDetails";
        JSONObject param = new JSONObject();
        param.put("ids", rids);
        String json = HttpUtil.PostWithJsonString(url, param.toJSONString());
        try {
            if (json != null) {
                JSONObject rsp = JSON.parseObject(json);
                if (rsp.getInteger("code") == 0) {
                    JSONArray jsonArray = rsp.getJSONArray("data");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        coverMap.put(jsonArray.getJSONObject(i).getJSONObject("item").getInteger("id"),
                                jsonArray.getJSONObject(i).getJSONObject("item").getJSONArray("pictures")
                                        .getJSONObject(0).getString("img_src"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }
}
