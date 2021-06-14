package coinbase.websocket.decoder;

public class L2Update extends L2Message {
	
	public L2Update(String product_id) {
		super(product_id);
	}
	
	@Override
	public String type() {
		return "l2update";
	}
	
	@Override
	public boolean isSnapshot() {
		return false;
	}

	@Override
	public boolean isL2Update() {
		return true;
	}

}
