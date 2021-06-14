package coinbase.websocket.decoder;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class PassthroughTextDecoder implements Decoder.Text<String>{

	@Override
	public void init(EndpointConfig config) {
	
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public String decode(String s) throws DecodeException {
		return s;
	}

	@Override
	public boolean willDecode(String s) {
		return true;
	}

	
}
