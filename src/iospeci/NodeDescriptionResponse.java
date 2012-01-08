package iospeci;

import java.net.UnknownHostException;

public class NodeDescriptionResponse extends DirectedResponsemessage {
	public static final int type = 6;

	public NodeDescriptionResponse( DirectedRequestmessage req , byte[] nodekey ) throws UnknownHostException {
		super( req );
		initBuffer( 4 + nodekey.length, type );
		bb.putInt( nodekey.length );
		bb.put( nodekey );
	}

	public NodeDescriptionResponse( int length , byte[] bytes ) {
		super( length, bytes );
	}

	public byte[] getKey() {
		bb.position( getBufferoffset() );
		byte[] key = new byte[ bb.getInt() ];
		bb.get( key );
		return key;
	}
}
