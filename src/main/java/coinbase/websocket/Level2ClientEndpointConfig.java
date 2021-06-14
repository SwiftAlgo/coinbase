package coinbase.websocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;

import coinbase.decoder.Level2Decoder;
import coinbase.decoder.PassthroughTextDecoder;

public class Level2ClientEndpointConfig implements ClientEndpointConfig {
	
	private final String productId;
	private final int displayedLevels;
	private final int retainedLevels;
	private final List<Class<? extends Decoder>> decoders = new ArrayList<>();
	
	public Level2ClientEndpointConfig(String productId, int displayedLevels,  int retainedLevels) {
		this.productId = Objects.requireNonNull(productId);
		if (displayedLevels < 1) throw new IllegalArgumentException("displayed levels must be positive");
		if (retainedLevels < displayedLevels) throw new IllegalArgumentException("retained levels must be >= displayed levels");
		this.displayedLevels = displayedLevels;
		this.retainedLevels = retainedLevels;
		decoders.add(Level2Decoder.class);
		// TODO is this still needed?
		decoders.add(PassthroughTextDecoder.class);
	}

	@Override
	public List<Class<? extends Encoder>> getEncoders() {
		return Collections.emptyList();
	}

	@Override
	public List<Class<? extends Decoder>> getDecoders() {
		return decoders;
	}

	@Override
	public Map<String, Object> getUserProperties() {
		return null;
	}

	@Override
	public List<String> getPreferredSubprotocols() {
		return Collections.emptyList();
	}

	@Override
	public List<Extension> getExtensions() {
		return Collections.emptyList();
	}

	@Override
	public Configurator getConfigurator() {
		return new ClientEndpointConfig.Configurator();
	}
	
	public String productId() {
		return productId;
	}
	
	public int displayLevels() {
		return displayedLevels;
	}
	
	public int retainedLevels() {
		return retainedLevels;
	}
	
}
