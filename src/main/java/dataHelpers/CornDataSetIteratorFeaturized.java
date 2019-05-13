package dataHelpers;

import org.deeplearning4j.datasets.iterator.AsyncDataSetIterator;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.transferlearning.TransferLearningHelper;
import org.deeplearning4j.zoo.ZooModel;
import org.deeplearning4j.zoo.model.VGG16;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.ExistingMiniBatchDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.slf4j.Logger;
import utils.Utils;
import utils.imageProcessing.CornExtractor;

import java.io.File;
import java.io.IOException;

import static utils.Constants.FEATURE_EXTRACTION_LAYER;

public class CornDataSetIteratorFeaturized {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CornDataSetIteratorFeaturized.class);

    public static DataSetIterator trainIterator() throws IOException, InterruptedException {
        runFeaturize();
        DataSetIterator existingTrainingData = new ExistingMiniBatchDataSetIterator(new File("trainFolder"), "corns-train-%d.bin");
        return new AsyncDataSetIterator(existingTrainingData);
    }

    public static DataSetIterator testIterator() {
        DataSetIterator existingTestData = new ExistingMiniBatchDataSetIterator(new File("testFolder"), "corns-test-%d.bin");
        return new AsyncDataSetIterator(existingTestData);
    }

    private static void runFeaturize() throws IOException, InterruptedException {
        File trainDir = new File("trainFolder");
        if (!trainDir.isFile()) {
            log.info("Featurized data not found");
            featurizePreSave();
        }
    }

    private static void featurizePreSave() throws IOException, InterruptedException {
        log.info("Start feature extracting");

        log.info("Loading VGG16 model");
        ZooModel zooModel = VGG16.builder().build();
        ComputationGraph vgg16 = (ComputationGraph) zooModel.initPretrained();
        log.info(vgg16.summary());

        TransferLearningHelper transferLearningHelper = new TransferLearningHelper(vgg16, FEATURE_EXTRACTION_LAYER);
        log.info(vgg16.summary());

        CornExtractor extractor = new CornExtractor();
        extractor.extractCorns();

        int batchSize = 3;
        int trainPercent = 80;
        CornDataSetIterator cornDataSetIterator = new CornDataSetIterator(batchSize);
        cornDataSetIterator.setup(trainPercent);
        DataSetIterator trainIter = cornDataSetIterator.getTrainIterator();
        DataSetIterator testIter = cornDataSetIterator.getTestIterator();

        int trainDataSaved = 0;
        while (trainIter.hasNext()) {
            DataSet currentFeaturized = transferLearningHelper.featurize(trainIter.next());
            Utils.saveToDisk(currentFeaturized, trainDataSaved, true);
            trainDataSaved++;
        }

        int testDataSaved = 0;
        while (testIter.hasNext()) {
            DataSet currentFeaturized = transferLearningHelper.featurize(testIter.next());
            Utils.saveToDisk(currentFeaturized, testDataSaved, false);
            testDataSaved++;
        }

        log.info("Finished pre saving featurized test and train data");
    }
}