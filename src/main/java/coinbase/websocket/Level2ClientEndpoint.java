package coinbase.websocket;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import coinbase.Level2OrderBook;
import coinbase.websocket.encoder.Channel;
import coinbase.websocket.encoder.Subscribe;
import coinbase.websocket.decoder.L2Message;
import coinbase.websocket.decoder.L2Snapshot;
import coinbase.websocket.decoder.L2Update;

public class Level2ClientEndpoint extends Endpoint {

    private Logger logger = LoggerFactory.getLogger(Level2ClientEndpoint.class);
    private Level2OrderBook orderBook;
    private int displayLevels;
    private Session session;
    private final AutoCloseable closeTheApp;

    /**
     * Tyrus JSR-356 client end point implementation.
     * 
     * @param closeTheApp Callback to notify application of an error.
     */
    public Level2ClientEndpoint(AutoCloseable closeTheApp) {
        logger.info("Created");
        this.closeTheApp = closeTheApp;
    }

    /**
     * It would probably be better to make the application manage the OrderBook.
     **/
    private void closeTheApp() {
        try {
            closeTheApp.close();
        } catch (Exception closeException) {
            logger.error("Failed to close.", closeException);
        }
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
                logger.error(String.format("%s[%s] Update for wrong product: %s", session.getId(), l2Message.productId,
                        l2Message)); // Hasn't ever happened.
            }
        } else {
            logger.info("Ignoring: " + decoded);
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        Level2ClientEndpointConfig level2Config = (Level2ClientEndpointConfig) config;
        this.orderBook = new Level2OrderBook(level2Config.productId(), level2Config.retainedLevels());
        this.displayLevels = level2Config.displayLevels();
        addWholeMessageHandler(session, this::onMessage);
        String[] productIds = new String[] { level2Config.productId() };
        String jsonSubscribe = new Gson()
                .toJson(new Subscribe(productIds, new Channel[] { new Channel("level2", null) }));
        logger.info(String.format("%s[%s]: onOpen subscribing with:\n%s", session.getId(), level2Config.productId(),
                jsonSubscribe));
        try {
            session.getBasicRemote().sendText(jsonSubscribe);
        } catch (IOException e) {
            throw new RuntimeException(e); // this will be propagated to onError callback.
        }
    }

    @Override
    public void onError(Session session, Throwable throwable) {
        logger.error(session.getId(), throwable);
        closeTheApp();
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        logger.info(String.format("%s: Session closing because of %s", session.getId(), closeReason));
        closeTheApp();
    }

}
