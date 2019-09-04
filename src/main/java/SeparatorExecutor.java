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

import static utils.Constants.*;

public class SeparatorExecutor {
    private static final Logger log = LoggerFactory.getLogger(NetworkController.class);
    private static NetworkController networkController = new NetworkController();

    public void start() {
        log.info("Start corns classification");
        try {
            CornType classType = CornType.BARLEY;
            File imageFile = Utils.getFileFromResources("images/scale/rice_1.png");
            BufferedImage image = ImageIO.read(imageFile);
            File backgroundFile = Utils.getFileFromResources("images/scale/background.bmp");
            BufferedImage background = ImageIO.read(backgroundFile);
            Map<CornType, List<Corn>> extractedMap = runNetworkTest(image, background);

//            ClassThresholds classThresholds = Utils.getThresholdsFromCorns(extractedMap.get(classType.getValue()));
//            runThresholding(classThresholds, image, background);
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

    private void runThresholding(ClassThresholds classThresholds, BufferedImage image, BufferedImage background) {
        ThresholdController thresholdController = new ThresholdController();
        List<Point> defectiveCorns = thresholdController.getDefectiveCorns(classThresholds, image, background);
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
