package utils;

public final class Constants {
    public static final String BACKGROUND_FOLDER = "images/background/";
    public static final String BACKGROUND_IMAGE = "background.bmp";
    public static final String DATA_FOLDER = "images/corns/";
    public static final String EXTRACTED_DATA_FOLDER = "extracted-corns/";
    public static final String TEST_DATA_FOLDER = "images/mixture/";

    public static final String TRAINED_MODEL = "trained-model.zip";

    public static final int NUMBER_OF_CLASSES = 3;
    public static final int NUMBER_OF_EPOCHS = 3;
    public static final int TRAIN_PERCENT = 80;
    public static final int BATCH_SIZE = 10;
    public static final long SEED = 12345;

    public static final int HEIGHT = 224;
    public static final int WIDTH = 224;
    public static final int CHANNELS = 3;

    public enum CornType {
        BARLEY,
        BUCKWHEAT,
        RICE
    }
}