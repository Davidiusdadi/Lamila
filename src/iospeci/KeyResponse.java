package iospeci;

import java.net.UnknownHostException;

/**
 * Represents a response to a {@link KeyRequest}
 * 
 * @author David Rohmer
 */
public class KeyResponse extends DirectedResponsemessage {
	public static final int type = 7;

	public KeyResponse( KeyRequest req , byte[] key ) throws UnknownHostException {
		super( req, type, 4 + key.length );
		bb.putInt( key.length );
		bb.put( key );
	}

	public KeyResponse( int length , byte[] bytes ) {
		super( length, bytes );
	}

	/**
	 * Returns the key of the requested node with the requested correlator.
	 */
	public byte[] getKey() {
		bb.position( getBufferoffset() );
		byte[] thekey = new byte[ bb.getInt() ];
		bb.get( thekey );
		return thekey;
	}
}
