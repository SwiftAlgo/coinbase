package coinbase.websocket.decoder;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public abstract class L2Message {
	
	public L2Message(String productId) {
		this.productId = Objects.requireNonNull(productId);
	}
	
    	
	public final String productId;
	
	protected int bidCount = 0;
	protected int askCount = 0;
	
	protected double[] bidPrices;
	protected double[] bidSizes;
	
	protected double[] askPrices;
	protected double[] askSizes;
	
	public Instant time;
	
    /**
     * Type of message as per Coinbase API.
     * @return
     */
    public abstract String type();
    
    public String productId() {
    	return productId;
    }
    
    /**
     * Flag to indicate snapshot message (type:"snapshot").
     * @return
     **/
    public abstract boolean isSnapshot();

    /**
     * Flag to indicate level 2 update message (type:"l2update").
     * @return
     **/
    public abstract boolean isL2Update();
	
	/**
	 * Initialise bid arrays once we know how large to make them.
	 * @param bidLevels
	 */
    public void initializeBids(int bidLevels) {
    	bidPrices = new double[bidLevels];
    	bidSizes = new double[bidLevels];    	
    }
    
	/**
	 * Initialise ask arrays once we know how large to make them.
	 * @param askLevels
	 */
    public void initializeAsks(int askLevels) {
    	askPrices = new double[askLevels];
    	askSizes = new double[askLevels];
    }
	
	public void addBid(double price, double size) {
		bidPrices[bidCount] = price;
		bidSizes[bidCount] = size;
		++bidCount;		
		
	}
	
	public void addAsk(double price, double size) {
		askPrices[askCount] = price;
		askSizes[askCount] = size;
		++askCount;	
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type()).append('[').append(productId).append(']');
		if (isL2Update()) {
			sb.append(DateTimeFormatter.ISO_INSTANT.format(time));
		}
		sb.append(String.format("\nBIDS (%d):\n", bidCount));
		for (int i = 0; i < bidCount; ++i) {
			sb.append(String.format("%d: %f @ %f\n", i+1, bidSizes[i], bidPrices[i]));
		}
		sb.append(String.format("ASKS (%d):\n", askCount));
		for (int i = 0; i < askCount; ++i) {
			sb.append(String.format("%d: %f @ %f\n", i + 1, askSizes[i], askPrices[i]));
		}
		return sb.toString();
	}
	
	public int bidCount() {
		return bidCount;
	}
	
	public int askCount() {
		return askCount;
	}
	
	public double[] bidPrices() {
	    return bidPrices;
	}
	
	public double[] bidSizes() {
	    return bidSizes;
	}
	
	public double[] askPrices() {
	    return askPrices;
	}
	
	public double[] askSizes() {
	    return askSizes;
	}

}
