package iospeci;

import java.net.InetSocketAddress;

public interface DirectedMessage {
	public byte[] getHost();

	public int getPort();

	public InetSocketAddress getAddress();

	public Buffercontent getContent();
}