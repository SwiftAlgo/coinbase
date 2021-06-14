package coinbase;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import javax.websocket.DeploymentException;

import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coinbase.websocket.Level2ClientEndpoint;
import coinbase.websocket.Level2ClientEndpointConfig;

public class Level2OrderBookConsole implements AutoCloseable {

    private final CountDownLatch latch = new CountDownLatch(1);
    private final ClientManager client = ClientManager.createClient();
    private final Level2ClientEndpointConfig config;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Level2OrderBookConsole(String productId, int displayedLevels, int retainedLevels) {
        config = new Level2ClientEndpointConfig(productId, 10, 100);
    }

    public void start() {
        logger.info(String.format("Starting %s Level2 displaying %d of max %d levels", config.productId(),
                config.displayLevels(), config.retainedLevels()));
        try {
            client.connectToServer(new Level2ClientEndpoint(this), config, new URI("wss://ws-feed.pro.coinbase.com"));
            latch.await();
        } catch (DeploymentException | URISyntaxException | InterruptedException | IOException e) {
            logger.error(e.getMessage(), e);
            close();
        }
    }

    @Override
    public void close() {
        if (latch.getCount() > 0) {
            logger.info("Shutting down.");
            client.shutdown(); // Shutdown the Tyrus container threads.
            latch.countDown(); // Allow the main thread to terminate.
        }
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            final String productId = args[0];
            Level2OrderBookConsole app = new Level2OrderBookConsole(productId, 10, 100);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                app.close();
            }));
            app.start();
        } else {
            System.out.println(
                    String.format("Usage:\n  java %s <product_id>\nwhere <product_id> is the symbol eg. BTC-USD",
                            Level2ClientEndpoint.class));
            System.exit(1);
        }

    }

}
