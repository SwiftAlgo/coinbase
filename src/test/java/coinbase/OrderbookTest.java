package coinbase;

import static org.junit.jupiter.api.Assertions.*;

import javax.websocket.DecodeException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coinbase.decoder.Level2Decoder;
import coinbase.websocket.Level2ClientEndpointConfig;
import coinbase.websocket.decoder.L2Snapshot;
import coinbase.websocket.decoder.L2Update;

class OrderbookTest {
    
    Logger logger = LoggerFactory.getLogger(getClass());

    @Test
	void bestBidAndAskUpdated() throws DecodeException {
	    Level2ClientEndpointConfig config = new Level2ClientEndpointConfig("BTC-USD", 10, 10);
		Level2Decoder decoder = new Level2Decoder();
		decoder.init(config);
		
		L2Snapshot snapshot = (L2Snapshot) decoder.decode("{\"type\":\"snapshot\",\"product_id\":\"BTC-USD\","+
		"\"asks\":[[\"37414.91\",\"3.57264981\"],[\"37415.15\",\"0.56720000\"],[\"37415.66\",\"0.03000000\"],[\"37416.32\",\"0.05658518\"],[\"37416.61\",\"0.05000000\"],[\"37417.59\",\"0.28000000\"],[\"37417.83\",\"0.09977351\"],[\"37419.78\",\"0.09823134\"],[\"37419.79\",\"0.53400000\"],[\"37419.81\",\"0.02500000\"],[\"37419.85\",\"0.00060000\"]]," +
		"\"bids\":[[\"37414.90\",\"6.88766935\"],[\"37410.36\",\"0.86800000\"],[\"37410.31\",\"0.24000000\"],[\"37410.07\",\"0.01062022\"],[\"37410.00\",\"1.52697597\"],[\"37409.93\",\"0.10260855\"],[\"37405.00\",\"0.53200000\"],[\"37404.28\",\"0.86700000\"],[\"37404.23\",\"0.13200000\"],[\"37402.70\",\"1.06600000\"],[\"37402.65\",\"0.02160000\"]]}");

		assertEquals("snapshot[BTC-USD]\n"
		        + "BIDS (10):\n"
		        + "1: 6.887669 @ 37414.900000\n"
		        + "2: 0.868000 @ 37410.360000\n"
		        + "3: 0.240000 @ 37410.310000\n"
		        + "4: 0.010620 @ 37410.070000\n"
		        + "5: 1.526976 @ 37410.000000\n"
		        + "6: 0.102609 @ 37409.930000\n"
		        + "7: 0.532000 @ 37405.000000\n"
		        + "8: 0.867000 @ 37404.280000\n"
		        + "9: 0.132000 @ 37404.230000\n"
		        + "10: 1.066000 @ 37402.700000\n"
		        + "ASKS (10):\n"
		        + "1: 3.572650 @ 37414.910000\n"
		        + "2: 0.567200 @ 37415.150000\n"
		        + "3: 0.030000 @ 37415.660000\n"
		        + "4: 0.056585 @ 37416.320000\n"
		        + "5: 0.050000 @ 37416.610000\n"
		        + "6: 0.280000 @ 37417.590000\n"
		        + "7: 0.099774 @ 37417.830000\n"
		        + "8: 0.098231 @ 37419.780000\n"
		        + "9: 0.534000 @ 37419.790000\n"
		        + "10: 0.025000 @ 37419.810000\n", snapshot.toString());
		
		Level2OrderBook orderBook = new Level2OrderBook("BTC-USD", 10);
		orderBook.onL2Snapshot(snapshot);
		assertEquals(
		        "\n"
		        + "BTC-USD             BID | ASK\n"
		        + "6.88766935 - 37414.9    | 37414.91   - 3.57264981\n"
		        + "0.868      - 37410.36   | 37415.15   - 0.5672    \n"
		        + "0.24       - 37410.31   | 37415.66   - 0.03      \n"
		        + "0.01062022 - 37410.07   | 37416.32   - 0.05658518\n"
		        + "1.52697597 - 37410.0    | 37416.61   - 0.05      \n"
		        + "0.10260855 - 37409.93   | 37417.59   - 0.28      \n"
		        + "0.532      - 37405.0    | 37417.83   - 0.09977351\n"
		        + "0.867      - 37404.28   | 37419.78   - 0.09823134\n"
		        + "0.132      - 37404.23   | 37419.79   - 0.534     \n"
		        + "1.066      - 37402.7    | 37419.81   - 0.025     \n"
		        , orderBook.toString());
		L2Update l2UpdateBid = (L2Update) decoder.decode("{\"type\":\"l2update\",\"product_id\":\"BTC-USD\",\"changes\":[[\"buy\",\"37414.90\",\"7.01192935\"]],\"time\":\"2021-06-13T19:31:24.621461Z\"}");
		assertEquals("l2update[BTC-USD]2021-06-13T19:31:24.621461Z\n"
		        + "BIDS (1):\n"
		        + "1: 7.011929 @ 37414.900000\n"
		        + "ASKS (0):\n", l2UpdateBid.toString());
		
		assertTrue(orderBook.onL2Update(l2UpdateBid));
		assertEquals(
		        "\n"
		        + "BTC-USD             BID | ASK\n"
		        + "7.01192935 - 37414.9    | 37414.91   - 3.57264981\n"
		        + "0.868      - 37410.36   | 37415.15   - 0.5672    \n"
		        + "0.24       - 37410.31   | 37415.66   - 0.03      \n"
		        + "0.01062022 - 37410.07   | 37416.32   - 0.05658518\n"
		        + "1.52697597 - 37410.0    | 37416.61   - 0.05      \n"
		        + "0.10260855 - 37409.93   | 37417.59   - 0.28      \n"
		        + "0.532      - 37405.0    | 37417.83   - 0.09977351\n"
		        + "0.867      - 37404.28   | 37419.78   - 0.09823134\n"
		        + "0.132      - 37404.23   | 37419.79   - 0.534     \n"
		        + "1.066      - 37402.7    | 37419.81   - 0.025     \n",
		        orderBook.toString());
		
		L2Update l2UpdateAsk = (L2Update) decoder.decode("{\"type\":\"l2update\",\"product_id\":\"BTC-USD\",\"changes\":[[\"sell\",\"37414.91\",\"3.69264981\"]],\"time\":\"2021-06-13T19:31:24.624375Z\"}");
	    assertEquals("l2update[BTC-USD]2021-06-13T19:31:24.624375Z\n"
	            + "BIDS (0):\n"
	            + "ASKS (1):\n"
	            + "1: 3.692650 @ 37414.910000\n",
	            l2UpdateAsk.toString());
	    
	    assertTrue(orderBook.onL2Update(l2UpdateAsk));
	    assertEquals(
	            "\n"
	            + "BTC-USD             BID | ASK\n"
	            + "7.01192935 - 37414.9    | 37414.91   - 3.69264981\n"
	            + "0.868      - 37410.36   | 37415.15   - 0.5672    \n"
	            + "0.24       - 37410.31   | 37415.66   - 0.03      \n"
	            + "0.01062022 - 37410.07   | 37416.32   - 0.05658518\n"
	            + "1.52697597 - 37410.0    | 37416.61   - 0.05      \n"
	            + "0.10260855 - 37409.93   | 37417.59   - 0.28      \n"
	            + "0.532      - 37405.0    | 37417.83   - 0.09977351\n"
	            + "0.867      - 37404.28   | 37419.78   - 0.09823134\n"
	            + "0.132      - 37404.23   | 37419.79   - 0.534     \n"
	            + "1.066      - 37402.7    | 37419.81   - 0.025     \n",
	            orderBook.toString());
    }

}
