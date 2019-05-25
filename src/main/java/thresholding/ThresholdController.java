package thresholding;

import utils.Utils;
import utils.imagePreprocessing.Binarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static utils.Constants.*;

public class ThresholdController {

    public void binarize() throws IOException {
        BufferedImage image = ImageIO.read(Utils.getFileFromResources(TEST_DATA_FOLDER + "buckwheat_and_barley_1_2.bmp"));
        BufferedImage background = ImageIO.read(Utils.getFileFromResources(BACKGROUND_FOLDER + BACKGROUND_IMAGE));
        byte[][] binary = Binarizer.binarize(Binarizer.subtract(background, image));
    }
}
