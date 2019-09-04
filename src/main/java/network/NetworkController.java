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
import utils.Corn;
import utils.CornType;
import utils.Utils;
import utils.imagePreprocessing.CornExtractor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static utils.Constants.*;

public class NetworkController {

    private static final Logger log = LoggerFactory.getLogger(NetworkController.class);

    public void trainNetwork() throws IOException, InterruptedException {
        log.info("Get corn dataset");
        CornDataSetIterator.setup();
        DataSetIterator trainIter = CornDataSetIterator.trainIterator();
        DataSetIterator testIter = CornDataSetIterator.testIterator();

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
                    log.info("Evaluate model at iter " + (iter + 1) + " ....");
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

    public Map<CornType, List<Corn>> testNetwork(BufferedImage image, BufferedImage background) throws IOException, InterruptedException {
        log.info("Restore saved model");
        ComputationGraph network = ModelSerializer.restoreComputationGraph(TRAINED_MODEL);

        log.info("Test model");
        Map<Corn, BufferedImage> corns = CornExtractor.extract(image, background);
        Map<CornType, List<Corn>> predictions = new HashMap<>();
        NativeImageLoader loader = new NativeImageLoader(HEIGHT, WIDTH, CHANNELS);
        for (Map.Entry<Corn, BufferedImage> entry : corns.entrySet()) {
            Corn corn = entry.getKey();
            BufferedImage cornImage = entry.getValue();
            INDArray input = loader.asMatrix(cornImage);
            INDArray prediction = network.outputSingle(input);
            List<Double> probabilities = new ArrayList<>();
            for (int i = 0; i < prediction.length(); i++) {
                probabilities.add(prediction.getDouble(i));
            }
            double maxProbability = Collections.max(probabilities);
            int labelPosition = probabilities.indexOf(maxProbability);

            CornType type = CornType.values()[labelPosition];
            if (!predictions.containsKey(type)) {
                predictions.put(type, new ArrayList<>());
            }
            predictions.get(type).add(corn);
        }
        Utils.drawPredictions(image, predictions);
        log.info(Utils.getPredictionsStats(predictions));

        return predictions;
    }
}
