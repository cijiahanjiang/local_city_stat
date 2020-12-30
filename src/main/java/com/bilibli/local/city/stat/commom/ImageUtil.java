package com.bilibli.local.city.stat.commom;

import eud.bupt.liujun.ImageStat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ImageUtil {

    public static void main(String[] args) throws Exception {
        BufferedImage image = ImageIO.read(new URL("http://i2.hdslb.com/bfs/archive/25e4214d7a5baca3b2fa17d05574a74abce7aa26.jpg"));
        double ss = getSameLineRate(image);
        System.out.println(ss);
    }

    public static ImageStat getPictureStat(String url) {
        ImageStat imageStat = new ImageStat();
        try {
            BufferedImage image = ImageIO.read(new URL(url));
            imageStat.size = getSize(image);
            imageStat.colorRate = getColorCoverRate(image);
            imageStat.sameLineRate = getSameLineRate(image);
            return imageStat;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
}
