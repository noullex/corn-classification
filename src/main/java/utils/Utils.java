package utils;

import utils.Constants.CornType;
import utils.imagePreprocessing.CornExtractor.Corn;

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
        ImageIO.write(image, "png", new File("result.png"));
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

    private static void paintCorn(BufferedImage image, Corn corn, int x, int y, int z) {
        for (Point pixel : corn.points) {
            Color originalColor = new Color(image.getRGB(pixel.x, pixel.y));
            Color newColor = new Color(x * originalColor.getRed(), y * originalColor.getGreen(), z * originalColor.getBlue());
            image.setRGB(pixel.x, pixel.y, newColor.getRGB());
        }
    }
}
