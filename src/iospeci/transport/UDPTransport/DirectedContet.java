package iospeci.transport.UDPTransport;

import iospeci.Buffercontent;
import iospeci.DirectedMessage;

class DirectedContet implements DirectedMessage {
	private byte[] adress;
	private int port;
	private Buffercontent buf;

	public DirectedContet( Buffercontent buf , byte[] adress , int port ) {
		this.adress = adress;
		this.port = port;
		this.buf = buf;
	}

	@Override
	public byte[] getHost() {
		return adress;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public Buffercontent getContent() {
		return buf;
	}
}