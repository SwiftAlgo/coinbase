package coinbase;

import java.util.Objects;

import coinbase.decoder.L2Snapshot;
import coinbase.decoder.L2Update;

public class Level2OrderBook {

    private final String productId;
    private final OrderBookSide bidSide;
    private final OrderBookSide askSide;

    public Level2OrderBook(String productId, int levels) {
        this.productId = Objects.requireNonNull(productId);
        bidSide = new OrderBookSide(true, levels);
        askSide = new OrderBookSide(false, levels);
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
        sb.append(String.format("\nBID | ASK\n"));
        for (int i = 0; i < actualLevels; ++i) {
            if (i < bidSide.currentLevels()) {
                sb.append(String.format("%f - %f", bidSide.sizeAtIndex(i), bidSide.priceAtIndex(i)));
            }
            sb.append(" | ");
            if (i < askSide.currentLevels()) {
                sb.append(String.format("%f - %f", askSide.priceAtIndex(i), askSide.sizeAtIndex(i)));
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
