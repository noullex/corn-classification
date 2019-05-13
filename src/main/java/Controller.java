import dataHelpers.CornDataSetIteratorFeaturized;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.nn.transferlearning.TransferLearningHelper;
import org.deeplearning4j.zoo.ZooModel;
import org.deeplearning4j.zoo.model.VGG16;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static utils.Constants.*;

public class Controller {

    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    public void trainNetwork() throws IOException, InterruptedException {
        log.info("Loading VGG16 model");
        ZooModel zooModel = VGG16.builder().build();
        ComputationGraph vgg16 = (ComputationGraph) zooModel.initPretrained();
        log.info(vgg16.summary());

        FineTuneConfiguration fineTuneConf = new FineTuneConfiguration.Builder()
                .updater(new Nesterovs(3e-5, 0.9))
                .seed(SEED)
                .build();

        Distribution distribution = new NormalDistribution(0, 0.2 * (2.0 / (4096 + NUMBER_OF_CLASSES)));
        ComputationGraph vgg16Transfer = new TransferLearning.GraphBuilder(vgg16)
                .fineTuneConfiguration(fineTuneConf)
                .setFeatureExtractor(FEATURE_EXTRACTION_LAYER)
                .removeVertexKeepConnections("predictions")
                .addLayer("predictions",
                        new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                                .nIn(4096).nOut(NUMBER_OF_CLASSES)
                                .weightInit(distribution)
                                .dist(distribution)
                                .activation(Activation.SOFTMAX).build(),
                        FEATURE_EXTRACTION_LAYER)
                .build();
        log.info(vgg16Transfer.summary());

        DataSetIterator trainIter = CornDataSetIteratorFeaturized.trainIterator();
        DataSetIterator testIter = CornDataSetIteratorFeaturized.testIterator();

        TransferLearningHelper transferLearningHelper = new TransferLearningHelper(vgg16Transfer);
        log.info(transferLearningHelper.unfrozenGraph().summary());

        for (int epoch = 0; epoch < NUMBER_OF_EPOCHS; epoch++) {
            if (epoch == 0) {
                Evaluation eval = transferLearningHelper.unfrozenGraph().evaluate(testIter);
                log.info("Eval stats BEFORE fit.....");
                log.info(eval.stats() + "\n");
                testIter.reset();
            }
            int iter = 0;
            while (trainIter.hasNext()) {
                transferLearningHelper.fitFeaturized(trainIter.next());
                if (iter % 10 == 0) {
                    log.info("Evaluate model at iter " + iter + " ....");
                    Evaluation eval = transferLearningHelper.unfrozenGraph().evaluate(testIter);
                    log.info(eval.stats());
                    testIter.reset();
                }
                iter++;
            }
            trainIter.reset();
            log.info("Epoch #" + epoch + " complete");
        }
        log.info("Model build complete");
    }
}
