package network;

import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.VGG16ImagePreProcessor;
import utils.imagePreprocessing.CornExtractor;

import java.io.File;
import java.io.IOException;
import java.util.Random;

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
        CornExtractor.extractCorns();
        Random rng = new Random(10);
        File parentDir = new File(EXTRACTED_DATA_FOLDER);
        FileSplit filesInDir = new FileSplit(parentDir, ALLOWED_FORMATS, rng);
        BalancedPathFilter pathFilter = new BalancedPathFilter(rng, ALLOWED_FORMATS, labelMaker);
        InputSplit[] filesInDirSplit = filesInDir.sample(pathFilter, TRAIN_PERCENT, 100 - TRAIN_PERCENT);
        trainData = filesInDirSplit[0];
        testData = filesInDirSplit[1];
    }

    private static DataSetIterator makeIterator(InputSplit sample) throws IOException {
        ImageRecordReader recordReader = new ImageRecordReader(HEIGHT, WIDTH, CHANNELS, labelMaker);
        recordReader.initialize(sample);
        DataSetIterator iter = new RecordReaderDataSetIterator(recordReader, BATCH_SIZE, 1, NUMBER_OF_CLASSES);
        iter.setPreProcessor(new VGG16ImagePreProcessor());
        return iter;
    }
}