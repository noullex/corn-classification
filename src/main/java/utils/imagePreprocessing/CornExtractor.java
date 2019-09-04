package utils.imagePreprocessing;

import sun.misc.Queue;
import utils.Corn;

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
        for (Corn corn : corns) {
            BufferedImage cornImage = new BufferedImage(
                    corn.getMaxX() - corn.getMinX() + 1, corn.getMaxY() - corn.getMinY() + 1, TYPE_INT_RGB);
            for (Point pixel : corn.points) {
                cornImage.setRGB(pixel.x - corn.getMinX(), pixel.y - corn.getMinY(), image.getRGB(pixel.x, pixel.y));
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
        for (int y = 0; y < binaryImage.length; y++) {
            for (int x = 0; x < binaryImage[0].length; x++) {
                if (binaryImage[y][x] == 1) {
                    binaryImage[y][x] = curLabel;
                    Corn corn = new Corn();
                    corn.setMinX(x);
                    corn.setMaxX(x);
                    corn.setMinY(y);
                    corn.setMaxY(y);
                    curPoints.enqueue(new Point(x, y));
                    while (curPoints.elements().hasMoreElements()) {
                        Point p = curPoints.dequeue();
                        corn.points.add(p);
                        if (p.x > corn.getMaxX()) corn.setMaxX(p.x);
                        if (p.x < corn.getMinX()) corn.setMinX(p.x);
                        if (p.y > corn.getMaxY()) corn.setMaxY(p.y);
                        if (p.y < corn.getMinY()) corn.setMinY(p.y);
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dy = -1; dy <= 1; dy++) {
                                int nx = p.x + dx;
                                int ny = p.y + dy;
                                if (nx >= 0 && ny >= 0 && nx < binaryImage[0].length && ny < binaryImage.length
                                        && binaryImage[ny][nx] == 1 && dy != dx && -dy != dx) {
                                    binaryImage[ny][nx] = curLabel;
                                    curPoints.enqueue(new Point(nx, ny));
                                }
                            }
                        }
                    }
                    if ((corn.getMaxY() - corn.getMinY()) > 2 && (corn.getMaxX() - corn.getMinX()) > 2) {
                        result.add(corn);
                    }
                }
            }
        }
        return result;
    }
}
