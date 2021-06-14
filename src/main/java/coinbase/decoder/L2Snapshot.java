package coinbase.decoder;

public class L2Snapshot extends L2Message {

    public L2Snapshot(String product_id) {
        super(product_id);
    }

    @Override
    public String type() {
        return "snapshot";
    }

    @Override
    public boolean isSnapshot() {
        return true;
    }

    @Override
    public boolean isL2Update() {
        return false;
    }

    public void copyBids(double[] targetPrices, double[] targetSizes) {
        System.arraycopy(this.bidPrices, 0, targetPrices, 0, Math.max(bidPrices.length, targetPrices.length));
        System.arraycopy(this.bidSizes, 0, targetSizes, 0, Math.max(bidSizes.length, targetSizes.length));
    }

    public void copyAsks(double[] targetPrices, double[] targetSizes) {
        System.arraycopy(this.askPrices, 0, targetPrices, 0, Math.max(askPrices.length, targetPrices.length));
        System.arraycopy(this.askSizes, 0, targetSizes, 0, Math.max(askSizes.length, targetSizes.length));
    }

}
