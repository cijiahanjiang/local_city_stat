package com.bilibli.local.city.stat.new_user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
public class NewUserController {

    private static final String path = "/Users/beibei/codes/java/local_city.stat/src/main/resources/同城投放内容/";

    private static final String DYNAMIC_DETAIL_URL = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/get_dynamic_detail?dynamic_id=%d";

    public static void main(String[] args) {
        readFile();
    }

    private static Map<String, DynamicDO> readFile() {
        Map<String, DynamicDO> allData = new HashMap<>();
        try {
            FileInputStream inputStream = new FileInputStream("/Users/beibei/codes/java/local_city.stat/src/main/resources/data/上海理工大学.xlsx");
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = xssfWorkbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                String dynamicId = getCellValue(sheet.getRow(i).getCell(0));
                //调接口拿
                String rid = getCellValue(sheet.getRow(i).getCell(3));
                String mid = getCellValue(sheet.getRow(i).getCell(4));
                String playNum = getCellValue(sheet.getRow(i).getCell(5));
                DynamicDO dynamicDO = new DynamicDO();
                dynamicDO.setDynamicId(dynamicId);
                dynamicDO.setRid(rid);
                dynamicDO.setMid(mid);
                dynamicDO.setPlayNum(playNum);
                allData.put(dynamicId, dynamicDO);
            }
            XSSFSheet sheet1 = xssfWorkbook.getSheetAt(3);
//            for (int i = 1; i <= 10; i++) {
            for (int i = 1; i <= sheet1.getLastRowNum(); i++) {
                String dynamicId = getCellValue(sheet1.getRow(i).getCell(0));
                DynamicDO dynamicDO = allData.get(dynamicId);
                Map<String, String> param = new HashMap<>();
                param.put("city_id", "1");
                param.put("dynamic_id", dynamicId);
                param.put("uid", dynamicDO.getMid());
                param.put("rid", dynamicDO.getRid());
                param.put("play", dynamicDO.getPlayNum());
                param.put("school_id", "5");
                param.put("school_name", "上海财经大学");
                // 1 上海师范  2复旦 3同济大学 4上海财经大学 5上海理工大学
                System.out.println(JSON.toJSONString(param));
                saveData(param);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allData;
    }

    private static String getCellValue(XSSFCell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        }
        if (cell.getCellType() == CellType.FORMULA) {
            return String.valueOf(cell.getStringCellValue());
        }
        return "";
    }

    private static void readExcel(String fileName) {
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

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveData(Map<String, String> param) {

        postWithForm(param, "http://manager-pre.bilibili.co/x/admin/dynamic/localcity/newuser/add");
    }

    private static long getRid(long dynamicId) {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(String.format(DYNAMIC_DETAIL_URL, dynamicId));
        try {
            httpClient.executeMethod(getMethod);
            String result = getMethod.getResponseBodyAsString();
            if (result != null) {
                JSONObject jsonObject = JSON.parseObject(result);
                if (jsonObject.getInteger("code") == 0) {
                    return jsonObject.getJSONObject("data").getJSONObject("card").getJSONObject("desc").getLong("rid");
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
            postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            postMethod.setRequestBody(nameValuePairs);
            postMethod.setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
            httpClient.executeMethod(postMethod);
            System.out.println(postMethod.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

