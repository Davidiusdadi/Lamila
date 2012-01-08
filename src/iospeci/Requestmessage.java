package iospeci;

import iospeci.transport.ResponsePreview;

import java.net.UnknownHostException;

/**
 * Groundclass for all Requests
 * 
 * @author David Rohmer
 */
public abstract class Requestmessage extends Buffercontent {
	private Integer offset;

	@Override
	protected int getBufferoffset() {
		return super.getBufferoffset() + 4;
	}

	public Requestmessage() {
	}

	public Requestmessage( int length , byte[] bytes ) {
		super( length, bytes );
	}

	/* @Override
	 * protected void initBuffer( int size , int messagetype,Requestmessage ){
	 * super.initBuffer( size + 4 , messagetype );
	 * bb.putInt( getNextId() );
	 * } */
	@Override
	protected void initBuffer( int size, int messagetype, int packetid ) {
		super.initBuffer( size + 4, messagetype, packetid );
		bb.putInt( getNextId() );
	}

	public Requestmessage( int messagetype , int size ) throws UnknownHostException {
		initBuffer( size, messagetype );
	}

	/**
	 * The id of this message, by which the response can be assined to the
	 * request.
	 */
	public int getMessageId() {
		return bb.getInt( super.getBufferoffset() );
	}

	/**
	 * Returns true if the response will arrive in fragments. That mean the
	 * response will start with a {@link ResponsePreview}
	 */
	abstract public boolean isMultiResponseRequest();

	@Override
	public String toString() {
		return super.toString() + " mesid:" + getMessageId();
	}
}
