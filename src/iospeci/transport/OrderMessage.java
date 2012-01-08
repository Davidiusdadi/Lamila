package iospeci.transport;

import iospeci.Buffercontent;

import java.net.UnknownHostException;

/**
 * Represents the beginn of a response if the response in made up of more than
 * one package
 * 
 * @author David Rohmer
 * 
 */
public class OrderMessage extends Buffercontent {
	public static final int type = -2;

	public OrderMessage( int startid ) throws UnknownHostException {
		initBuffer( 0, type, startid );
	}

	public OrderMessage( int length , byte[] bytes ) {
		super( length, bytes );
	}

	/**
	 * The id of the preview that was send.
	 * Just passes this call to super.getPacketId()
	 */
	@Override
	public int getPacketId() {
		return super.getPacketId();
	}

	/**
	 * Returns the id of the preview this message is an order for.
	 */
	public int getOrderId() {
		return getPacketId();
	}

	@Override
	public String toString() {
		return super.toString() + " ordering for: " + getPacketId();
	}
}
