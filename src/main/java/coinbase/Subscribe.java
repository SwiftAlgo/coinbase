package coinbase;

public class Subscribe {
	
	public Subscribe(String[] product_ids, Channel[] channels) {
		this.product_ids = product_ids;
		this.channels = channels;
	}
	
	private final String type = "subscribe";
	
	private String[] product_ids;
	
	private Channel[] channels;
	
    // Used for signing the subscribe message to the Websocket feed
	public void addSignature(String signature, String passphrase, String timestamp, String apiKey) {
		this.signature = signature;
		this.passphrase = passphrase;
		this.timestamp = timestamp;
		this.apiKey = apiKey;
	}
	
    private String signature;
    private String passphrase;
    private String timestamp;
    private String apiKey;

}
