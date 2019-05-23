import network.NetworkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Utils;

import javax.imageio.ImageIO;
import javax.rmi.CORBA.Util;
import java.awt.image.BufferedImage;
import java.io.File;

import static utils.Constants.*;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(NetworkController.class);

    public static void main(String[] args) {
        log.info("Start corns classification");
        try {
            NetworkController networkController = new NetworkController();
//            networkController.trainNetwork();
            File imageFile = Utils.getFileFromResources(TEST_DATA_FOLDER + "buckwheat_and_barley_1_2.bmp");
            BufferedImage image = ImageIO.read(imageFile);
            File backgroundFile = Utils.getFileFromResources(BACKGROUND_FOLDER + BACKGROUND_IMAGE);
            BufferedImage background = ImageIO.read(backgroundFile);
            networkController.testNetwork(image, background);
        } catch (Exception exception) {
            log.error("Unexpectedly shutdown", exception);
            System.exit(-1);
        }
    }
}
