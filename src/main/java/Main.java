import network.NetworkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(NetworkController.class);

    public static void main(String[] args) {
        log.info("Start corns classification");
        try {
            NetworkController networkController = new NetworkController();
//            networkController.trainNetwork();
            networkController.testNetwork();
        } catch (Exception exception) {
            log.error("Unexpectedly shutdown", exception);
            System.exit(-1);
        }
    }
}
