package iospeci.transport.UDPTransport;

import iospeci.Buffercontent;
import iospeci.DirectedMessage;
import iospeci.InvalidPackageException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

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

	@Override
	public InetSocketAddress getAddress() {
		try {
			return new InetSocketAddress( InetAddress.getByAddress( getHost() ), getPort() );
		} catch ( UnknownHostException e ) {
			throw new InvalidPackageException( e );
		}
	}
}