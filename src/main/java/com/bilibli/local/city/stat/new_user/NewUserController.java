package com.bilibli.local.city.stat.new_user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bilibli.local.city.stat.picture.PictureDAO;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
public class NewUserController {

    @Autowired
    private PictureDAO pictureDAO;

    private static final String path = "/Users/beibei/codes/java/local_city.stat/src/main/resources/同城投放内容/";


    private static final String DYNAMIC_DETAIL_URL = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/get_dynamic_detail?dynamic_id=%d";

    public void run() {

    }

    public static Map<String, Integer> CITY_MAP = new HashMap<>();

    static {
        CITY_MAP.put("上海", 1);
        CITY_MAP.put("杭州", 2);
        CITY_MAP.put("广州", 3);
        CITY_MAP.put("成都", 4);
        CITY_MAP.put("太原", 6);
        CITY_MAP.put("深圳", 7);
        CITY_MAP.put("重庆", 8);
        CITY_MAP.put("武汉", 9);
        CITY_MAP.put("南京", 10);
        CITY_MAP.put("天津", 11);
        CITY_MAP.put("西安", 12);
        CITY_MAP.put("长沙", 13);
        CITY_MAP.put("东莞", 14);
        CITY_MAP.put("郑州", 15);
        CITY_MAP.put("苏州", 16);
        CITY_MAP.put("合肥", 17);
        CITY_MAP.put("福州", 18);
    }

    public static void main(String[] args) {
        File file = new File(path);
        String[] fileNames = file.list();
        for (String fileName : fileNames) {
            if (fileName.contains("xlsx")) {
                readExcel(fileName);
            }
        }
    }

    private static void readExcel(String fileName) {
        // 读取文件获取工作薄
        Map<String, Double> dynamicMap = new HashMap<>();
        try {
            // 获取工作表数量
            // 获取工作表
            FileInputStream inputStream = new FileInputStream(path + fileName);
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = xssfWorkbook.getSheetAt(1);
            for (int j = 1; j <= sheet.getLastRowNum(); j++) {
                // 获取行
                XSSFRow row = sheet.getRow(j);
                Double playNumbers;
                if (row.getCell(5).getCellType() == CellType.STRING) {
                    playNumbers = Double.valueOf(row.getCell(5).getStringCellValue());
                } else if (row.getCell(5).getCellType() == CellType.NUMERIC) {
                    playNumbers = row.getCell(5).getNumericCellValue();
                } else {
                    continue;
                }
                Double rid;
                if (row.getCell(2).getCellType() == CellType.STRING) {
                    rid = Double.valueOf(row.getCell(2).getStringCellValue());
                } else if (row.getCell(2).getCellType() == CellType.NUMERIC) {
                    rid = row.getCell(2).getNumericCellValue();
                } else {
                    continue;
                }
                String dynamicId = row.getCell(0).getStringCellValue();
                int city = CITY_MAP.get(fileName.split("\\.")[0]);
                long uid = getUid(Long.valueOf(dynamicId));
                System.out.println(String.format("city:%d,dynamic:%s,mid:%s,rid:%s,playNums:%s",
                        city, Long.valueOf(dynamicId), uid, rid.intValue(), playNumbers.intValue()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static long getUid(long dynamicId) {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(String.format(DYNAMIC_DETAIL_URL, dynamicId));
        try {
            httpClient.executeMethod(getMethod);
            String result = getMethod.getResponseBodyAsString();
            if (result != null) {
                JSONObject jsonObject = JSON.parseObject(result);
                if (jsonObject.getInteger("code") == 0) {
                    return jsonObject.getJSONObject("data").getJSONObject("card").getJSONObject("desc").getLong("uid");
                }
            }
        } catch (Exception e) {

        }
        return 0;
    }

    private static void postWithForm(Map<String, String> param, String url) {
        try {
            HttpClient httpClient = new HttpClient();

            PostMethod postMethod = new PostMethod(url);
            NameValuePair[] nameValuePairs = new NameValuePair[param.size()];
            int index = 0;
            for (String key : param.keySet()) {
                nameValuePairs[index++] = new NameValuePair(key, param.get(key));
            }
            postMethod.setRequestBody(nameValuePairs);
            httpClient.executeMethod(postMethod);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

