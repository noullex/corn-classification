package utils.imagePreprocessing;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Binarizer {

    public static int[][] subtract(BufferedImage image, BufferedImage background) {
        if (image.getWidth() != background.getWidth() || image.getHeight() != image.getHeight()) {
            throw new IllegalArgumentException("subtraction arguments must have the same dimensions");
        }
        int[][] result = new int[image.getHeight()][image.getWidth()];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color pixelA = new Color(image.getRGB(x, y));
                Color pixelB = new Color(background.getRGB(x, y));
                result[y][x] = (int) Math.sqrt(Math.pow((pixelA.getRed() - pixelB.getRed()), 2) +
                        Math.pow((pixelA.getGreen() - pixelB.getGreen()), 2) +
                        Math.pow((pixelA.getBlue() - pixelB.getBlue()), 2));
            }
        }
        return result;
    }

    public static byte[][] binarize(int[][] input) {
        int[] hist = new int[256];
        for (int x = 0; x < input.length; x++) {
            for (int y = 0; y < input[0].length; y++) {
                int t = input[x][y] > 255 ? 255 : input[x][y];
                hist[t]++;
            }
        }
        double[] bcVariances = new double[256];
        double maxBcVariance = -1;
        int maxBcVarianceThreshold = 0;
        for (int threshold = 0; threshold < bcVariances.length; threshold++) {
            int bCount = 0, fCount = 0;
            long bSum = 0, fSum = 0;
            for (int i = 0; i < hist.length; i++) {
                if (i >= threshold) {
                    fSum += hist[i] * i;
                    fCount += hist[i];
                } else {
                    bSum += hist[i] * i;
                    bCount += hist[i];
                }
            }
            try {
                double fW = (double) fCount / input.length;
                double bW = (double) bCount / input.length;
                bcVariances[threshold] = fW * bW * Math.pow(((double) bSum / bCount - (double) fSum / fCount), 2);
            } catch (ArithmeticException exception) {
                bcVariances[threshold] = -1;
            }
            if (bcVariances[threshold] > maxBcVariance) {
                maxBcVariance = bcVariances[threshold];
                maxBcVarianceThreshold = threshold;
            }
        }

        byte[][] result = new byte[input.length][input[0].length];
        for (int x = 0; x < input.length; x++) {
            for (int y = 0; y < input[0].length; y++) {
                if (input[x][y] >= maxBcVarianceThreshold) {
                    result[x][y] = 1;
                } else {
                    result[x][y] = 0;
                }
            }
        }
        return result;
    }
}
