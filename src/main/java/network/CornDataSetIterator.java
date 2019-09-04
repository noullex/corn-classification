package network;

import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.VGG16ImagePreProcessor;
import utils.Utils;
import utils.imagePreprocessing.CornExtractor;
import utils.Corn;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.datavec.image.loader.BaseImageLoader.ALLOWED_FORMATS;
import static utils.Constants.*;

public class CornDataSetIterator {

    private static InputSplit trainData, testData;
    private static ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();

    public static DataSetIterator trainIterator() throws IOException {
        return makeIterator(trainData);
    }

    public static DataSetIterator testIterator() throws IOException {
        return makeIterator(testData);
    }

    public static void setup() throws IOException, InterruptedException {
        preprocessData();

        Random rng = new Random(10);
        File parentDir = new File(EXTRACTED_DATA_FOLDER);
        FileSplit filesInDir = new FileSplit(parentDir, ALLOWED_FORMATS, rng);
        BalancedPathFilter pathFilter = new BalancedPathFilter(rng, ALLOWED_FORMATS, labelMaker);
        InputSplit[] filesInDirSplit = filesInDir.sample(pathFilter, TRAIN_PERCENT, 100 - TRAIN_PERCENT);
        trainData = filesInDirSplit[0];
        testData = filesInDirSplit[1];
    }

    private static void preprocessData() throws IOException, InterruptedException {
        File backgroundFile = Utils.getFileFromResources(BACKGROUND_FOLDER + BACKGROUND_IMAGE);
        BufferedImage background = ImageIO.read(backgroundFile);

        List<String> types = Arrays.stream(Objects.requireNonNull(Utils.getFileFromResources(DATA_FOLDER).listFiles(File::isDirectory)))
                .map(File::getName).collect(Collectors.toList());
        for (String type : types) {
            File dataFolder = Utils.getFileFromResources(DATA_FOLDER + type);
            File[] listOfFiles = dataFolder.listFiles();
            if (listOfFiles == null || listOfFiles.length == 0) {
                throw new IllegalArgumentException(String.format("There are no images for type %s", type));
            }
            int fileNumber = 0;
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    BufferedImage image = ImageIO.read(file);
                    Map<Corn, BufferedImage> corns = CornExtractor.extract(image, background);
                    String folder = EXTRACTED_DATA_FOLDER + type;
                    String baseFile = type + "-" + ++fileNumber;
                    Utils.saveListImages(corns.values(), folder, baseFile);
                }
            }
        }
    }

    private static DataSetIterator makeIterator(InputSplit sample) throws IOException {
        ImageRecordReader recordReader = new ImageRecordReader(HEIGHT, WIDTH, CHANNELS, labelMaker);
        recordReader.initialize(sample);
        DataSetIterator iter = new RecordReaderDataSetIterator(recordReader, BATCH_SIZE, 1, NUMBER_OF_CLASSES);
        iter.setPreProcessor(new VGG16ImagePreProcessor());
        return iter;
    }
}
