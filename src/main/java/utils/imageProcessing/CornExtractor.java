package utils.imageProcessing;

import sun.misc.Queue;
import utils.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static utils.Constants.*;

public class CornExtractor {

    public void extractCorns() throws IOException, InterruptedException {
        File backgroundFile = Utils.getFileFromResources(BACKGROUND_FOLDER + BACKGROUND_IMAGE);
        BufferedImage background = ImageIO.read(backgroundFile);

        List<String> types = Arrays.stream(Objects.requireNonNull(Utils.getFileFromResources(DATA_FOLDER).listFiles(File::isDirectory)))
                .map(File::getName).collect(Collectors.toList());
        for (String type : types) {
            File folder = Utils.getFileFromResources(DATA_FOLDER + type);
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles == null || listOfFiles.length == 0) {
                throw new IllegalArgumentException(String.format("There are no images for type %s", type));
            }
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    BufferedImage image = ImageIO.read(file);
                    List<Corn> corns = getCornsFromImage(image, background);
                    saveCornsImages(image, type, corns);
                }
            }
        }
    }

    private List<Corn> getCornsFromImage(BufferedImage image, BufferedImage background) throws InterruptedException {
        byte[][] binaryImage = Binarizer.binarize(Binarizer.subtract(background, image));
        Queue<Point> curPoints = new Queue<Point>();
        List<Corn> result = new ArrayList<Corn>();
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


    private void saveCornsImages(BufferedImage originalImage, String type, List<Corn> corns) throws IOException {
        File folder = new File(EXTRACTED_DATA_FOLDER + type);
        folder.mkdirs();
        for (int i = 0; i < corns.size(); i++) {
            Corn corn = corns.get(i);
            BufferedImage cornImage = new BufferedImage(corn.maxX - corn.minX + 1, corn.maxY - corn.minY + 1, TYPE_INT_RGB);
            for (Point pixel : corn.points) {
                cornImage.setRGB(pixel.x - corn.minX, pixel.y - corn.minY, originalImage.getRGB(pixel.x, pixel.y));
            }
            ImageIO.write(cornImage, "png", new File(EXTRACTED_DATA_FOLDER + type, type + "-" + i + ".png"));
        }
    }

    public class Corn {
        int minX, minY, maxX, maxY;
        List<Point> points = new ArrayList<Point>();
    }
}
