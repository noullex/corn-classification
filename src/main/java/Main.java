import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    public static void main(String[] args) {
        log.info("Start corns classification");
        try {
            Controller controller = new Controller();
            controller.trainNetwork();
        } catch (Exception exception) {
            log.error("Unexpectedly shutdown", exception);
            System.exit(-1);
        }
    }
}
