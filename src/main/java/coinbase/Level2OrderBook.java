package coinbase;

import java.util.Objects;

import coinbase.websocket.decoder.L2Snapshot;
import coinbase.websocket.decoder.L2Update;

public class Level2OrderBook {

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

    public String toString(int maxLevelsToDisplay) {
        int actualLevels = Math.min(maxLevelsToDisplay, Math.max(bidSide.currentLevels(), askSide.currentLevels()));
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("\n%-12s        BID | ASK\n", productId));
        for (int i = 0; i < actualLevels; ++i) {
            if (i < bidSide.currentLevels()) {
                sb.append(String.format("%-10s - %-10s", Double.toString(bidSide.sizeAtIndex(i)), Double.toString(bidSide.priceAtIndex(i))));
            }
            sb.append(" | ");
            if (i < askSide.currentLevels()) {
                sb.append(String.format("%-10s - %-10s", Double.toString(askSide.priceAtIndex(i)), Double.toString(askSide.sizeAtIndex(i))));
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
