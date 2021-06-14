package coinbase.decoder;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import coinbase.Level2ClientEndpointConfig;

public class Level2Decoder implements Decoder.Text<L2Message> {

    private static Logger logger = LoggerFactory.getLogger(Level2Decoder.class);

    /**
     * Number of levels stored in order book providers upper bound on number of bids / asks to parse.
     */
    private int levels;

    @Override
    public void init(EndpointConfig config) {
        this.levels = ((Level2ClientEndpointConfig) config).levels();
    }

    @Override
    public void destroy() {
    }

    @Override
	public L2Message decode(String s) throws DecodeException {
		logger.info(s);
		JsonObject e = (JsonObject) JsonParser.parseString(s);
		String type = e.get("type").getAsString();
		final boolean isSnapshot = type.equals("snapshot");
		final boolean isUpdate = type.equals("l2update");
		if (!isSnapshot && !isUpdate) {
			throw new IllegalStateException("Unexpected type: " + type);
		}
		final String product_id = e.get("product_id").getAsString();
		L2Message l2Message = isSnapshot ? new L2Snapshot(product_id) : new L2Update(product_id);

		if (isSnapshot) {
			JsonArray asks = e.get("asks").getAsJsonArray();
			int maxAsks = Math.min(asks.size(), levels);
			l2Message.initializeAsks(maxAsks);
			for (int i = 0; i < maxAsks; ++i) {
				JsonArray priceQty = asks.get(i).getAsJsonArray();
				double price = Double.parseDouble(priceQty.get(0).getAsString());
				double qty = Double.parseDouble(priceQty.get(1).getAsString());
				l2Message.addAsk(price, qty);
			}
			JsonArray bids = e.get("bids").getAsJsonArray();
			final int maxBids = Math.min(bids.size(), levels);
			l2Message.initializeBids(maxBids);
			for (int i = 0; i < maxBids; ++i) {
				JsonArray priceQty = bids.get(i).getAsJsonArray();
				double price = Double.parseDouble(priceQty.get(0).getAsString());
				double qty = Double.parseDouble(priceQty.get(1).getAsString());
				l2Message.addBid(price, qty);
			}
		} else {
			// L2Update
			JsonArray changes = e.get("changes").getAsJsonArray();

			for (int i = 0; i < changes.size(); ++i) {
				JsonArray sidePriceQty = changes.get(i).getAsJsonArray();
				boolean buy = sidePriceQty.get(0).getAsString().equals("buy");
				double price = Double.parseDouble(sidePriceQty.get(1).getAsString());
				double qty = Double.parseDouble(sidePriceQty.get(2).getAsString());
				// NOTE Only initialise bid / ask arrays when we come across first bid / ask respectively.
				// In case of both bids and asks they will likely be over-sized to avoid having to loop twice.
				if (buy) {
					if (l2Message.bidCount() == 0) {
						l2Message.initializeBids(changes.size() - l2Message.askCount());
					}
					l2Message.addBid(price, qty);
				} else {
					if (l2Message.askCount() == 0) {
						l2Message.initializeAsks(changes.size() - l2Message.bidCount());
					}
					l2Message.addAsk(price, qty);
				}
			}
			String time = e.get("time").getAsString();
			Instant utc = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(time));
			l2Message.time = utc;
		}
		return l2Message;
	}

    @Override
    public boolean willDecode(String s) {
        return s.startsWith("{\"type\":\"l2update\"") || s.startsWith("{\"type\":\"snapshot\"");
    }

}
