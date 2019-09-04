import network.NetworkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thresholding.ThresholdController;
import utils.ClassThresholds;
import utils.Corn;
import utils.CornType;
import utils.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SeparatorExecutor {
    private static final Logger log = LoggerFactory.getLogger(SeparatorExecutor.class);
    private static NetworkController networkController = new NetworkController();

    public void start() {
        log.info("Start corns classification");
        try {
            CornType classType = CornType.BUCKWHEAT;
            File imageFile = Utils.getFileFromResources("images/corns/buckwheat/buckwheat_1_1.bmp");
            BufferedImage image = ImageIO.read(imageFile);
            File testImageFile = Utils.getFileFromResources("images/corns/rice/rice_1_1.bmp");
            BufferedImage testImage = ImageIO.read(testImageFile);
            File backgroundFile = Utils.getFileFromResources("images/background/background.bmp");
            BufferedImage background = ImageIO.read(backgroundFile);
            Map<CornType, List<Corn>> extractedMap = runNetworkTest(image, background);
            ClassThresholds classThresholds = Utils.getThresholdsFromCorns(extractedMap.get(classType));
            runThresholding(classThresholds, testImage, background);
        } catch (Exception exception) {
            log.error("Unexpectedly shutdown", exception);
            System.exit(-1);
        }
    }

    private void runNetworkTrain(BufferedImage image, BufferedImage background) throws IOException, InterruptedException {
        networkController.trainNetwork();
    }

    private Map<CornType, List<Corn>> runNetworkTest(BufferedImage image, BufferedImage background) throws IOException, InterruptedException {
        return networkController.testNetwork(image, background);
    }

    private void runThresholding(ClassThresholds classThresholds, BufferedImage image, BufferedImage background) throws IOException {
        ThresholdController thresholdController = new ThresholdController();
        thresholdController.separate(classThresholds, image, background);
    }
}
