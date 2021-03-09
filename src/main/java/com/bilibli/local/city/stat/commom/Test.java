package com.bilibli.local.city.stat.commom;

import java.util.LinkedList;
import java.util.List;

public class Test {
    public static double cos(double[] a, double[] b) {
        double aa = 0;
        double a1 = 0;
        double b1 = 0;
        for (int i = 0; i < a.length; i++) {
            aa += a[i] * b[i];
            a1 += a[i] * a[i];
            b1 += b[i] * b[i];
        }
        return aa / Math.sqrt(a1) / Math.sqrt(b1);
    }

    public static void main(String[] args) {
        List<Double> s1 = new LinkedList<>();
        s1.add(1.1);
        s1.add(1.2);
        List<Double> s2 = new LinkedList<>();
        s2.add(1.2);
        s2.add(1.3);
    }
}
