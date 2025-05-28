package org.playground;

import java.util.Random;

public class NormalDistributionAnalysis {
    public static void main(String[] args) {
        Random random = new Random(1);
        double sigma = 2;

        double value = 0;
        for (int i = 0; i < 10000; i++) {
            double x = Math.abs(random.nextGaussian() * sigma);
            value += Math.exp(x) / (1 + Math.exp(x));
        }
        System.out.println(value / 10000);
    }
}
