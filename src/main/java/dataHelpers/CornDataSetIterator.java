package dataHelpers;

import org.datavec.api.io.filters.BalancedPathFilter;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.VGG16ImagePreProcessor;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static org.datavec.image.loader.BaseImageLoader.ALLOWED_FORMATS;
import static utils.Constants.*;

public class CornDataSetIterator {

    private InputSplit trainData, testData;
    private int batchSize;

    private static ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();

    public CornDataSetIterator(int batchSize) {
        this.batchSize = batchSize;
    }

    public DataSetIterator getTrainIterator() throws IOException {
        return makeIterator(trainData);
    }

    public DataSetIterator getTestIterator() throws IOException {
        return makeIterator(testData);
    }

    public void setup(int trainPercent) {
        Random rng = new Random(10);
        File parentDir = new File(EXTRACTED_DATA_FOLDER);
        FileSplit filesInDir = new FileSplit(parentDir, ALLOWED_FORMATS, rng);
        BalancedPathFilter pathFilter = new BalancedPathFilter(rng, ALLOWED_FORMATS, labelMaker);
        if (trainPercent >= 100) {
            throw new IllegalArgumentException("Percentage of data set aside for training has to be less than 100%");
        }
        InputSplit[] filesInDirSplit = filesInDir.sample(pathFilter, trainPercent, 100 - trainPercent);
        trainData = filesInDirSplit[0];
        testData = filesInDirSplit[1];
    }

    private DataSetIterator makeIterator(InputSplit split) throws IOException {
        ImageRecordReader recordReader = new ImageRecordReader(HEIGHT, WIDTH, CHANNELS, labelMaker);
        recordReader.initialize(split);
        DataSetIterator iter = new RecordReaderDataSetIterator(recordReader, batchSize, 1, NUMBER_OF_CLASSES);
        iter.setPreProcessor(new VGG16ImagePreProcessor());
        return iter;
    }
}
