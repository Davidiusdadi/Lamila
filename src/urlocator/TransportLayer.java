package urlocator;

import iospeci.Buffercontent;
import iospeci.CryptManager;
import iospeci.DirectedMessage;
import iospeci.OpenRange;
import iospeci.transport.RecepientInputStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public interface TransportLayer extends Runnable {
	public void setMessageHandler( Messagehandler handler );

	public Thread getThread();

	public void send( DirectedMessage mes ) throws UnknownHostException , IOException;

	public void send( Buffercontent m, byte[] adress, int port ) throws UnknownHostException , IOException;

	public void setCrypter( CryptManager crypt );

	/***/
	public RecepientInputStream previewTransfere( SocketAddress adrss, OpenRange range, byte cryption ) throws IOException;

	/**
	 * Returns the local SocketAdress
	 */
	public InetSocketAddress getAdress();
}
