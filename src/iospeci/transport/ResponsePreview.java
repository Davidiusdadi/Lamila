package iospeci.transport;

import iospeci.DirectedRequestmessage;
import iospeci.DirectedResponsemessage;

import java.net.UnknownHostException;

/**
 * Represents the beginn of a response if the response in made up of more than
 * one package
 * 
 * @author David Rohmer
 * 
 */
public class ResponsePreview extends DirectedResponsemessage {
	public static final int type = 2;

	public ResponsePreview( DirectedRequestmessage req , int messagecount ) throws UnknownHostException {
		super( req, type, 4 );
		bb.putInt( messagecount );
	}

	public ResponsePreview( int length , byte[] bytes ) {
		super( length, bytes );
	}

	/**
	 * Returns how many responses will follow after this preview
	 */
	public int getIncommingMessageCount() {
		return bb.getInt( getBufferoffset() );
	}

	@Override
	public String toString() {
		return super.toString() + " await: " + getIncommingMessageCount();
	}
}
