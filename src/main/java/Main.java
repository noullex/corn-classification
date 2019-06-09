import network.NetworkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thresholding.ThresholdController;
import utils.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static utils.Constants.*;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(NetworkController.class);

    public static void main(String[] args) {
        log.info("Start corns classification");
        try {
            File imageFile = Utils.getFileFromResources(DATA_FOLDER + "rice/" + "rice_1_1.bmp");
            BufferedImage image = ImageIO.read(imageFile);
            File backgroundFile = Utils.getFileFromResources(BACKGROUND_FOLDER + BACKGROUND_IMAGE);
            BufferedImage background = ImageIO.read(backgroundFile);
            runNetwork(image, background);
//            runThresholding(image, background);
        } catch (Exception exception) {
            log.error("Unexpectedly shutdown", exception);
            System.exit(-1);
        }
    }

    private static void runNetwork(BufferedImage image, BufferedImage background) throws IOException, InterruptedException {
        NetworkController networkController = new NetworkController();
//        runNetworkInTrainMode(networkController);
        runNetworkInTestMode(networkController, image, background);
    }

    private static void runNetworkInTrainMode(NetworkController networkController) throws IOException, InterruptedException {
        networkController.trainNetwork();
    }

    private static void runNetworkInTestMode(NetworkController networkController, BufferedImage image, BufferedImage background) throws IOException, InterruptedException {
        networkController.testNetwork(image, background);
    }

    private static void runThresholding(BufferedImage image, BufferedImage background) {
        ThresholdController thresholdController = new ThresholdController();
        List<Point> defectiveCorns = thresholdController.getDefectiveCorns(image, background);
//        for (Point defectiveCorn : defectiveCorns) {
//            Color originalColor = new Color(image.getRGB(defectiveCorn.x, defectiveCorn.y));
//            Color newColor = new Color(originalColor.getRed(), 0, 0);
//            for (int y = defectiveCorn.y - 10; y <= defectiveCorn.y + 10; y++) {
//                for (int x = defectiveCorn.x - 10; x <= defectiveCorn.x + 10; y++) {
//                    image.setRGB(x, y, newColor.getRGB());
//                }
//            }
//        }
    }
}
