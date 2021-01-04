package com.bilibli.local.city.stat.commom;

import com.alibaba.fastjson.JSON;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ImageUtil {


    public static void main(String[] args) throws Exception {
        ImageStat imageStat = getPictureStat("http://i0.hdslb.com/bfs/archive/29955ca4660a84d33b5051f5fc1e4ce4b93453b4.jpg");
        System.out.println(JSON.toJSONString(imageStat));
    }

    public static ImageStat getPictureStat(String url) throws Exception {
        ImageStat imageStat = new ImageStat();
        BufferedImage image = ImageIO.read(new URL(url));
        imageStat.size = getSize(image);
        imageStat.colorRate = getColorCoverRate(image);
        imageStat.sameLineRate = getSameLineRate(image);
        imageStat.colors = (int) (imageStat.colorRate * imageStat.size);
        return imageStat;
    }

    public static int getSize(BufferedImage image) {
        try {
            int x = image.getWidth();
            int y = image.getHeight();
            return x * y;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double getColorCoverRate(BufferedImage image) {
        Set<Integer> set = new HashSet();
        try {
            StatLocation statLocation = getStatLocation(image);
            for (int i = statLocation.xStart; i < statLocation.xEnd; i = i + 1) {
                for (int j = statLocation.yStart; j < statLocation.yEnd; j = j + 1) {
                    set.add(image.getRGB(i, j));
                }
            }
            return (set.size() + 0.0) / ((statLocation.xEnd - statLocation.xStart) * (statLocation.yEnd - statLocation.yStart));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }


    public static double getSameLineRate(String url) {
        try {
            BufferedImage image = ImageIO.read(new URL(url));
            return getSameLineRate(image);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static double getSameLineRate(BufferedImage image) {
        try {
            int hitLines = 0;
            int total = 0;
            int middleSameLines = 0;
            int middleTotal = 0;
            StatLocation statLocation = getStatLocation(image);
            for (int j = statLocation.yStart; j < statLocation.yEnd; j = j + 1) {
                Color color1 = new Color(image.getRGB(0, j));
                int red = color1.getRed();
                int green = color1.getGreen();
                int blue = color1.getBlue();
                boolean flag = true;
                for (int i = statLocation.xStart; i < statLocation.xEnd; i = i + 1) {
                    Color color = new Color(image.getRGB(i, j));
                    if (Math.abs(color.getRed() - red) > 10 || Math.abs(color.getBlue() - blue) > 10 || Math.abs(color.getGreen() - green) > 10) {
                        flag = false;
                        break;
                    }
                }
                total++;
                if (flag) {
                    hitLines++;
                }
                if (Math.abs((statLocation.yStart + statLocation.yEnd) / 2 - j) < (statLocation.yStart + statLocation.yEnd) / 10) {
                    if (flag) {
                        middleSameLines++;
                    }
                    middleTotal++;
                }
            }
            return Math.min((hitLines + 0.0) / total, (middleSameLines + 0.0) / middleTotal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private static StatLocation getStatLocation(BufferedImage image) {
        int x = image.getWidth();
        int y = image.getHeight();
        StatLocation statLocation = new StatLocation(x, y);
        return statLocation;
    }

    static class StatLocation {
        public StatLocation(int x, int y) {
            this.xStart = x / 10;
            this.xEnd = (int) (x * 0.9);
            this.yStart = y / 10;
            this.yEnd = (int) (y * 0.9);
        }

        public int xStart;
        public int xEnd;
        public int yStart;
        private int yEnd;
    }

    public static class ImageStat {
        public int size;
        public double colorRate;
        public double sameLineRate;
        public int colors;

        public ImageStat() {
        }
    }
}
