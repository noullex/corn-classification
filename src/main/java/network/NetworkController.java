package network;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.distribution.Distribution;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.zoo.PretrainedType;
import org.deeplearning4j.zoo.ZooModel;
import org.deeplearning4j.zoo.model.VGG16;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static utils.Constants.*;

public class NetworkController {

    private static final Logger log = LoggerFactory.getLogger(NetworkController.class);

    public void trainNetwork() throws IOException, InterruptedException {
        log.info("Loading VGG16 model");
        ZooModel zooModel = new VGG16();
        ComputationGraph vgg16 = (ComputationGraph) zooModel.initPretrained(PretrainedType.IMAGENET);
        log.info(vgg16.summary());

        FineTuneConfiguration fineTuneConf = new FineTuneConfiguration.Builder()
                .learningRate(5e-5)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(Updater.NESTEROVS)
                .seed(SEED)
                .build();

        Distribution distribution = new NormalDistribution(0, 0.2 * (2.0 / (4096 + NUMBER_OF_CLASSES)));
        ComputationGraph vgg16Transfer = new TransferLearning.GraphBuilder(vgg16)
                .fineTuneConfiguration(fineTuneConf)
                .setFeatureExtractor("fc2")
                .removeVertexKeepConnections("predictions")
                .addLayer("predictions",
                        new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                                .nIn(4096).nOut(NUMBER_OF_CLASSES)
                                .weightInit(WeightInit.DISTRIBUTION)
                                .dist(distribution)
                                .activation(Activation.SOFTMAX).build(),
                        "fc2")
                .build();
        log.info(vgg16Transfer.summary());

        CornDataSetIterator.setup();
        DataSetIterator trainIter = CornDataSetIterator.trainIterator();
        DataSetIterator testIter = CornDataSetIterator.testIterator();

        UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new InMemoryStatsStorage();
        uiServer.attach(statsStorage);
        vgg16Transfer.setListeners(new StatsListener(statsStorage));

        Evaluation eval;
        for (int epoch = 0; epoch < NUMBER_OF_EPOCHS; epoch++) {
            int iter = 0;
            while (trainIter.hasNext()) {
                log.info("Iteration #" + (iter + 1));
                DataSet trained = trainIter.next();
                vgg16Transfer.fit(trained);
                if (iter % 10 == 0) {
                    log.info("Evaluate model at iter " + iter + " ....");
                    eval = vgg16Transfer.evaluate(testIter);
                    log.info(eval.stats());
                    testIter.reset();
                }
                iter++;
            }
            trainIter.reset();
            log.info("Epoch #" + (epoch + 1) + " complete");
        }
        log.info("Model build complete");

        File locationToSave = new File("trained-model.zip");
        ModelSerializer.writeModel(vgg16Transfer, locationToSave, true);
    }

    public void testNetwork() throws IOException {
        ComputationGraph network = ModelSerializer.restoreComputationGraph(TRAINED_MODEL);
        log.info("Going to test single image");
        NativeImageLoader loader = new NativeImageLoader(HEIGHT, WIDTH, CHANNELS);

        File imageFile = new File("C:/Users/Ann/Desktop/diploma/corn-classification/extracted-corns/barley/barley-1-1.png");
        INDArray image = loader.asMatrix(imageFile);
        INDArray output = network.outputSingle(image);

        INDArray[] lab = network.getLabelMaskArrays();

        List<String> labels = new ArrayList<>();
        labels.add("barley");
        labels.add("buckwheat");
        labels.add("rice");
        log.info("\n\nPredictions:");
        for( int i=0; i<labels.size(); i++ ){
            log.info("P(" + labels.get(i) + ") = " + output.getDouble(i));
        }
    }
}
