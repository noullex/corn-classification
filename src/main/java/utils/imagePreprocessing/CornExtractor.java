package utils.imagePreprocessing;

import sun.misc.Queue;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class CornExtractor {

    public static Map<Corn, BufferedImage> extract(BufferedImage image, BufferedImage background) throws InterruptedException {
        List<Corn> corns = getCornsFromImage(image, background);
        Map<Corn, BufferedImage> extraction = new HashMap<>();
        for (int i = 0; i < corns.size(); i++) {
            Corn corn = corns.get(i);
            BufferedImage cornImage = new BufferedImage(corn.maxX - corn.minX + 1, corn.maxY - corn.minY + 1, TYPE_INT_RGB);
            for (Point pixel : corn.points) {
                cornImage.setRGB(pixel.x - corn.minX, pixel.y - corn.minY, image.getRGB(pixel.x, pixel.y));
            }
            extraction.put(corn, cornImage);
        }
        return extraction;
    }

    private static List<Corn> getCornsFromImage(BufferedImage image, BufferedImage background) throws InterruptedException {
        byte[][] binaryImage = Binarizer.binarize(Binarizer.subtract(background, image));
        Queue<Point> curPoints = new Queue<>();
        List<Corn> result = new ArrayList<>();
        byte curLabel = 2;
        for (int x = 0; x < binaryImage.length; x++) {
            for (int y = 0; y < binaryImage[0].length; y++) {
                if (binaryImage[x][y] == 1) {
                    binaryImage[x][y] = curLabel;
                    Corn corn = new Corn();
                    corn.minX = corn.maxX = x;
                    corn.minY = corn.maxY = y;
                    curPoints.enqueue(new Point(x, y));
                    while (curPoints.elements().hasMoreElements()) {
                        Point p = curPoints.dequeue();
                        corn.points.add(p);
                        if (p.x > corn.maxX) corn.maxX = p.x;
                        if (p.x < corn.minX) corn.minX = p.x;
                        if (p.y > corn.maxY) corn.maxY = p.y;
                        if (p.y < corn.minY) corn.minY = p.y;
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dy = -1; dy <= 1; dy++) {
                                int nx = p.x + dx;
                                int ny = p.y + dy;
                                if (nx >= 0 && ny >= 0 && nx < binaryImage.length && ny < binaryImage[0].length
                                        && binaryImage[nx][ny] == 1 && dy != dx && -dy != dx) {
                                    binaryImage[nx][ny] = curLabel;
                                    curPoints.enqueue(new Point(nx, ny));
                                }
                            }
                        }
                    }
                    if ((corn.maxY - corn.minY) > 2 && (corn.maxX - corn.minX) > 2) {
                        result.add(corn);
                    }
                }
            }
        }
        return result;
    }

    public static class Corn {
        int minX, minY, maxX, maxY;
        public List<Point> points = new ArrayList<>();
    }
}
