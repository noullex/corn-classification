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

import java.io.File;
import java.io.IOException;

public class CornDataSetIteratorFeaturized {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CornDataSetIteratorFeaturized.class);

    private static final int trainPerc = 75;
    private static final int batchSize = 3;
    private static String featureExtractorLayer = "fc2";

    public static DataSetIterator trainIterator() throws IOException {
        runFeaturize();
        DataSetIterator existingTrainingData = new ExistingMiniBatchDataSetIterator(new File("trainFolder"), "corns-" + featureExtractorLayer + "-train-%d.bin");
        DataSetIterator asyncTrainIter = new AsyncDataSetIterator(existingTrainingData);
        return asyncTrainIter;
    }

    public static DataSetIterator testIterator() {
        DataSetIterator existingTestData = new ExistingMiniBatchDataSetIterator(new File("testFolder"), "corns-" + featureExtractorLayer + "-test-%d.bin");
        DataSetIterator asyncTestIter = new AsyncDataSetIterator(existingTestData);
        return asyncTestIter;
    }

    private static void runFeaturize() throws IOException {
        File trainDir = new File("trainFolder", "corns-" + featureExtractorLayer + "-train-0.bin");
        if (!trainDir.isFile()) {
            log.info("\n\tFEATURIZED DATA NOT FOUND. \n\t\tRUNNING \"FeaturizedPreSave\" first to do presave of featurized data");
            featurizePreSave();
        }
    }

    private static void featurizePreSave() throws IOException {
        //import org.deeplearning4j.transferlearning.vgg16 and print summary
        log.info("\n\nLoading org.deeplearning4j.transferlearning.vgg16...\n\n");
        ZooModel zooModel = VGG16.builder().build();
        ComputationGraph vgg16 = (ComputationGraph) zooModel.initPretrained();
        log.info(vgg16.summary());

        TransferLearningHelper transferLearningHelper = new TransferLearningHelper(vgg16, featureExtractorLayer);
        log.info(vgg16.summary());
        CornDataSetIterator cornDataSetIterator = new CornDataSetIterator();
        cornDataSetIterator.setup(batchSize, trainPerc);
        DataSetIterator trainIter = cornDataSetIterator.trainIterator();
        DataSetIterator testIter = cornDataSetIterator.testIterator();


        int trainDataSaved = 0;
        while (trainIter.hasNext()) {
            DataSet currentFeaturized = transferLearningHelper.featurize(trainIter.next());
            saveToDisk(currentFeaturized, trainDataSaved, true);
            trainDataSaved++;
        }

        int testDataSaved = 0;
        while (testIter.hasNext()) {
            DataSet currentFeaturized = transferLearningHelper.featurize(testIter.next());
            saveToDisk(currentFeaturized, testDataSaved, false);
            testDataSaved++;
        }

        log.info("Finished pre saving featurized test and train data");
    }

    public static void saveToDisk(DataSet currentFeaturized, int iterNum, boolean isTrain) {
        File fileFolder = isTrain ? new File("trainFolder") : new File("testFolder");
        if (iterNum == 0) {
            fileFolder.mkdirs();
        }
        String fileName = "corns-" + featureExtractorLayer + "-";
        fileName += isTrain ? "train-" : "test-";
        fileName += iterNum + ".bin";
        currentFeaturized.save(new File(fileFolder, fileName));
        log.info("Saved " + (isTrain ? "train " : "test ") + "dataset #" + iterNum);
    }
}
