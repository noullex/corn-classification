package utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Utils {

    public static File getFileFromResources(String fileName) {
        ClassLoader classLoader = Utils.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("File is not found!");
        } else {
            return new File(resource.getFile());
        }
    }

    public static void saveListImages(Collection<BufferedImage> images, String folder, String baseFile) throws IOException {
        File file = new File(folder);
        file.mkdirs();
        int i = 0;
        for (BufferedImage image : images) {
            ImageIO.write(image, "png", new File(folder, baseFile + "-" + ++i + ".png"));
        }
    }

    public static void drawPredictions(BufferedImage image, Map<CornType, List<Corn>> predictions) throws IOException {
        for (Entry<CornType, List<Corn>> entry : predictions.entrySet()) {
            CornType type = entry.getKey();
            List<Corn> corns = entry.getValue();
            switch (type) {
                case BARLEY:
                    corns.forEach(corn -> paintCorn(image, corn, 1, 0, 0));
                    break;
                case BUCKWHEAT:
                    corns.forEach(corn -> paintCorn(image, corn, 0, 1, 0));
                    break;
                case RICE:
                    corns.forEach(corn -> paintCorn(image, corn, 0, 0, 1));
                    break;
            }
        }
        ImageIO.write(image, "png", new File("prediction-result.png"));
    }

    public static void drawDefectiveCorns(BufferedImage image, List<Point> defectiveCorns) throws IOException {
        int i = 0;
        for (Point defectiveCorn : defectiveCorns) {
            for (int y = defectiveCorn.y - 2; y <= defectiveCorn.y + 2; y++) {
                for (int x = defectiveCorn.x - 2; x <= defectiveCorn.x + 2; x++) {
                    if (x > 0 && y > 0 && x < image.getWidth() && y < image.getHeight()) {
                        image.setRGB(x, y, Color.RED.getRGB());
                    }
                }
            }
        }
        ImageIO.write(image, "png", new File("separation-result.png"));
    }

    public static String getPredictionsStats(Map<CornType, List<Corn>> predictions) {
        StringBuilder builder = new StringBuilder("\n======================================================\n");
        builder.append("Statistics:\n");
        for (Entry<CornType, List<Corn>> entry : predictions.entrySet()) {
            builder.append("For type")
                    .append(" ")
                    .append(entry.getKey().name())
                    .append(" ")
                    .append("predicted count is")
                    .append(" ")
                    .append(entry.getValue().size())
                    .append("\n");
        }
        return builder.toString();
    }

    public static String getSeparationStats(List<Point> defectiveCorns) {
        StringBuilder builder = new StringBuilder("\n======================================================\n");
        builder.append("Statistics:\n")
                .append("Count of defective corns is")
                .append(" ")
                .append(defectiveCorns.size());
        return builder.toString();
    }

    private static void paintCorn(BufferedImage image, Corn corn, int x, int y, int z) {
        for (Point pixel : corn.points) {
            Color originalColor = new Color(image.getRGB(pixel.x, pixel.y));
            Color newColor = new Color(x * originalColor.getRed(), y * originalColor.getGreen(), z * originalColor.getBlue());
            image.setRGB(pixel.x, pixel.y, newColor.getRGB());
        }
    }

    public static ClassThresholds getThresholdsFromCorns(List<Corn> corns) {
        if (corns.isEmpty()) {
            throw new IllegalArgumentException("There are no corns of specified type");
        }
        ClassThresholds classThresholds = new ClassThresholds();
        classThresholds.init(corns.get(0));
        int square, width, height;
        for (Corn corn : corns) {
            square = corn.getPoints().size();
            width = corn.getMaxX() - corn.getMinX();
            height = corn.getMaxY() - corn.getMinY();
            if (square > classThresholds.getSquareMax()) classThresholds.setSquareMax(square);
            if (square < classThresholds.getSquareMin()) classThresholds.setSquareMin(square);
            if (height > classThresholds.getHeightMax()) classThresholds.setHeightMax(height);
            if (height < classThresholds.getHeightMin()) classThresholds.setHeightMin(height);
            if (width > classThresholds.getWidthMax()) classThresholds.setWidthMax(square);
            if (width < classThresholds.getWidthMin()) classThresholds.setWidthMin(square);
        }
        return classThresholds;
    }
}
