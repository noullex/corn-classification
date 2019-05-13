package utils;

import org.nd4j.linalg.dataset.DataSet;

import java.io.File;
import java.net.URL;

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

    public static void saveToDisk(DataSet currentFeaturized, int iterator, boolean isTrain) {
        File fileFolder = isTrain ? new File("trainFolder") : new File("testFolder");
        if (iterator == 0) {
            fileFolder.mkdirs();
        }
        String fileName = "corns-";
        fileName += isTrain ? "train-" : "test-";
        fileName += iterator + ".bin";
        currentFeaturized.save(new File(fileFolder, fileName));
    }
}
