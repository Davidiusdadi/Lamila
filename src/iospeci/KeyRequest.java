package iospeci;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Representation of a Request for the key of Node
 * 
 * @author David Rohmer
 */
public class KeyRequest extends DirectedRequestmessage {
	public static final int type = 8;

	public KeyRequest( InetSocketAddress adress , byte[] nodeid , int correlatorid ) throws UnknownHostException {
		super( adress, type, 4 + 4 + nodeid.length );
		bb.putInt( correlatorid );
		bb.putInt( nodeid.length );
		bb.put( nodeid );
	}

	public KeyRequest( final int length , final byte[] bytes ) {
		super( length, bytes );
	}

	@Override
	public boolean isMultiResponseRequest() {
		return false;
	}

	/**
	 * Returns the id of the Correlator which is to use for the correlation.
	 */
	public int getCorrelatorId() {
		return bb.getInt( getBufferoffset() );
	}

	/**
	 * Returns the id of the node which is requested
	 */
	public byte[] getNodeId() {
		bb.position( getBufferoffset() + 4 );
		int bytecout = bb.getInt();
		validateArLen( bytecout );
		byte[] id = new byte[ bytecout ];
		bb.get( id );
		return id;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
