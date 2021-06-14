package coinbase;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;

import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import coinbase.decoder.L2Message;
import coinbase.decoder.L2Snapshot;
import coinbase.decoder.L2Update;
import coinbase.encoder.Channel;

public class Level2ClientEndpoint extends Endpoint {

    private static Logger logger = LoggerFactory.getLogger(Level2ClientEndpoint.class);
    private Level2OrderBook orderBook;
    private int displayLevels;
    private Session session;

    public Level2ClientEndpoint() {
        logger.info("Created");
    }

    /**
     * Utility method allows lambda to be used. The reason for this is the Tyrus API
     * method takes a MessageHandler which is an empty interface whereas
     * MessageHandler.Whole is a functional interface.
     * 
     * @param session
     * @param handler
     */
    private void addWholeMessageHandler(Session session, MessageHandler.Whole<Object> handler) {
        session.addMessageHandler(handler);
    }

    private void onMessage(Object decoded) {
        if (decoded instanceof L2Message) {
            L2Message l2Message = (L2Message) decoded;
            if (l2Message.productId().equals(orderBook.productId())) {
                if (decoded instanceof L2Update) {
                    if (orderBook.onL2Update((L2Update) l2Message)) {
                        // OrderBook has changed.
                        logger.info(orderBook.toString(displayLevels));
                    }
                } else if (decoded instanceof L2Snapshot) {
                    orderBook.onL2Snapshot((L2Snapshot) decoded);
                    logger.info(orderBook.toString(displayLevels));
                }
            } else {
                logger.error(String.format("%s[%s] Update for wrong product: %s", session.getId(), l2Message.productId, l2Message));
            }
        } else {
            logger.info("Ignoring: " + decoded);
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        Level2ClientEndpointConfig level2Config = (Level2ClientEndpointConfig) config;
        this.orderBook = new Level2OrderBook(level2Config.productId(), level2Config.levels());
        this.displayLevels = level2Config.displayLevels();
        addWholeMessageHandler(session, this::onMessage);
        String[] productIds = new String[] { level2Config.productId() };

        String jsonSubscribe = new Gson()
                .toJson(new Subscribe(productIds, new Channel[] { new Channel("level2", null) }));
        logger.info(String.format("%s[%s]: onOpen subscribing with:\n%s", session.getId(), level2Config.productId(), jsonSubscribe));
        try {
            session.getBasicRemote().sendText(jsonSubscribe);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            // TODO graceful shutdown.
        }
    }

    @Override
    public void onError(Session session, Throwable throwable) {
        logger.error(session.getId(), throwable);
        try {
            session.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            // TODO graceful shutdown.
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        logger.info(String.format("Session %s close because of %s", session.getId(), closeReason));
    }

    public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException {
        if (args.length == 1) {
            final String product_id = args[0];
            logger.info(String.format("Starting Level2OrderBook for '%s'", product_id));
            ThreadPoolConfig.defaultConfig()
            .setPoolName("Level2")
            .setCorePoolSize(1)
            .setMaxPoolSize(1);
            CountDownLatch latch = new CountDownLatch(1);
            ClientManager client = ClientManager.createClient();
            int displayedLevels = 10;
            int retainedLevels = displayedLevels;
            try {
                client.connectToServer(new Level2ClientEndpoint(),
                        new Level2ClientEndpointConfig(product_id, displayedLevels, retainedLevels),
                        new URI("wss://ws-feed.pro.coinbase.com"));
                latch.await();
            } catch (DeploymentException | URISyntaxException | InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println(
                    String.format("Usage:\n  java %s <product_id>\nwhere <product_id> is the symbol eg. BTC-USD",
                            Level2ClientEndpoint.class));
            System.exit(1);
        }

    }

}
