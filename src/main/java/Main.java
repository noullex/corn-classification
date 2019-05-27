import network.NetworkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thresholding.ThresholdController;
import utils.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static utils.Constants.*;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(NetworkController.class);

    public static void main(String[] args) {
        log.info("Start corns classification");
        try {
//            runNetwork();
            runThresholding();
        } catch (Exception exception) {
            log.error("Unexpectedly shutdown", exception);
            System.exit(-1);
        }
    }

    private static void runNetwork() throws IOException, InterruptedException {
        NetworkController networkController = new NetworkController();
//        runNetworkInTrainMode(networkController);
        runNetworkInTestMode(networkController);
    }

    private static void runNetworkInTrainMode(NetworkController networkController) throws IOException, InterruptedException {
        networkController.trainNetwork();
    }

    private static void runNetworkInTestMode(NetworkController networkController) throws IOException, InterruptedException {
        File imageFile = Utils.getFileFromResources(TEST_DATA_FOLDER + "buckwheat_and_barley_2_1.bmp");
        BufferedImage image = ImageIO.read(imageFile);
        File backgroundFile = Utils.getFileFromResources(BACKGROUND_FOLDER + BACKGROUND_IMAGE);
        BufferedImage background = ImageIO.read(backgroundFile);
        networkController.testNetwork(image, background);
    }

    private static void runThresholding() throws IOException {
        ThresholdController thresholdController = new ThresholdController();
        thresholdController.sort();
    }
}
