package iospeci;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class UDPMessage {
	Buffercontent content;
	protected InetAddress host;
	protected int port;

	public UDPMessage( String host , int port , Buffercontent content ) throws UnknownHostException {
		if( host == null || port < 1 )
			throw new IllegalArgumentException();
		this.host = InetAddress.getByName( host );
		this.port = port;
		this.content = content;
	}

	public UDPMessage( DatagramPacket packet ) throws UnknowTypeException {
		host = packet.getAddress();
		port = packet.getPort();
		content = Buffercontent.convert( (InetSocketAddress) packet.getSocketAddress(), packet.getLength(), packet.getData() );

	}

	public DatagramPacket getAsPacket() {
		return new DatagramPacket( content.bb.array(), content.bb.array().length, this.host, port );
	}

	public String getHost() {
		return getAsPacket().getAddress().getHostAddress();
	}

	public int getPort() {
		return port;
	}
}
