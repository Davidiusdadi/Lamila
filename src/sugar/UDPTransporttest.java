package sugar;

import globalstatic.Lamilastatics;
import iospeci.Buffercontent;
import iospeci.CryptManager;
import iospeci.InvalidPackageException;
import iospeci.KeyRequest;
import iospeci.KeyResponse;
import iospeci.NodeDescriptionResponse;
import iospeci.PointerRequest;
import iospeci.PointerResponse;
import iospeci.StoreRequest;
import iospeci.transport.OrderMessage;
import iospeci.transport.PacketPreview;
import iospeci.transport.Reordermessage;
import iospeci.transport.ResponsePreview;
import iospeci.transport.UDPTransport.UDPTransporter;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import urlocator.IgnoringPackageException;
import urlocator.Messagehandler;
import urlocator.TransportLayer;
import urlocator.UnknowPacketTypeException;
import correlation.Hashcodecorrelator;

public class UDPTransporttest {
	public void sleep( long time ) {
		try {
			Thread.sleep( time );
		} catch ( InterruptedException e ) {
			e.printStackTrace();
		}
	}

	public UDPTransporttest() throws IOException {
		Buffercontent.addOptainer( OrderMessage.class );
		Buffercontent.addOptainer( PacketPreview.class );
		Buffercontent.addOptainer( Reordermessage.class );
		Buffercontent.addOptainer( KeyRequest.class );
		Buffercontent.addOptainer( PointerRequest.class );
		Buffercontent.addOptainer( StoreRequest.class );
		Buffercontent.addOptainer( KeyResponse.class );
		Buffercontent.addOptainer( NodeDescriptionResponse.class );
		Buffercontent.addOptainer( PointerResponse.class );
		Buffercontent.addOptainer( ResponsePreview.class );
		// Buffercontent.addOptainer( LoginRequest.class );
		// Buffercontent.addOptainer( LoginResponse.class );
		// Buffercontent.addOptainer( Getrequest.class );
		// Buffercontent.addOptainer( .class );

		String testid = "Webtouch";
		Loopback debughandler = new Loopback();
		byte[] testbytes = testid.getBytes( Lamilastatics.charset );
		InetSocketAddress adr = new InetSocketAddress( InetAddress.getByName( "localhost" ), 4430 );
		UDPTransporter trans = new UDPTransporter( new DatagramSocket( adr ) );
		trans.setMessageHandler( debughandler );
		trans.setCrypter( new CryptManager( adr ) );
		for( double i = 5000 ; i > 20 ; i *= 0.7 ) {
			KeyRequest req = new KeyRequest( adr, testbytes, Hashcodecorrelator.id );
			req.setCryption( Buffercontent.BLOWFISH_ENCRYPTION );
			trans.send( req );
			System.out.println( "/////////////////////////////////////////////////" );
			// sleep( ( long ) i );
		}
		System.out.println( "Fin" );
	}

	class Loopback implements Messagehandler {
		public Loopback() {
		}

		@Override
		public void handlePackage( TransportLayer transp, Buffercontent message ) throws UnknowPacketTypeException , IgnoringPackageException , InvalidPackageException , IOException {
			System.out.println( "handle packet: " + message.toString() );
			System.out.println( "Empfangen" );

		}
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main( String[] args ) throws IOException {
		new UDPTransporttest();
	}
}
