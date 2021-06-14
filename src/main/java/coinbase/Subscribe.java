package coinbase;

import coinbase.encoder.Channel;

public class Subscribe {
	
	public Subscribe(String[] product_ids, Channel[] channels) {
		this.product_ids = product_ids;
		this.channels = channels;
	}
	
	private final String type = "subscribe";
	
	private String[] product_ids;
	
	private Channel[] channels;

}
