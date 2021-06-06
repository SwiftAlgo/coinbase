package coinbase;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coinbase.exchange.security.Signature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@ClientEndpoint
public class OrderBookAccess {
	
	private static Logger logger = LoggerFactory.getLogger(OrderBookAccess.class);
	
	private static Gson gson = new GsonBuilder().create();
	
	private static Signature signer = new Signature("Y1hExBJXe3L2pF1f44MW+GRkzD1ZQQ3bYOLVIQOv3jXiXXgt+jEgCAmsthxp9oHwGkCNtQs2RbVBbDLIfIduKw==");

	public OrderBookAccess() {
		logger.info("Created");
	}
	
	@OnOpen
	public void onOpen(Session session) {
        String[] product_ids = new String[]{"BTC-USD", "ETH-USD"};
		String jsonSubscribe = gson.toJson(signObject(new Subscribe(product_ids, new Channel[] {new Channel("full", product_ids)})));
		System.out.println(jsonSubscribe);
		logger.info(String.format("%s: onOpen subscribing with:\n%s", session.getId(), jsonSubscribe));
		try {
        session.getBasicRemote().sendText(jsonSubscribe);
		}
		catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
    @OnMessage
    public void onMessage(String message, Session session) {
      logger.info(String.format("%s: onMessage: %s", session.getId(), message));

    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error(session.getId(), throwable);
    }

	
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
      logger.info(String.format("Session %s close because of %s", session.getId(), closeReason));
    }
    
    public String signObject(Subscribe jsonObj) {
    	return gson.toJson(jsonObj);
    	/*
        String jsonString = gson.toJson(jsonObj);

        String timestamp = Instant.now().getEpochSecond() + "";
        String passphrase = "g3q4agjvcwp";
        String key = "98f73a6708209dbddcd32cefc9444b4c";
        String signature = signer.generate("", "GET", jsonString, timestamp);

        
        jsonObj.addSignature(signature, passphrase, timestamp, key);
        return gson.toJson(jsonObj);*/
    }
    
    public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException {
    	logger.info("Starting");
        CountDownLatch latch = new CountDownLatch(1);
        ClientManager client = ClientManager.createClient();
        try {
        	// new URI("ws://echo.websocket.org"))
            client.connectToServer(OrderBookAccess.class, new URI("wss://ws-feed-public.sandbox.pro.coinbase.com")); //new URI("wss://ws-feed.pro.coinbase.com:443"));//
            latch.await();
        } catch (DeploymentException | URISyntaxException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    

}
