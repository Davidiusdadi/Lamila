package iospeci;

import java.net.UnknownHostException;

public abstract class Responsemessage extends Buffercontent {
	private final Integer messageid;

	@Override
	protected int getBufferoffset() {
		return super.getBufferoffset() + 4;
	}

	@Override
	protected void initBuffer( int size, int messagetype, int packetid ) {
		super.initBuffer( size + 4, messagetype, packetid );
		bb.putInt( messageid );
	}

	/**
	 * Returns the messageid of of the request this message is a response to.
	 * 
	 * @see Requestmessage#getMessageId()
	 */
	public int getRequestMessageId() {
		return bb.getInt( super.getBufferoffset() );
	}

	public Responsemessage( int length , byte[] bytes ) {
		super( length, bytes );
		messageid = null;
	}

	public Responsemessage( int messagetype , int size , int messageid ) throws UnknownHostException {
		this.messageid = messageid;
		initBuffer( size, messagetype );
	}

	public Responsemessage( int messageid ) {
		this.messageid = messageid;
	}

	@Override
	public String toString() {
		return super.toString() + " mesid:" + getRequestMessageId();
	}
}
