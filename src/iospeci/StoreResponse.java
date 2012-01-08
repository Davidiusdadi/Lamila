package iospeci;

import java.net.UnknownHostException;

public class StoreResponse extends DirectedResponsemessage {
	public static final int type = 5;

	public StoreResponse( DirectedRequestmessage req , long timelapse ) throws UnknownHostException {
		super( req );
		initBuffer( 8, type );
		bb.putLong( timelapse );
	}

	public StoreResponse( int length , byte[] bytes ) {
		super( length, bytes );
	}

	public long getTimelapse() {
		return bb.getLong( getBufferoffset() );
	}
}
