package coinbase;

import java.util.Objects;

import coinbase.websocket.decoder.L2Snapshot;
import coinbase.websocket.decoder.L2Update;

public class Level2OrderBook {

    private static int MAX_WIDTH = 10;
    
    private final String productId;
    private final Level2OrderBookSide bidSide;
    private final Level2OrderBookSide askSide;

    public Level2OrderBook(String productId, int levels) {
        this.productId = Objects.requireNonNull(productId);
        bidSide = new Level2OrderBookSide(true, levels);
        askSide = new Level2OrderBookSide(false, levels);
    }

    public String productId() {
        return productId;
    }

    public int levels() {
        return bidSide.levels();
    }

    public void onL2Snapshot(L2Snapshot snapshot) {
        bidSide.onL2Snapshot(snapshot);
        askSide.onL2Snapshot(snapshot);
    }

    public boolean onL2Update(L2Update update) {
        return bidSide.onL2Update(update) | askSide.onL2Update(update);
    }

    public String toString() {
        return toString(bidSide.levels());
    }
    
    private String trim(String fp) {
        return (fp.length() > MAX_WIDTH)? fp.substring(0, MAX_WIDTH) : fp;
    }

    public String toString(int maxLevelsToDisplay) {
        int actualLevels = Math.min(maxLevelsToDisplay, Math.max(bidSide.currentLevels(), askSide.currentLevels()));
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n%-12s        BID | ASK\n", productId));
        for (int i = 0; i < actualLevels; ++i) {
            if (i < bidSide.currentLevels()) {
                //NOTE format specifiers "%-10s" should match MAX_WIDTH.
                sb.append(String.format("%-10s - %-10s", trim(Double.toString(bidSide.sizeAtIndex(i))), trim(Double.toString(bidSide.priceAtIndex(i)))));
            }
            else {
                sb.append(String.format("%-23s", " ")); //empty bid
            }
            sb.append(" | ");
            if (i < askSide.currentLevels()) {
              //NOTE format specifiers "%-10s" should match MAX_WIDTH.
                sb.append(String.format("%-10s - %-10s", trim(Double.toString(askSide.priceAtIndex(i))), trim(Double.toString(askSide.sizeAtIndex(i)))));
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
