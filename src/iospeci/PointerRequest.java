package iospeci;

import globalstatic.Lamilastatics;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import urlocator.Node;

/**
 * Represents a Requestmessage for nodeids with have a smaller distance to the
 * given key than the retriver.
 * 
 * @author David Rohmer
 */
public class PointerRequest extends DirectedRequestmessage {
	public static final int type = 1;

	public PointerRequest( InetSocketAddress adress , byte[] nodeid , Integer correlatorid , byte[] key , int responsecount ) throws UnknownHostException {
		super( adress );
		initBuffer( 4 + 4 + 4 + 4 + key.length + nodeid.length, type );
		bb.putInt( correlatorid );// 0
		bb.putInt( responsecount );// 4
		bb.putInt( nodeid.length );// 8
		bb.putInt( key.length );// 12
		bb.put( nodeid );// 16
		bb.put( key );//
	}

	public PointerRequest( final int length , final byte[] bytes ) {
		super( length, bytes );
	}

	/**
	 * Returns the id of the Correlator which is to use for the correlation.
	 */
	public int getCorrelatorId() throws InvalidPackageException {
		try {
			return bb.getInt( getBufferoffset() );
		} catch ( Exception e ) {
			throw new InvalidPackageException( e );
		}
	}

	// public String getKeyAsString() throws InvalidPackageException {
	// try {
	// return new String(bb.array(), bufferoffset + 20
	// + bb.getInt(bufferoffset + 12), bb
	// .getInt(bufferoffset + 16), globalstatics.charset);
	// } catch (Exception e) {
	// throw new InvalidPackageException(e);
	// }
	// }
	/**
	 * Returns the requested key.
	 */
	public byte[] getKey() throws InvalidPackageException {
		try {
			byte[] key = new byte[ bb.getInt( getBufferoffset() + 12 ) ];
			byte[] arrey = bb.array();
			int beginat = getBufferoffset() + 16 + bb.getInt( getBufferoffset() + 8 );
			for( int i = 0 ; i < key.length ; i++ )
				key[ i ] = arrey[ beginat + i ];
			return key;
		} catch ( Exception e ) {
			throw new InvalidPackageException( e );
		}
	}

	/**
	 * Returns the requested node id
	 */
	public byte[] getNodeId() {
		int bytecount = bb.getInt( getBufferoffset() + 8 );
		byte bytearry[] = new byte[ bytecount ];
		bb.position( getBufferoffset() + 16 );
		bb.get( bytearry );
		return bytearry;
	}

	// public int getMessageId() {
	// int mesid=bb.getInt(getBufferoffset() + 8);
	// return mesid;
	// }
	@Override
	public String toString() {
		return super.toString() + " long: " + Node.convertBytes2Long( getNodeId() ) + " for str: " + new String( getNodeId(), Lamilastatics.charset );
	}

	@Override
	public boolean isMultiResponseRequest() {
		return true;
	}

	/**
	 * Returns how many Pointerresponses the Requestor want to get.
	 */
	public int getLeafes() {
		return bb.getInt( getBufferoffset() + 4 );
	}
}
